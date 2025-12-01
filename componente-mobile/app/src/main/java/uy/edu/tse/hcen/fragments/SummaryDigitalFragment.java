package uy.edu.tse.hcen.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.adapters.SummaryAdapter;
import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.dialog.DialogType;
import uy.edu.tse.hcen.dialog.StatusDialogFragment;
import uy.edu.tse.hcen.manager.SessionManager;
import uy.edu.tse.hcen.manager.UserManager;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.summary.SummaryItem;
import uy.edu.tse.hcen.summary.SummaryItemFactory;

public class SummaryDigitalFragment extends Fragment {

    private View emptyView;
    private RecyclerView recyclerSummary;
    private SummaryAdapter adapter;
    private List<SummaryItem> items = new ArrayList<>();
    private View contentContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary_digital, container, false);

        contentContainer = view.findViewById(R.id.contentContainer);
        contentContainer.setVisibility(View.GONE); // Ocultar al inicio

        emptyView = view.findViewById(R.id.emptyView);
        recyclerSummary = view.findViewById(R.id.recyclerSummary);
        recyclerSummary.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SummaryAdapter(new ArrayList<>(), item -> {});
        recyclerSummary.setAdapter(adapter);


        Button btnExportPdf = view.findViewById(R.id.btnExportPdf);
        btnExportPdf.setOnClickListener(v -> generatePdfFromSummary(items));

        getSummaryDigital();

        return view;
    }

    // Mostrar todos los elementos de un tipo en el popup
    private void showTypePopup(String type, List<SummaryItem> items) {
        StringBuilder sb = new StringBuilder();
        for (SummaryItem item : items) {
            sb.append("• ").append(item.description).append("\n\n");
        }
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle(type);
        builder.setMessage(sb.toString().trim());
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void getSummaryDigital() {
        StatusDialogFragment loadingDialog = StatusDialogFragment.newInstance(DialogType.LOADING, "Cargando resumen digital");
        loadingDialog.show(getParentFragmentManager(), "loadingDialog");

        new Thread(() -> {
            try {
                URL url = new URL(AppConfig.SUMMARY_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getJwtSession(requireContext()));
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();

                    if (responseCode == 200) {
                        parseSummary(response.toString());
                        contentContainer.setVisibility(View.VISIBLE);
                    } else {
                        loadingDialog.dismiss();
                        StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "No se pudo obtener el resumen, intenta más tarde");
                        dialog.setOnDismissAction(() -> requireActivity().onBackPressed());
                        dialog.show(getParentFragmentManager(), "errorDialog");
                    }
                });

            } catch (Exception e) {
                Log.e("SummaryDigitalFragment", "Error obteniendo el resumen", e);
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error de red");
                    dialog.setOnDismissAction(() -> requireActivity().onBackPressed());
                    dialog.show(getParentFragmentManager(), "errorDialog");
                });
            }
        }).start();
    }

    private void parseSummary(String jsonResponse) {
        try {
            JSONObject root = new JSONObject(jsonResponse);

            items.clear();

            String[] categories = {"allergies", "conditions", "medications", "immunizations", "observations", "procedures"};
            for (String category : categories) {
                JSONArray arr = root.optJSONArray(category);
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        SummaryItem item = SummaryItemFactory.fromCategory(category, obj);
                        items.add(item);
                    }
                }
            }

            requireActivity().runOnUiThread(() -> {
                // Definir títulos
                String[] titles = {"Alergias", "Diagnósticos", "Medicación", "Vacunas", "Observaciones", "Procedimientos"};

                // Agrupar por categoría original
                java.util.Map<String, List<SummaryItem>> groupedItems = new java.util.LinkedHashMap<>();
                for (String cat : categories) {
                    groupedItems.put(cat, new ArrayList<>());
                }
                for (int i = 0; i < items.size(); i++) {
                    SummaryItem item = items.get(i);
                    // Buscar la categoría original por el título singular
                    for (int j = 0; j < categories.length; j++) {
                        String singular = uy.edu.tse.hcen.summary.SummaryItemFactory.fromCategory(categories[j], new JSONObject()).title;
                        if (item.title.equals(singular)) {
                            groupedItems.get(categories[j]).add(item);
                            break;
                        }
                    }
                }

                // Crear tarjetas por tipo (usando el primer elemento de cada grupo)
                List<SummaryItem> typeCards = new ArrayList<>();
                java.util.Map<String, String> typeTitleMap = new java.util.HashMap<>();
                for (int i = 0; i < categories.length; i++) {
                    String cat = categories[i];
                    String pluralTitle = titles[i];
                    List<SummaryItem> group = groupedItems.get(cat);
                    if (!group.isEmpty()) {
                        SummaryItem first = group.get(0);
                        // Crear una tarjeta con el plural como título
                        typeCards.add(new SummaryItem(pluralTitle, first.description, first.iconResId));
                        typeTitleMap.put(pluralTitle, cat);
                    }
                }
                adapter = new SummaryAdapter(typeCards, item -> {
                    String cat = typeTitleMap.get(item.title);
                    List<SummaryItem> group = groupedItems.get(cat);
                    showTypePopup(item.title, group);
                });
                recyclerSummary.setAdapter(adapter);

                if (typeCards.isEmpty()) {
                    contentContainer.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    contentContainer.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            Log.e("SummaryDigitalFragment", "Error parseando JSON", e);
        }
    }

    private void generatePdfFromSummary(List<SummaryItem> items) {
        StatusDialogFragment loadingDialog = StatusDialogFragment.newInstance(DialogType.LOADING, "Generando PDF");
        loadingDialog.show(getParentFragmentManager(), "loadingDialog");

        new Thread(() -> {
            try {
                PdfDocument document = new PdfDocument();
                Paint paint = new Paint();
                int pageWidth = 595;  // A4 width in points
                int pageHeight = 842; // A4 height in points
                int margin = 40;
                int logoSize = 60;
                int headerHeight = logoSize + 40;
                int footerHeight = 40;
                int contentTop = headerHeight + 30;
                int contentBottom = pageHeight - footerHeight - 20;

                User user = UserManager.getUser(requireContext());
                String patientName = user.getFullName();
                String dateText = "Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

                int y = contentTop;
                int pageNumber = 1;

                PdfDocument.Page page = null;
                Canvas canvas = null;

                // Agrupar por categoría original
                String[] categories = {"allergies", "conditions", "medications", "immunizations", "observations", "procedures"};
                String[] titles = {"Alergias", "Diagnósticos", "Medicación", "Vacunas", "Observaciones", "Procedimientos"};
                java.util.Map<String, List<SummaryItem>> groupedItems = new java.util.LinkedHashMap<>();
                for (String cat : categories) {
                    groupedItems.put(cat, new ArrayList<>());
                }
                for (int i = 0; i < items.size(); i++) {
                    SummaryItem item = items.get(i);
                    for (int j = 0; j < categories.length; j++) {
                        String singular = uy.edu.tse.hcen.summary.SummaryItemFactory.fromCategory(categories[j], new JSONObject()).title;
                        if (item.title.equals(singular)) {
                            groupedItems.get(categories[j]).add(item);
                            break;
                        }
                    }
                }
                for (int i = 0; i < categories.length; i++) {
                    String cat = categories[i];
                    String pluralTitle = titles[i];
                    List<SummaryItem> typeItems = groupedItems.get(cat);
                    if (typeItems == null || typeItems.isEmpty()) continue;

                    if (page == null) {
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        y = drawHeader(canvas, paint, pageWidth, patientName, dateText, logoSize, margin);
                        y += 30;

                        paint.setTextSize(18);
                        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                        paint.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText("Resumen Digital HCEN", pageWidth / 2, y, paint);
                        y += 30;

                        paint.setTextAlign(Paint.Align.LEFT);
                        paint.setTextSize(14);
                        paint.setTypeface(Typeface.DEFAULT);
                    }

                    // Título de la categoría (plural)
                    paint.setTextSize(16);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    canvas.drawText(pluralTitle, margin, y, paint);
                    y += 24;
                    paint.setTextSize(14);
                    paint.setTypeface(Typeface.DEFAULT);

                    for (SummaryItem item : typeItems) {
                        if (y > contentBottom) {
                            drawFooter(canvas, paint, pageWidth, pageHeight, pageNumber);
                            document.finishPage(page);
                            page = null;
                            canvas = null;
                            pageNumber++;
                            // Repetir categoría en nueva página
                            y = contentTop;
                            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                            page = document.startPage(pageInfo);
                            canvas = page.getCanvas();
                            y = drawHeader(canvas, paint, pageWidth, patientName, dateText, logoSize, margin);
                            y += 30;
                            paint.setTextSize(18);
                            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                            paint.setTextAlign(Paint.Align.CENTER);
                            canvas.drawText("Resumen Digital HCEN", pageWidth / 2, y, paint);
                            y += 30;
                            paint.setTextAlign(Paint.Align.LEFT);
                            paint.setTextSize(16);
                            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                            canvas.drawText(pluralTitle, margin, y, paint);
                            y += 24;
                            paint.setTextSize(14);
                            paint.setTypeface(Typeface.DEFAULT);
                        }

                        // Descripción de cada elemento
                        canvas.drawText(item.description, margin + 20, y, paint);
                        y += 20;
                    }
                    y += 16;
                }

                if (page != null) {
                    drawFooter(canvas, paint, pageWidth, pageHeight, pageNumber);
                    document.finishPage(page);
                }

                File pdfFile = new File(requireContext().getExternalFilesDir(null), "resumen-digital-hcen.pdf");
                try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                    document.writeTo(fos);
                }
                document.close();

                Uri pdfUri = FileProvider.getUriForFile(
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
                        StatusDialogFragment successDialog = StatusDialogFragment.newInstance(DialogType.SUCCESS, "PDF generado correctamente");
                        successDialog.show(getParentFragmentManager(), "successDialog");
                    } catch (ActivityNotFoundException e) {
                        StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, "No hay visor de PDF instalado");
                        errorDialog.show(getParentFragmentManager(), "errorDialog");
                    }
                });

            } catch (Exception e) {
                Log.e("SummaryDigitalFragment", "Error generando PDF", e);
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error al generar el PDF");
                    errorDialog.show(getParentFragmentManager(), "errorDialog");
                });
            }
        }).start();
    }

    private int drawHeader(Canvas canvas, Paint paint, int pageWidth, String patientName, String dateText, int logoSize, int margin) {
        int headerTop = 40;

        paint.setTextSize(14);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(patientName, margin, headerTop + 20, paint);
        canvas.drawText(dateText, margin, headerTop + 40, paint);

        Drawable logoDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_logo);
        if (logoDrawable != null) {
            Bitmap logoBitmap = Bitmap.createBitmap(logoSize, logoSize, Bitmap.Config.ARGB_8888);
            Canvas logoCanvas = new Canvas(logoBitmap);
            logoDrawable.setBounds(0, 0, logoSize, logoSize);
            logoDrawable.draw(logoCanvas);
            canvas.drawBitmap(logoBitmap, pageWidth - margin - logoSize, headerTop, null);
        }

        return headerTop + logoSize + 20;
    }

    private void drawFooter(Canvas canvas, Paint paint, int pageWidth, int pageHeight, int pageNumber) {
        paint.setTextSize(12);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Página " + pageNumber, pageWidth / 2, pageHeight - 20, paint);
    }
}