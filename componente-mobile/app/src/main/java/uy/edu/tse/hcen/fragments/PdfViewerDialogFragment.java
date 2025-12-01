package uy.edu.tse.hcen.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import uy.edu.tse.hcen.R;

public class PdfViewerDialogFragment extends DialogFragment {

    private static Bitmap pdfBitmap;

    public static PdfViewerDialogFragment newInstance(Bitmap bitmap) {
        pdfBitmap = bitmap;
        return new PdfViewerDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_pdf_viewer, container, false);
        ImageView imagePdf = view.findViewById(R.id.imagePdf);
        imagePdf.setImageBitmap(pdfBitmap);
        return view;
    }
}