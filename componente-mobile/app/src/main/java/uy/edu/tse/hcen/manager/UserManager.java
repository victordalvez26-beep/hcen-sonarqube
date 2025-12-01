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

    // Método para consumir datos del backend y actualizar SharedPreferences
    private static void fetchUserDataFromBackend(Context context, String jwt) throws Exception {
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

                editor.putString("email", payload.optString("email"));
                editor.putString("firstName", payload.optString("primer_nombre"));
                editor.putString("secondName", payload.optString("segundo_nombre"));
                editor.putString("firstLastName", payload.optString("primer_apellido"));
                editor.putString("secondLastName", payload.optString("segundo_apellido"));
                editor.putString("documentType", payload.optString("tipo_documento"));
                editor.putString("documentCode", payload.optString("codigo_documento"));
                editor.putString("nationality", payload.optString("nacionalidad"));
                editor.putString("location", payload.optString("localidad"));
                editor.putString("address", payload.optString("direccion"));

                Date birthdate = parseBirthdate(payload.optString("fecha_nacimiento"), "yyyy-MM-dd");
                if (birthdate != null) {
                    editor.putLong("birthdate", birthdate.getTime());
                }

                Department department = parseDepartment(payload.optString("departamento"));
                if (department != null) {
                    editor.putString("department", department.name());
                }

                editor.commit();
                Log.i("UserManager", "Datos de usuario actualizados desde el backend");
            } else {
                throw new Exception("Backend error. Codigo: " + responseCode);
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
                Log.e("UserManager", "Error creando usuario", e);
            }
        }).start();
    }

    public static User getUser(Context context) {
        new Thread(() -> {
            try {
                String jwt = SessionManager.getJwtSession(context);
                fetchUserDataFromBackend(context, jwt);
            } catch (Exception e) {
                Log.e("UserManager", "Error devolviendo usuario", e);
            }
        }).start();

        // Devolver el usuario guardado en SharedPreferences
        return buildUserFromPrefs(context);
    }


    // Construye el objeto User desde SharedPreferences
    private static User buildUserFromPrefs(Context context) {
        SharedPreferences prefs = SecurePrefsHelper.getSessionPrefs(context);

        String email = prefs.getString("email", null);
        String firstName = prefs.getString("firstName", null);
        String secondName = prefs.getString("secondName", null);
        String firstLastName = prefs.getString("firstLastName", null);
        String secondLastName = prefs.getString("secondLastName", null);
        String documentType = prefs.getString("documentType", null);
        String documentCode = prefs.getString("documentCode", null);
        String nationality = prefs.getString("nationality", null);

        long fechaMillis = prefs.getLong("birthdate", -1);
        Date birthdate = fechaMillis > 0 ? new Date(fechaMillis) : null;

        String depString = prefs.getString("department", null);
        Department department = parseDepartment(depString);

        String location = prefs.getString("location", null);
        String address = prefs.getString("address", null);

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
                if (email != null) body.put("email", email);
                if (department != null) body.put("departamento", department.name());
                if (location != null) body.put("localidad", location);
                if (address != null) body.put("direccion", address);

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
                    if (email != null) editor.putString("email", email);
                    if (department != null) editor.putString("department", department.name());
                    if (location != null) editor.putString("location", location);
                    if (address != null) editor.putString("address", address);
                    editor.commit();
                    Log.i("UserManager", "Datos de usuario actualizados en backend y local");
                    if (onSuccess != null) {
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(onSuccess);
                    }
                } else {
                    Log.e("UserManager", "Error actualizando usuario: " + responseBody);
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e("UserManager", "Error actualizando usuario", e);
            }
        }).start();
    }

    public static void clearUser(Context context) {
        SharedPreferences prefs = SecurePrefsHelper.getSessionPrefs(context);
        prefs.edit()
                .remove("email")
                .remove("firstName")
                .remove("secondName")
                .remove("firstLastName")
                .remove("secondLastName")
                .remove("documentType")
                .remove("documentCode")
                .remove("nationality")
                .remove("birthdate")
                .remove("department")
                .remove("location")
                .remove("address")
                .commit();
    }

    private static Date parseBirthdate(String birthdateStr, String format) {
        if (birthdateStr == null || birthdateStr.isEmpty()) return null;
        try {
            return new SimpleDateFormat(format, Locale.getDefault()).parse(birthdateStr);
        } catch (ParseException e) {
            Log.e("UserManager", "Error parsing date", e);
            return null;
        }
    }

    private static Department parseDepartment(String departmentStr) {
        if (departmentStr == null || departmentStr.isEmpty()) return null;
        try {
            return Department.valueOf(departmentStr);
        } catch (IllegalArgumentException e) {
            Log.e("UserManager", "Invalid department", e);
            return null;
        }
    }
}
