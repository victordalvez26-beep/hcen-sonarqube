package uy.edu.tse.hcen.notifications;

import static uy.edu.tse.hcen.config.AppConfig.PREFS_PUSH_NOTIFICATIONS;

import uy.edu.tse.hcen.config.AppConfig;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationStorage {
    private static final String KEY_LIST = "notifications_list";

    public static void saveNotification(Context ctx, String message) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_PUSH_NOTIFICATIONS, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, "[]");

        try {
            JSONArray array = new JSONArray(json);
            JSONObject obj = new JSONObject();
            obj.put("message", message);
            obj.put("timestamp", System.currentTimeMillis());
            array.put(obj);

            prefs.edit().putString(KEY_LIST, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<NotificationItem> getNotifications(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_PUSH_NOTIFICATIONS, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, "[]");
        List<NotificationItem> list = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String message = obj.getString("message");
                long timestamp = obj.getLong("timestamp");
                list.add(new NotificationItem(message, timestamp));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREFS_PUSH_NOTIFICATIONS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static void deleteNotification(Context ctx, long timestamp) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_PUSH_NOTIFICATIONS, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, "[]");
        try {
            JSONArray array = new JSONArray(json);
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                long ts = obj.getLong("timestamp");
                if (ts != timestamp) {
                    newArray.put(obj);
                }
            }
            prefs.edit().putString(KEY_LIST, newArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}