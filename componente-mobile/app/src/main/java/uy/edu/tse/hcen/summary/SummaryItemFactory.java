package uy.edu.tse.hcen.summary;

import org.json.JSONObject;

import uy.edu.tse.hcen.R;

public class SummaryItemFactory {

    public static SummaryItem fromCategory(String category, JSONObject obj) {
        String title = mapCategoryToTitle(category);
        String description = extractDescription(category, obj);
        int icon = mapCategoryToIcon(category);
        return new SummaryItem(title, description, icon);
    }

    private static String mapCategoryToTitle(String category) {
        switch (category) {
            case "allergies": return "Alergia";
            case "conditions": return "Diagnóstico";
            case "medications": return "Medicación";
            case "immunizations": return "Vacuna";
            case "observations": return "Observación";
            case "procedures": return "Procedimiento";
            default: return category;
        }
    }

    private static int mapCategoryToIcon(String category) {
        switch (category) {
            case "allergies": return R.drawable.ic_allergy;
            case "conditions": return R.drawable.ic_condition;
            case "medications": return R.drawable.ic_medication;
            case "immunizations": return R.drawable.ic_vaccine;
            case "observations": return R.drawable.ic_observation;
            case "procedures": return R.drawable.ic_procedure;
            default: return R.drawable.ic_fhir_generic;
        }
    }

    private static String extractDescription(String category, JSONObject obj) {
        try {
            switch (category) {
                case "allergies":
                    return obj.optString("name", "Alergia") + " (" + obj.optString("status", "") + ")";
                case "conditions":
                    return obj.optString("name", "Condición") + " (" + obj.optString("status", "") + ")";
                case "medications":
                    return obj.optString("name", "Medicamento") + " - " + obj.optString("dose", "");
                case "immunizations":
                    return obj.optString("name", "Vacuna") + " (" + obj.optString("date", "") + ")";
                case "observations":
                    return obj.optString("type", "Observación") + ": " + obj.optString("value", "") +
                            " (" + obj.optString("date", "") + ")";
                case "procedures":
                    return obj.optString("name", "Procedimiento") + " (" + obj.optString("date", "") + ")";
                default:
                    return "Información no disponible";
            }
        } catch (Exception e) {
            return "Información no disponible";
        }
    }
}
