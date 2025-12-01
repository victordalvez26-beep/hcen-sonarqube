package uy.edu.tse.hcen.fragments;

import static uy.edu.tse.hcen.config.AppConfig.DOCS_URL;
import static uy.edu.tse.hcen.config.AppConfig.HISTORY_PDF_URL;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.adapters.ClinicalDocumentsAdapter;
import uy.edu.tse.hcen.dialog.DialogType;
import uy.edu.tse.hcen.dialog.StatusDialogFragment;
import uy.edu.tse.hcen.docs.ClinicalDocument;
import uy.edu.tse.hcen.manager.SessionManager;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import androidx.fragment.app.DialogFragment;

public class ClinicalDocumentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyView;
    private List<ClinicalDocument> documents = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clinical_documents, container, false);

        recyclerView = view.findViewById(R.id.recyclerDocuments);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        emptyView = view.findViewById(R.id.emptyView);

        loadDocuments();

        return view;
    }

    private void loadDocuments() {
        StatusDialogFragment loadingDialog = StatusDialogFragment.newInstance(DialogType.LOADING, "Cargando documentos clínicos");
        loadingDialog.show(getParentFragmentManager(), "loadingDialog");

        new Thread(() -> {
            try {
                URL url = new URL(DOCS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getJwtSession(requireContext()));
                conn.connect();

                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }

                JSONArray array = new JSONArray(sb.toString());
                documents.clear();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    ClinicalDocument doc = new ClinicalDocument();
                    doc.id = obj.optInt("id");
                    doc.documentFormat = obj.optString("formatoDocumento");
                    String rawType = obj.optString("tipoDocumento");
                    doc.documentType = rawType.replace('_', ' ');
                    // Parsear y formatear fecha
                    String rawDate = obj.optString("fechaCreacion");
                    String formattedDate = rawDate;
                    try {
                        java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault());
                        java.util.Date date = isoFormat.parse(rawDate);
                        java.text.SimpleDateFormat outFormat = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", java.util.Locale.getDefault());
                        formattedDate = outFormat.format(date);
                    } catch (Exception e) {
                        // Si falla el parseo, se muestra la original
                    }
                    doc.creationDate = formattedDate;
                    doc.originClinic = obj.optString("clinicaOrigen");
                    doc.professional = obj.optString("profesionalSalud");
                    doc.description = obj.optString("descripcion");
                    doc.accessAllowed = obj.optBoolean("accesoPermitido");
                    doc.documentUri = obj.optString("uriDocumento");
                    documents.add(doc);
                }

                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (documents.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(new ClinicalDocumentsAdapter(documents, this::openDocument));
                    }
                });

            } catch (Exception e) {
                Log.e("ClinicalDocumentsFragment", "Error cargando documentos clínicos", e);
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "No se pudo cargar los documentos. Intente nuevamente");
                    dialog.setOnDismissAction(() -> requireActivity().onBackPressed());
                    dialog.show(getParentFragmentManager(), "errorDialog");
                });
            }
        }).start();
    }

    private void openDocument(ClinicalDocument doc) {
        StatusDialogFragment loadingDialog = StatusDialogFragment.newInstance(DialogType.LOADING, "Abriendo documento");
        loadingDialog.show(getParentFragmentManager(), "loadingDialog");

        new Thread(() -> {
            try {
                File pdfFile = new File(requireContext().getExternalFilesDir(null), "documento-clinico-" + doc.id + ".pdf");
                URL url = new URL(doc.documentUri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getJwtSession(requireContext()));
                conn.connect();

                try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(pdfFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
                }

                Uri pdfUri = androidx.core.content.FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        pdfFile
                );

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    try {
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e) {
                        StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "No hay visor de PDF instalado");
                        dialog.show(getParentFragmentManager(), "errorDialog");
                    }
                });

            } catch (Exception e) {
                Log.e("ClinicalDocumentsFragment", "Error abriendo documento clínico", e);
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "No se pudo abrir el documento. Intente nuevamente");
                    dialog.show(getParentFragmentManager(), "errorDialog");
                });
            }
        }).start();
    }
}
