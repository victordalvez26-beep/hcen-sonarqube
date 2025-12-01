package uy.edu.tse.hcen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.docs.ClinicalDocument;

public class ClinicalDocumentsAdapter extends RecyclerView.Adapter<ClinicalDocumentsAdapter.DocViewHolder> {

    private List<ClinicalDocument> documents;
    private OnDocumentClickListener listener;

    public interface OnDocumentClickListener {
        void onDocumentClick(ClinicalDocument doc);
    }

    public ClinicalDocumentsAdapter(List<ClinicalDocument> documents, OnDocumentClickListener listener) {
        this.documents = documents;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clinical_document, parent, false);
        return new DocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocViewHolder holder, int position) {
        ClinicalDocument doc = documents.get(position);
        holder.bind(doc, listener);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {
        TextView type;
        TextView date;
        TextView clinicProfessional;
        TextView description;
        CardView card;

        public DocViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardDocument);
            type = itemView.findViewById(R.id.textType);
            date = itemView.findViewById(R.id.textDate);
            clinicProfessional = itemView.findViewById(R.id.textClinicProfessional);
            description = itemView.findViewById(R.id.textDescription);
        }

        public void bind(ClinicalDocument doc, OnDocumentClickListener listener) {
            type.setText(doc.documentType);
            date.setText(doc.creationDate);
            clinicProfessional.setText(doc.originClinic + " | " + doc.professional);
            description.setText(doc.description);

            card.setOnClickListener(v -> listener.onDocumentClick(doc));
        }
    }
}
