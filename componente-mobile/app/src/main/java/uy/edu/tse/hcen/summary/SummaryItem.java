package uy.edu.tse.hcen.summary;

public class SummaryItem {
    public final String title;
    public final String description;
    public final int iconResId;

    public SummaryItem(String title, String description, int iconResId) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
    }
}
