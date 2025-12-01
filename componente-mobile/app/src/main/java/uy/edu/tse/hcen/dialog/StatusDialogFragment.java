package uy.edu.tse.hcen.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import uy.edu.tse.hcen.R;

public class StatusDialogFragment extends DialogFragment {

    private static final String ARG_TYPE = "dialog_type";
    private static final String ARG_MESSAGE = "dialog_message";
    private Runnable onDismissAction;

    public static StatusDialogFragment newInstance(DialogType type, String message) {
        StatusDialogFragment fragment = new StatusDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TYPE, type);
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        fragment.setCancelable(type != DialogType.LOADING);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_status, container, false);

        ImageView icon = view.findViewById(R.id.dialogIcon);
        ProgressBar progress = view.findViewById(R.id.dialogProgress);
        TextView messageText = view.findViewById(R.id.dialogMessage);

        DialogType type = (DialogType) getArguments().getSerializable(ARG_TYPE);

        String baseMessage = getArguments().getString(ARG_MESSAGE);
        messageText.setText(baseMessage);

        switch (type) {
            case SUCCESS:
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.ic_success);
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.success));
                break;
            case ERROR:
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.ic_error);
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.error));
                break;
            case INFO:
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.ic_error);
                break;
            case WARNING:
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.ic_warning);
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.warning));
                break;
            case LOADING:
                progress.setVisibility(View.VISIBLE);
                final Handler handler = new Handler(Looper.getMainLooper());
                final int[] dotCounts = {0, 1, 2, 3};
                final int delay = 500;
                final Runnable dotAnimator = new Runnable() {
                    int index = 0;

                    @Override
                    public void run() {
                        if (!isAdded()) return;
                        String dots = new String(new char[dotCounts[index]]).replace("\0", ".");
                        messageText.setText(baseMessage + dots);
                        index = (index + 1) % dotCounts.length;
                        handler.postDelayed(this, delay);
                    }
                };
                handler.post(dotAnimator);
                break;
        }

        if (type == DialogType.SUCCESS || type == DialogType.ERROR) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) dismiss();
            }, 5000);
        }

        return view;
    }

    @Override
    public void onStart() {
    super.onStart();
    if (getDialog() != null && getDialog().getWindow() != null) {
        int width = (int) (360 * getResources().getDisplayMetrics().density); // 340dp
        int height = (int) (200 * getResources().getDisplayMetrics().density); // 200dp
        getDialog().getWindow().setLayout(width, height);
        getDialog().getWindow().getDecorView().startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
    }
    }

    public void setOnDismissAction(Runnable action) {
        this.onDismissAction = action;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissAction != null) {
            onDismissAction.run();
        }
    }

}