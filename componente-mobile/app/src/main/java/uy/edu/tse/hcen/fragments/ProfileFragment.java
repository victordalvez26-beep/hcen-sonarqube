package uy.edu.tse.hcen.fragments;

import static uy.edu.tse.hcen.config.AppConfig.FRONTEND_URL;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.splash.SplashActivity;
import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.manager.SessionManager;
import uy.edu.tse.hcen.manager.UserManager;



public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        View sectionHelp = view.findViewById(R.id.sectionHelp);
        View sectionSettings = view.findViewById(R.id.sectionSettings);
        View sectionHCEN = view.findViewById(R.id.sectionHCEN);

        setupSection(sectionHelp, R.drawable.ic_help, "Ayuda y FAQs",
                Arrays.asList("Servicio de ayuda"));

        setupSection(sectionSettings, R.drawable.ic_settings, "Configuración y privacidad",
                Arrays.asList("Mis datos", "Gestionar notificaciones"));

        setupSection(sectionHCEN, R.drawable.ic_logo, "HCEN",
                Arrays.asList("HCEN Web"));

        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void setupSection(View section, int iconRes, String title, List<String> options) {
        ImageView imgIcon = section.findViewById(R.id.imgIcon);
        TextView txtTitle = section.findViewById(R.id.txtTitle);
        ImageView imgArrow = section.findViewById(R.id.imgArrow);
        LinearLayout header = section.findViewById(R.id.headerLayout);
        LinearLayout content = section.findViewById(R.id.contentLayout);

        imgIcon.setImageResource(iconRes);
        txtTitle.setText(title);

        // Agregar botones dinámicamente
        content.removeAllViews();
        for (String opt : options) {
            Button btn = new Button(requireContext());
            btn.setText(opt);
            btn.setAllCaps(false);
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            content.addView(btn);

            btn.setOnClickListener(v -> {
                Fragment fragment = null;
                switch (opt) {
                    case "Servicio de ayuda":
                        fragment = new HelpFragment();
                        break;
                    case "Mis datos":
                        fragment = new MyDataFragment();
                        break;
                    case "Gestionar notificaciones":
                        fragment = new ManageNotificationsFragment();
                        break;
                    case "HCEN Web":
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(FRONTEND_URL));
                        startActivity(browserIntent);
                        return;
                }

                if (fragment != null) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment) // contenedor principal
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // Toggle
        header.setOnClickListener(v -> {
            if (content.getVisibility() == View.GONE) {
                content.setVisibility(View.VISIBLE);
                imgArrow.setImageResource(R.drawable.ic_arrow_up);
            } else {
                content.setVisibility(View.GONE);
                imgArrow.setImageResource(R.drawable.ic_arrow_down);
            }
        });
    }


    private void logout() {
        new Thread(() -> {
            String gubUyLogoutUrl = null;
            try {
                String jwt = SessionManager.getJwtSession(requireContext());
                if (jwt != null) {
                    URL url = new URL(AppConfig.LOGOUT_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(false); // No seguir redirect automáticamente
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Cookie", "hcen_session=" + jwt);
                    int responseCode = conn.getResponseCode();
                    Log.i(TAG, "logout: Codigo de respuesta backend: " + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_SEE_OTHER || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                        String location = conn.getHeaderField("Location");
                        if (location != null && location.contains("gub.uy")) {
                            gubUyLogoutUrl = location;
                        }
                    }
                }
                // Si hay URL de logout de GubUy, hacer petición HTTP en background
                if (gubUyLogoutUrl != null) {
                    try {
                        URL gubuyUrl = new URL(gubUyLogoutUrl);
                        HttpURLConnection gubuyConn = (HttpURLConnection) gubuyUrl.openConnection();
                        gubuyConn.setRequestMethod("GET");
                        gubuyConn.setConnectTimeout(5000);
                        gubuyConn.setReadTimeout(5000);
                        int gubuyCode = gubuyConn.getResponseCode();
                        Log.e(TAG, "URL gubuy: " + gubUyLogoutUrl);
                        Log.i(TAG, "logout GubUy: Codigo de respuesta: " + gubuyCode);
                        gubuyConn.disconnect();
                    } catch (Exception e) {
                        Log.e(TAG, "Error cerrando sesión en GubUy", e);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en logout", e);
            }
            // Limpiar sesión local y navegar
            SessionManager.clearSession(requireContext());
            UserManager.clearUser(requireContext());
            requireActivity().runOnUiThread(() -> {
                Intent intent = new Intent(requireContext(), SplashActivity.class);
                startActivity(intent);
                requireActivity().finish();
            });
        }).start();
    }
}