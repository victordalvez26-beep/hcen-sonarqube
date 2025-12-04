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

    private SwitchCompat switchResults;
    private SwitchCompat switchNewAccessRequest;
    private SwitchCompat switchMedicalHistory;
    private SwitchCompat switchNewAccessHistory;
    private SwitchCompat switchMaintenance;
    private SwitchCompat switchNewFeatures;
    private SwitchCompat switchAll;

    private boolean isGlobalChange = false;
    private static final String KEY_RESULTS = "results";
    private static final String KEY_NEW_ACCESS_REQUEST = "new_access_request";
    private static final String KEY_MEDICAL_HISTORY = "medical_history";
    private static final String KEY_NEW_ACCESS_HISTORY = "new_access_history";
    private static final String KEY_MAINTENANCE = "maintenance";
    private static final String KEY_NEW_FEATURES = "new_features";
    private static final String KEY_ALL_DISABLED = "all_disabled";
    private static final String ERROR_DIALOG_TAG = "errorDialog";
    private static final String SUCCESS_DIALOG_TAG = "successDialog";

    private SharedPreferences prefs;
    boolean isTurningOffGlobalFromIndividual = false;

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

    switchResults.setChecked(prefs.getBoolean(KEY_RESULTS, true));
    switchNewAccessRequest.setChecked(prefs.getBoolean(KEY_NEW_ACCESS_REQUEST, true));
    switchMedicalHistory.setChecked(prefs.getBoolean(KEY_MEDICAL_HISTORY, true));
    switchNewAccessHistory.setChecked(prefs.getBoolean(KEY_NEW_ACCESS_HISTORY, true));
    switchMaintenance.setChecked(prefs.getBoolean(KEY_MAINTENANCE, true));
    switchNewFeatures.setChecked(prefs.getBoolean(KEY_NEW_FEATURES, true));
    switchAll.setChecked(prefs.getBoolean(KEY_ALL_DISABLED, false));

    switchResults.setOnCheckedChangeListener((buttonView, isChecked) ->
        handleIndividualSwitchChange(KEY_RESULTS, isChecked));
    switchNewAccessRequest.setOnCheckedChangeListener((buttonView, isChecked) ->
        handleIndividualSwitchChange(KEY_NEW_ACCESS_REQUEST, isChecked));
    switchMedicalHistory.setOnCheckedChangeListener((buttonView, isChecked) ->
        handleIndividualSwitchChange(KEY_MEDICAL_HISTORY, isChecked));
    switchNewAccessHistory.setOnCheckedChangeListener((buttonView, isChecked) ->
        handleIndividualSwitchChange(KEY_NEW_ACCESS_HISTORY, isChecked));
    switchMaintenance.setOnCheckedChangeListener((buttonView, isChecked) ->
        handleIndividualSwitchChange(KEY_MAINTENANCE, isChecked));
    switchNewFeatures.setOnCheckedChangeListener((buttonView, isChecked) ->
        handleIndividualSwitchChange(KEY_NEW_FEATURES, isChecked));

        switchAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isTurningOffGlobalFromIndividual) {
                // si el apagado vino de un cambio individual, no ejecutar la lógica global
                isTurningOffGlobalFromIndividual = false;
                return;
            }

            prefs.edit().putBoolean(KEY_ALL_DISABLED, isChecked).apply();

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

    private void handleIndividualSwitchChange(String key, boolean isChecked) {
        prefs.edit().putBoolean(key, isChecked).apply();

        if (isGlobalChange) {
            return; // ignorar cambios internos
        }

        // Si estaba activo "all_disabled" y el usuario prende un switch individual, apagar el global pero marcando que vino de un cambio individual
        if (isChecked && switchAll.isChecked()) {
            isTurningOffGlobalFromIndividual = true;
            switchAll.setChecked(false);
            prefs.edit().putBoolean(KEY_ALL_DISABLED, false).apply();
        }

        // Si todos los individuales están apagados, activar "all_disabled"
        boolean allOff = !switchResults.isChecked()
                && !switchNewAccessRequest.isChecked()
                && !switchMedicalHistory.isChecked()
                && !switchNewAccessHistory.isChecked()
                && !switchMaintenance.isChecked()
                && !switchNewFeatures.isChecked();
        if (allOff && !switchAll.isChecked()) {
            isGlobalChange = true;
            switchAll.setChecked(true);
            prefs.edit().putBoolean(KEY_ALL_DISABLED, true).apply();
            isGlobalChange = false;
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
            errorDialog.show(getParentFragmentManager(), ERROR_DIALOG_TAG);
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
                        errorDialog.show(getParentFragmentManager(), ERROR_DIALOG_TAG);
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
                .putBoolean(KEY_RESULTS, notifyResults)
                .putBoolean(KEY_NEW_ACCESS_REQUEST, notifyNewAccessRequest)
                .putBoolean(KEY_MEDICAL_HISTORY, notifyMedicalHistory)
                .putBoolean(KEY_NEW_ACCESS_HISTORY, notifyNewAccessHistory)
                .putBoolean(KEY_MAINTENANCE, notifyMaintenance)
                .putBoolean(KEY_NEW_FEATURES, notifyNewFeatures)
                .putBoolean(KEY_ALL_DISABLED, allDisabled)
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
                        errorDialog.show(getParentFragmentManager(), ERROR_DIALOG_TAG);
                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(
                            DialogType.ERROR,
                            "Error al obtener preferencias"
                    );
                    errorDialog.show(getParentFragmentManager(), ERROR_DIALOG_TAG);
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
            json.put("notifyResults", prefs.getBoolean(KEY_RESULTS, true));
            json.put("notifyNewAccessRequest", prefs.getBoolean(KEY_NEW_ACCESS_REQUEST, true));
            json.put("notifyMedicalHistory", prefs.getBoolean(KEY_MEDICAL_HISTORY, true));
            json.put("notifyNewAccessHistory", prefs.getBoolean(KEY_NEW_ACCESS_HISTORY, true));
            json.put("notifyMaintenance", prefs.getBoolean(KEY_MAINTENANCE, true));
            json.put("notifyNewFeatures", prefs.getBoolean(KEY_NEW_FEATURES, true));
            json.put("allDisabled", prefs.getBoolean(KEY_ALL_DISABLED, false));
        } catch (JSONException e) {
            StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error al preparar preferencias");
            errorDialog.show(getParentFragmentManager(), ERROR_DIALOG_TAG);
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
                        errorDialog.show(getParentFragmentManager(), ERROR_DIALOG_TAG);
                        return;
                    }

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        boolean success = jsonResponse.optBoolean("success", false);
                        String message = jsonResponse.optString("message", "");

                        if (success) {
                            StatusDialogFragment successDialog = StatusDialogFragment.newInstance(DialogType.SUCCESS, message);
                            successDialog.show(getParentFragmentManager(), SUCCESS_DIALOG_TAG);
                        } else {
                            StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, message);
                            errorDialog.show(getParentFragmentManager(), ERROR_DIALOG_TAG);
                        }

                    } catch (JSONException e) {
                        StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error al interpretar respuesta del servidor");
                        errorDialog.show(getParentFragmentManager(), ERROR_DIALOG_TAG);
                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    StatusDialogFragment errorDialog = StatusDialogFragment.newInstance(DialogType.ERROR, "Error al actualizar preferencias");
                    errorDialog.show(getParentFragmentManager(), ERROR_DIALOG_TAG);
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
