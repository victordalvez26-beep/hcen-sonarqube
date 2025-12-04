package uy.edu.tse.hcen.manager;

import static uy.edu.tse.hcen.config.AppConfig.UPDATE_USER_DATA_URL;
import static uy.edu.tse.hcen.config.AppConfig.USER_DATA_URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uy.edu.tse.hcen.helper.SecurePrefsHelper;
import uy.edu.tse.hcen.model.Department;
import uy.edu.tse.hcen.model.User;

public class UserManager {

    private static final String TAG = "UserManager";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_SECOND_NAME = "secondName";
    private static final String KEY_FIRST_LAST_NAME = "firstLastName";
    private static final String KEY_SECOND_LAST_NAME = "secondLastName";
    private static final String KEY_DOCUMENT_TYPE = "documentType";
    private static final String KEY_DOCUMENT_CODE = "documentCode";
    private static final String KEY_NATIONALITY = "nationality";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_BIRTHDATE = "birthdate";
    private static final String KEY_DEPARTMENT = "department";

    private UserManager() { }

    // Método para consumir datos del backend y actualizar SharedPreferences
    public static void fetchUserDataFromBackend(Context context, String jwt) throws Exception {
        SharedPreferences prefs = SecurePrefsHelper.getSessionPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(USER_DATA_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + jwt);
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

            if (responseCode == HttpURLConnection.HTTP_OK && !responseBody.isEmpty()) {
                JSONObject payload = new JSONObject(responseBody);

                editor.putString(KEY_EMAIL, payload.optString(KEY_EMAIL));
                editor.putString(KEY_FIRST_NAME, payload.optString("primer_nombre"));
                editor.putString(KEY_SECOND_NAME, payload.optString("segundo_nombre"));
                editor.putString(KEY_FIRST_LAST_NAME, payload.optString("primer_apellido"));
                editor.putString(KEY_SECOND_LAST_NAME, payload.optString("segundo_apellido"));
                editor.putString(KEY_DOCUMENT_TYPE, payload.optString("tipo_documento"));
                editor.putString(KEY_DOCUMENT_CODE, payload.optString("codigo_documento"));
                editor.putString(KEY_NATIONALITY, payload.optString("nacionalidad"));
                editor.putString(KEY_LOCATION, payload.optString("localidad"));
                editor.putString(KEY_ADDRESS, payload.optString("direccion"));

                Date birthdate = parseBirthdate(payload.optString("fecha_nacimiento"), "yyyy-MM-dd");
                if (birthdate != null) {
                    editor.putLong(KEY_BIRTHDATE, birthdate.getTime());
                }

                Department department = parseDepartment(payload.optString("departamento"));
                if (department != null) {
                    editor.putString(KEY_DEPARTMENT, department.name());
                }

                editor.commit();
                Log.i(TAG, "Datos de usuario actualizados desde el backend");
            } else {
                throw new BackendException("Backend error. Codigo: " + responseCode);
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void createUser(Context context) {
        new Thread(() -> {
            try {
                String jwt = SessionManager.getJwtSession(context);
                fetchUserDataFromBackend(context, jwt);
            } catch (Exception e) {
                Log.e(TAG, "Error creando usuario", e);
            }
        }).start();
    }

    public static User getUser(Context context) {
        new Thread(() -> {
            try {
                String jwt = SessionManager.getJwtSession(context);
                fetchUserDataFromBackend(context, jwt);
            } catch (Exception e) {
                Log.e(TAG, "Error devolviendo usuario", e);
            }
        }).start();

        // Devolver el usuario guardado en SharedPreferences
        return buildUserFromPrefs(context);
    }


    // Construye el objeto User desde SharedPreferences
    private static User buildUserFromPrefs(Context context) {
        SharedPreferences prefs = SecurePrefsHelper.getSessionPrefs(context);

    String email = prefs.getString(KEY_EMAIL, null);
    String firstName = prefs.getString(KEY_FIRST_NAME, null);
    String secondName = prefs.getString(KEY_SECOND_NAME, null);
    String firstLastName = prefs.getString(KEY_FIRST_LAST_NAME, null);
    String secondLastName = prefs.getString(KEY_SECOND_LAST_NAME, null);
    String documentType = prefs.getString(KEY_DOCUMENT_TYPE, null);
    String documentCode = prefs.getString(KEY_DOCUMENT_CODE, null);
    String nationality = prefs.getString(KEY_NATIONALITY, null);

    long fechaMillis = prefs.getLong(KEY_BIRTHDATE, -1);
        Date birthdate = fechaMillis > 0 ? new Date(fechaMillis) : null;

    String depString = prefs.getString(KEY_DEPARTMENT, null);
        Department department = parseDepartment(depString);

    String location = prefs.getString(KEY_LOCATION, null);
    String address = prefs.getString(KEY_ADDRESS, null);

        return new User(
                email, firstName, secondName, firstLastName, secondLastName,
                documentType, documentCode, nationality, birthdate, department,
                location, address
        );
    }

    public static void saveUser(Context context,
                                            String email,
                                            String departmentStr,
                                            String location,
                                            String address,
                                            Runnable onSuccess) {
        Department department = parseDepartment(departmentStr);

        new Thread(() -> {
            try {
                String jwt = SessionManager.getJwtSession(context);
                URL url = new URL(UPDATE_USER_DATA_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Authorization", "Bearer " + jwt);
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                if (email != null) body.put(KEY_EMAIL, email);
                if (department != null) body.put(KEY_DEPARTMENT, department.name());
                if (location != null) body.put(KEY_LOCATION, location);
                if (address != null) body.put(KEY_ADDRESS, address);

                byte[] bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.getOutputStream().write(bodyBytes);

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
                JSONObject responseJson = new JSONObject(responseBody);
                if (responseCode == HttpURLConnection.HTTP_OK && responseJson.optBoolean("success", false)) {
                    // Actualizar preferencias locales solo si el backend responde éxito
                    SharedPreferences prefs = SecurePrefsHelper.getSessionPrefs(context);
                    SharedPreferences.Editor editor = prefs.edit();
                    if (email != null) editor.putString(KEY_EMAIL, email);
                    if (department != null) editor.putString(KEY_DEPARTMENT, department.name());
                    if (location != null) editor.putString(KEY_LOCATION, location);
                    if (address != null) editor.putString(KEY_ADDRESS, address);
                    editor.commit();
                    Log.i(TAG, "Datos de usuario actualizados en backend y local");
                    if (onSuccess != null) {
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(onSuccess);
                    }
                } else {
                    Log.e(TAG, "Error actualizando usuario: " + responseBody);
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error actualizando usuario", e);
            }
        }).start();
    }

    public static void clearUser(Context context) {
        SharedPreferences prefs = SecurePrefsHelper.getSessionPrefs(context);
    prefs.edit()
        .remove(KEY_EMAIL)
        .remove(KEY_FIRST_NAME)
        .remove(KEY_SECOND_NAME)
        .remove(KEY_FIRST_LAST_NAME)
        .remove(KEY_SECOND_LAST_NAME)
        .remove(KEY_DOCUMENT_TYPE)
        .remove(KEY_DOCUMENT_CODE)
        .remove(KEY_NATIONALITY)
        .remove(KEY_BIRTHDATE)
        .remove(KEY_DEPARTMENT)
        .remove(KEY_LOCATION)
        .remove(KEY_ADDRESS)
        .commit();
    }

    private static Date parseBirthdate(String birthdateStr, String format) {
        if (birthdateStr == null || birthdateStr.isEmpty()) return null;
        try {
            return new SimpleDateFormat(format, Locale.getDefault()).parse(birthdateStr);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date", e);
            return null;
        }
    }

    private static Department parseDepartment(String departmentStr) {
        if (departmentStr == null || departmentStr.isEmpty()) return null;
        try {
            // Normalizar a mayúsculas y reemplazar espacios con guiones bajos
            String normalized = departmentStr.trim().toUpperCase().replace(" ", "_");
            return Department.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid department", e);
            return null;
        }
    }

    // Excepción personalizada para errores de backend
    public static class BackendException extends Exception {
        public BackendException(String message) {
            super(message);
        }
    }
}
