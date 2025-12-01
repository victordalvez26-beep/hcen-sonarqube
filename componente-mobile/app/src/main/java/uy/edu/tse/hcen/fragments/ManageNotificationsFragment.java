package uy.edu.tse.hcen.fragments;

import static uy.edu.tse.hcen.config.AppConfig.BACKEND_URL;
import static uy.edu.tse.hcen.config.AppConfig.MANAGE_NOTIFICATIONS_URL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.dialog.DialogType;
import uy.edu.tse.hcen.dialog.StatusDialogFragment;
import uy.edu.tse.hcen.helper.SecurePrefsHelper;
import uy.edu.tse.hcen.manager.SessionManager;

public class ManageNotificationsFragment extends Fragment {

    private SwitchCompat switchResults, switchNewAccessRequest, switchMedicalHistory, switchNewAccessHistory,
            switchMaintenance, switchNewFeatures, switchAll;

    private boolean isGlobalChange = false;
    private boolean isTurningOffGlobalFromIndividual = false;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_notifications, container, false);

        prefs = SecurePrefsHelper.getUserNotificationPrefs(requireContext());

        switchResults = view.findViewById(R.id.switchResults);
        switchNewAccessRequest = view.findViewById(R.id.switchNewAccessRequest);
        switchMedicalHistory = view.findViewById(R.id.switchMedicalHistory);
        switchNewAccessHistory = view.findViewById(R.id.switchNewAccessHistory);
        switchMaintenance = view.findViewById(R.id.switchMaintenance);
        switchNewFeatures = view.findViewById(R.id.switchNewFeatures);
        switchAll = view.findViewById(R.id.switchAll);

        loadNotificationPreferencesFromBackend();

        switchResults.setChecked(prefs.getBoolean("results", true));
        switchNewAccessRequest.setChecked(prefs.getBoolean("new_access_request", true));
        switchMedicalHistory.setChecked(prefs.getBoolean("medical_history", true));
        switchNewAccessHistory.setChecked(prefs.getBoolean("new_access_history", true));
        switchMaintenance.setChecked(prefs.getBoolean("maintenance", true));
        switchNewFeatures.setChecked(prefs.getBoolean("new_features", true));
        switchAll.setChecked(prefs.getBoolean("all_disabled", false));

        switchResults.setOnCheckedChangeListener((buttonView, isChecked) ->
                handleIndividualSwitchChange("results", switchResults, isChecked));
        switchNewAccessRequest.setOnCheckedChangeListener((buttonView, isChecked) ->
                handleIndividualSwitchChange("new_access_request", switchNewAccessRequest, isChecked));
        switchMedicalHistory.setOnCheckedChangeListener((buttonView, isChecked) ->
                handleIndividualSwitchChange("medical_history", switchMedicalHistory, isChecked));
        switchNewAccessHistory.setOnCheckedChangeListener((buttonView, isChecked) ->
                handleIndividualSwitchChange("new_access_history", switchNewAccessHistory, isChecked));
        switchMaintenance.setOnCheckedChangeListener((buttonView, isChecked) ->
                handleIndividualSwitchChange("maintenance", switchMaintenance, isChecked));
        switchNewFeatures.setOnCheckedChangeListener((buttonView, isChecked) ->
                handleIndividualSwitchChange("new_features", switchNewFeatures, isChecked));

        switchAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isTurningOffGlobalFromIndividual) {
                // si el apagado vino de un cambio individual, no ejecutar la lógica global
                isTurningOffGlobalFromIndividual = false;
                return;
            }

            prefs.edit().putBoolean("all_disabled", isChecked).apply();

            isGlobalChange = true; // activar flag
            switchResults.setChecked(!isChecked);
            switchNewAccessRequest.setChecked(!isChecked);
            switchMedicalHistory.setChecked(!isChecked);
            switchNewAccessHistory.setChecked(!isChecked);
            switchMaintenance.setChecked(!isChecked);
            switchNewFeatures.setChecked(!isChecked);
            isGlobalChange = false; // desactivar flag

            String jwt = SessionManager.getJwtSession(requireContext());
            sendNotificationPreferencesToBackend(jwt); // solo una llamada
        });

        return view;
    }

    private void handleIndividualSwitchChange(String key, SwitchCompat changedSwitch, boolean isChecked) {
        prefs.edit().putBoolean(key, isChecked).apply();

        if (isGlobalChange) {
            return; // ignorar cambios internos
        }

        // Si estaba activo "all_disabled" y el usuario prende un switch individual, apagar el global pero marcando que vino de un cambio individual
        if (isChecked && switchAll.isChecked()) {
            isTurningOffGlobalFromIndividual = true;
            switchAll.setChecked(false);
            prefs.edit().putBoolean("all_disabled", false).apply();
        }

        // Si todos los individuales están apagados, activar "all_disabled"
        if (!switchResults.isChecked()
                && !switchNewAccessRequest.isChecked()
                && !switchMedicalHistory.isChecked()
                && !switchNewAccessHistory.isChecked()
                && !switchMaintenance.isChecked()
                && !switchNewFeatures.isChecked()) {
            if (!switchAll.isChecked()) {
                isGlobalChange = true;
                switchAll.setChecked(true);
                prefs.edit().putBoolean("all_disabled", true).apply();
                isGlobalChange = false;
            }
        }

        String jwt = SessionManager.getJwtSession(requireContext());
        sendNotificationPreferencesToBackend(jwt);
    }

    private void loadNotificationPreferencesFromBackend() {
        String jwt = SessionManager.getJwtSession(requireContext());
        if (jwt == null || jwt.isEmpty()) {
            StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(
                    DialogType.ERROR,
                    "Sesión inválida. No se pueden cargar preferencias."
            );
            errorDialog.show(getParentFragmentManager(), "errorDialog");
            return;
        }

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(MANAGE_NOTIFICATIONS_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + jwt);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoInput(true);

                int responseCode = connection.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBuilder.append(line.trim());
                    }
                }

                String responseBody = responseBuilder.toString();

                requireActivity().runOnUiThread(() -> {
                    if (responseBody == null || responseBody.isEmpty()) {
                        StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(
                                DialogType.ERROR,
                                "Respuesta vacía del servidor"
                        );
                        errorDialog.show(getParentFragmentManager(), "errorDialog");
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);

                        boolean notifyResults = json.optBoolean("notifyResults", true);
                        boolean notifyNewAccessRequest = json.optBoolean("notifyNewAccessRequest", true);
                        boolean notifyMedicalHistory = json.optBoolean("notifyMedicalHistory", true);
                        boolean notifyNewAccessHistory = json.optBoolean("notifyNewAccessHistory", true);
                        boolean notifyMaintenance = json.optBoolean("notifyMaintenance", true);
                        boolean notifyNewFeatures = json.optBoolean("notifyNewFeatures", true);
                        boolean allDisabled = json.optBoolean("allDisabled", false);

                        // Guardar en prefs
                        prefs.edit()
                                .putBoolean("results", notifyResults)
                                .putBoolean("new_access_request", notifyNewAccessRequest)
                                .putBoolean("medical_history", notifyMedicalHistory)
                                .putBoolean("new_access_history", notifyNewAccessHistory)
                                .putBoolean("maintenance", notifyMaintenance)
                                .putBoolean("new_features", notifyNewFeatures)
                                .putBoolean("all_disabled", allDisabled)
                                .apply();

                        // Actualizar switches
                        switchResults.setChecked(notifyResults);
                        switchNewAccessRequest.setChecked(notifyNewAccessRequest);
                        switchMedicalHistory.setChecked(notifyMedicalHistory);
                        switchNewAccessHistory.setChecked(notifyNewAccessHistory);
                        switchMaintenance.setChecked(notifyMaintenance);
                        switchNewFeatures.setChecked(notifyNewFeatures);
                        switchAll.setChecked(allDisabled);

                    } catch (JSONException e) {
                        StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(
                                DialogType.ERROR,
                                "Error al interpretar preferencias"
                        );
                        errorDialog.show(getParentFragmentManager(), "errorDialog");
                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(
                            DialogType.ERROR,
                            "Error al obtener preferencias"
                    );
                    errorDialog.show(getParentFragmentManager(), "errorDialog");
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void sendNotificationPreferencesToBackend(String jwt) {
        JSONObject json = new JSONObject();
        try {
            json.put("notifyResults", prefs.getBoolean("results", true));
            json.put("notifyNewAccessRequest", prefs.getBoolean("new_access_request", true));
            json.put("notifyMedicalHistory", prefs.getBoolean("medical_history", true));
            json.put("notifyNewAccessHistory", prefs.getBoolean("new_access_history", true));
            json.put("notifyMaintenance", prefs.getBoolean("maintenance", true));
            json.put("notifyNewFeatures", prefs.getBoolean("new_features", true));
            json.put("allDisabled", prefs.getBoolean("all_disabled", false));
        } catch (JSONException e) {
            StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error al preparar preferencias");
            errorDialog.show(getParentFragmentManager(), "errorDialog");
            return;
        }

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(MANAGE_NOTIFICATIONS_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Authorization", "Bearer " + jwt);
                connection.setDoOutput(true);

                // Enviar body JSON
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBuilder.append(line.trim());
                    }
                }

                String responseBody = responseBuilder.toString();

                requireActivity().runOnUiThread(() -> {
                    if (responseBody == null || responseBody.isEmpty()) {
                        StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error al actualizar preferencias");
                        errorDialog.show(getParentFragmentManager(), "errorDialog");
                        return;
                    }

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        boolean success = jsonResponse.optBoolean("success", false);
                        String message = jsonResponse.optString("message", "");

                        if (success) {
                            StatusDialogFragment successDialog = StatusDialogFragment.newInstance(DialogType.SUCCESS, message);
                            successDialog.show(getParentFragmentManager(), "successDialog");
                        } else {
                            StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, message);
                            errorDialog.show(getParentFragmentManager(), "errorDialog");
                        }

                    } catch (JSONException e) {
                        StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error al interpretar respuesta del servidor");
                        errorDialog.show(getParentFragmentManager(), "errorDialog");
                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error al actualizar preferencias");
                    errorDialog.show(getParentFragmentManager(), "errorDialog");
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
