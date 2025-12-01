package uy.edu.tse.hcen.notifications;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationItem {
    private final String message;
    private final long timestamp; //milisegundos

    public NotificationItem(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }

    public long getTimestamp() { return timestamp; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
