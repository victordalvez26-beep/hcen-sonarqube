package uy.edu.tse.hcen.fragments;

import static uy.edu.tse.hcen.config.AppConfig.HISTORY_PDF_URL;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.dialog.DialogType;
import uy.edu.tse.hcen.dialog.StatusDialogFragment;
import uy.edu.tse.hcen.manager.SessionManager;
import uy.edu.tse.hcen.manager.UserManager;
import uy.edu.tse.hcen.model.User;

public class HomeFragment extends Fragment {

    private long lastBackPressedTime = 0;
    private static final int BACK_PRESS_INTERVAL = 2000; // 2 segundos
    private static final String SESSION_EXPIRED = "sessionExpired";
    private static final String NETWORK_ERROR = "networkError";
    private static final String WELCOME_MESSAGE_PREFIX = "¡Bienvenido, ";
    private View rootView;

    private TextView txtWelcome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long now = System.currentTimeMillis();
                if (now - lastBackPressedTime < BACK_PRESS_INTERVAL) {
                    requireActivity().finish();
                } else {
                    lastBackPressedTime = now;
                    android.widget.Toast.makeText(requireContext(), "Presione dos veces para salir", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        SessionManager.checkSessionAndProceed(requireContext(), new SessionManager.SessionCheckCallback() {
            @Override
            public void onSessionValid() {
                refreshWelcome();
            }

            @Override
            public void onSessionInvalid() {
                if (!isAdded() || getView() == null || getActivity() == null || isRemoving() || isDetached()) return;
                StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "La sesión ha expirado. Por favor, inicia sesión nuevamente");
                dialog.show(getParentFragmentManager(), SESSION_EXPIRED);
                getParentFragmentManager().executePendingTransactions();
                StatusDialogFragment shownDialog = (StatusDialogFragment) getParentFragmentManager().findFragmentByTag(SESSION_EXPIRED);
                if (shownDialog != null) {
                    shownDialog.setOnDismissAction(() -> {
                        if (!isAdded() || getView() == null || getActivity() == null || isRemoving() || isDetached()) return;
                        SessionManager.clearSession(requireContext());
                        UserManager.clearUser(requireContext());
                        Intent intent = new Intent(requireContext(), uy.edu.tse.hcen.splash.SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onNetworkError() {
                if (!isAdded() || getView() == null || getActivity() == null || isRemoving() || isDetached()) return;
                StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error de conexión. Conéctate a Internet o intentalo más tarde");
                dialog.show(getParentFragmentManager(), NETWORK_ERROR);
                getParentFragmentManager().executePendingTransactions();
                StatusDialogFragment shownDialog = (StatusDialogFragment) getParentFragmentManager().findFragmentByTag(NETWORK_ERROR);
                if (shownDialog != null) {
                    shownDialog.setOnDismissAction(() -> {
                        new android.os.Handler().postDelayed(() -> {
                            if (isAdded() && getView() != null && getActivity() != null && !isRemoving() && !isDetached()) {
                                onCreateView(inflater, container, savedInstanceState);
                            }
                        }, 500);
                    });
                }
            }
        });

        txtWelcome = rootView.findViewById(R.id.txtWelcome);

        // Cargar nombre del usuario
        refreshWelcome();

        MaterialCardView cardHistory = rootView.findViewById(R.id.cardHistory);
        MaterialCardView cardClinicalDocs = rootView.findViewById(R.id.cardClinicalDocs);
        MaterialCardView cardSummary = rootView.findViewById(R.id.cardSummary);
        MaterialCardView cardData = rootView.findViewById(R.id.cardData);

        cardHistory.setOnClickListener(v -> {
            loadHistory();
        });

        cardClinicalDocs.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ClinicalDocumentsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardSummary.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SummaryDigitalFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardData.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MyDataFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return rootView;
    }

    private void checkSessionAndShow() {
    if (!isAdded() || getView() == null || getActivity() == null || isRemoving() || isDetached()) return;
        SessionManager.checkSessionAndProceed(requireContext(), new SessionManager.SessionCheckCallback() {
            @Override
            public void onSessionValid() {
                if (!isAdded() || getView() == null || getActivity() == null || isRemoving() || isDetached()) return;
                txtWelcome = rootView.findViewById(R.id.txtWelcome);
                User user = UserManager.getUser(requireContext());
                txtWelcome.setText(WELCOME_MESSAGE_PREFIX + user.getFirstName() + "!");

                MaterialCardView cardHistory = rootView.findViewById(R.id.cardHistory);
                MaterialCardView cardSummary = rootView.findViewById(R.id.cardSummary);
                MaterialCardView cardData = rootView.findViewById(R.id.cardData);

                cardHistory.setOnClickListener(v -> {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ClinicalDocumentsFragment())
                            .addToBackStack(null)
                            .commit();
                });

                cardSummary.setOnClickListener(v -> {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new SummaryDigitalFragment())
                            .addToBackStack(null)
                            .commit();
                });

                cardData.setOnClickListener(v -> {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new MyDataFragment())
                            .addToBackStack(null)
                            .commit();
                });
            }

            @Override
            public void onSessionInvalid() {
                if (!isAdded() || getView() == null) return;
                StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "La sesión ha expirado. Por favor, inicia sesión nuevamente");
                dialog.show(getParentFragmentManager(), SESSION_EXPIRED);
                getParentFragmentManager().executePendingTransactions();
                StatusDialogFragment shownDialog = (StatusDialogFragment) getParentFragmentManager().findFragmentByTag(SESSION_EXPIRED);
                if (shownDialog != null) {
                    shownDialog.setOnDismissAction(() -> {
                        if (!isAdded() || getView() == null || getActivity() == null || isRemoving() || isDetached()) return;
                        SessionManager.clearSession(requireContext());
                        UserManager.clearUser(requireContext());
                        Intent intent = new Intent(requireContext(), uy.edu.tse.hcen.splash.SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onNetworkError() {
                if (!isAdded() || getView() == null || getActivity() == null || isRemoving() || isDetached()) return;
                StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Sin conexión. Conéctate a Internet");
                dialog.show(getParentFragmentManager(), NETWORK_ERROR);
                getParentFragmentManager().executePendingTransactions();
                StatusDialogFragment shownDialog = (StatusDialogFragment) getParentFragmentManager().findFragmentByTag(NETWORK_ERROR);
                if (shownDialog != null) {
                    shownDialog.setOnDismissAction(() -> {
                        new android.os.Handler().postDelayed(() -> {
                            if (isAdded() && getView() != null && getActivity() != null && !isRemoving() && !isDetached()) {
                                checkSessionAndShow();
                            }
                        }, 500);
                    });
                }
            }
        });

        // Cargar nombre del usuario
        User user = UserManager.getUser(requireContext());
        txtWelcome.setText(WELCOME_MESSAGE_PREFIX + user.getFirstName() + "!");

        MaterialCardView cardHistory = rootView.findViewById(R.id.cardHistory);
        MaterialCardView cardClinicalDocs = rootView.findViewById(R.id.cardClinicalDocs);
        MaterialCardView cardSummary = rootView.findViewById(R.id.cardSummary);
        MaterialCardView cardData = rootView.findViewById(R.id.cardData);

        cardHistory.setOnClickListener(v -> {
            loadHistory();
        });

        cardClinicalDocs.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ClinicalDocumentsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardSummary.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SummaryDigitalFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardData.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MyDataFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadHistory() {
        StatusDialogFragment loadingDialog = StatusDialogFragment.newInstance(DialogType.LOADING, "Cargando historia clínica");
        loadingDialog.show(getParentFragmentManager(), "loadingDialog");

        new Thread(() -> {
            try {
                File pdfFile = new File(requireContext().getExternalFilesDir(null), "historia-clinica.pdf");
                URL url = new URL(HISTORY_PDF_URL);
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
                Log.e("ClinicalDocumentsFragment", "Error abriendo historia clínica", e);
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    StatusDialogFragment dialog = StatusDialogFragment.newInstance(DialogType.ERROR, "No se pudo abrir la historia. Intente nuevamente");
                    dialog.show(getParentFragmentManager(), "errorDialog");
                });
            }
        }).start();
    }

    private void refreshWelcome() {
        if (txtWelcome != null && isAdded() && getActivity() != null) {
            User user = UserManager.getUser(requireContext());
            String text = (user != null && user.getFirstName() != null) ? WELCOME_MESSAGE_PREFIX + user.getFirstName() + "!" : "¡Bienvenido!";
            txtWelcome.setText(text);
        }
    }
}