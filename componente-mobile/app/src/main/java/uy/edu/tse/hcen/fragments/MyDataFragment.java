package uy.edu.tse.hcen.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uy.edu.tse.hcen.R;
import uy.edu.tse.hcen.dialog.DialogType;
import uy.edu.tse.hcen.dialog.StatusDialogFragment;
import uy.edu.tse.hcen.manager.SessionManager;
import uy.edu.tse.hcen.manager.UserManager;
import uy.edu.tse.hcen.model.Department;
import uy.edu.tse.hcen.model.User;

public class MyDataFragment extends Fragment {

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtDocument;
    private TextView txtNationality;
    private TextView txtBirthdate;
    private TextView txtDepartament;
    private TextView txtLocation;
    private TextView txtAddress;
    private TextView txtInfo;

    private TextView labelEditEmail;
    private TextView labelEditDepartament;
    private TextView labelEditLocation;
    private TextView labelEditAddress;

    private EditText editEmail;
    private EditText editLocation;
    private EditText editAddress;

    private Spinner spinnerDepartament;
    private List<String> departmentNames;

    private Button btnEdit;
    private Button btnSave;

    StatusDialogFragment dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_data, container, false);

        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtDocument = view.findViewById(R.id.txtDocument);
        txtNationality = view.findViewById(R.id.txtNationality);
        txtBirthdate = view.findViewById(R.id.txtBirthdate);
        txtDepartament = view.findViewById(R.id.txtDepartament);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtAddress = view.findViewById(R.id.txtAddress);
        txtInfo = view.findViewById(R.id.txtInfo);

        labelEditEmail = view.findViewById(R.id.labelEditEmail);
        labelEditDepartament = view.findViewById(R.id.labelEditDepartament);
        labelEditLocation = view.findViewById(R.id.labelEditLocation);
        labelEditAddress = view.findViewById(R.id.labelEditAddress);

        editEmail = view.findViewById(R.id.editEmail);
        spinnerDepartament = view.findViewById(R.id.spinnerDepartament);
        editLocation = view.findViewById(R.id.editLocation);
        editAddress = view.findViewById(R.id.editAddress);

        btnEdit = view.findViewById(R.id.btnEdit);
        btnSave = view.findViewById(R.id.btnSave);

        departmentNames = new ArrayList<>();
        for (Department dep : Department.values()) {
            departmentNames.add(dep.getDisplayName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, departmentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartament.setAdapter(adapter);

        btnEdit.setOnClickListener(v -> toggleEditing(true));
        btnSave.setOnClickListener(v -> {
            saveUserData();
            toggleEditing(false);
        });

        dialog = StatusDialogFragment.newInstance(DialogType.LOADING, "Cargando tus datos");
        dialog.show(getParentFragmentManager(), "dialog");

        loadUserData();

        return view;
    }

    private void toggleEditing(boolean enable) {
        int show = enable ? View.VISIBLE : View.GONE;
        int hide = enable ? View.GONE : View.VISIBLE;

        txtEmail.setVisibility(hide); editEmail.setVisibility(show);
        txtBirthdate.setVisibility(View.VISIBLE); // siempre visible, no editable
        txtDepartament.setVisibility(hide); spinnerDepartament.setVisibility(show);
        txtLocation.setVisibility(hide); editLocation.setVisibility(show);
        txtAddress.setVisibility(hide); editAddress.setVisibility(show);
        txtInfo.setVisibility(show);

        labelEditEmail.setVisibility(enable ? View.VISIBLE : View.GONE);
        labelEditDepartament.setVisibility(enable ? View.VISIBLE : View.GONE);
        labelEditLocation.setVisibility(enable ? View.VISIBLE : View.GONE);
        labelEditAddress.setVisibility(enable ? View.VISIBLE : View.GONE);

        btnEdit.setVisibility(hide);
        btnSave.setVisibility(show);
    }

    private void loadUserData() {
        User user = UserManager.getUser(requireContext());
        dialog.dismiss();

        txtName.setText(Html.fromHtml("<b>Nombre:</b> " + user.getFullName()));
        txtEmail.setText(Html.fromHtml("<b>Email:</b> " + user.getEmail()));
        editEmail.setText(user.getEmail());

        String doc = user.getDocumentType() + " " + user.getDocumentCode();
        txtDocument.setText(Html.fromHtml("<b>Documento:</b> " + doc));

        txtNationality.setText(Html.fromHtml("<b>Nacionalidad:</b> " + user.getNationality()));

        if (user.getBirthdate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateStr = sdf.format(user.getBirthdate());
            txtBirthdate.setText(Html.fromHtml("<b>Fecha de nacimiento:</b> " + dateStr));
        } else {
            txtBirthdate.setText(Html.fromHtml("<b>Fecha de nacimiento:</b> Sin especificar"));
        }

        String dep = user.getDepartamento() != null ? user.getDepartamento().getDisplayName() : "-";
        txtDepartament.setText(Html.fromHtml("<b>Departamento:</b> " + dep));

        if (user.getDepartamento() != null) {
            int index = departmentNames.indexOf(user.getDepartamento().getDisplayName());
            if (index >= 0) {
                spinnerDepartament.setSelection(index);
            }
        }

        txtLocation.setText(Html.fromHtml("<b>Localidad:</b> " + (user.getLocation() != null ? user.getLocation() : "-")));
        editLocation.setText(user.getLocation());

        txtAddress.setText(Html.fromHtml("<b>Dirección:</b> " + (user.getAddress() != null ? user.getAddress() : "-")));
        editAddress.setText(user.getAddress());
    }

    private void saveUserData() {
        String email = editEmail.getText().toString();
        String selectedDisplayName = spinnerDepartament.getSelectedItem().toString();
        Department selectedDepartment = null;
        for (Department dep : Department.values()) {
            if (dep.getDisplayName().equalsIgnoreCase(selectedDisplayName)) {
                selectedDepartment = dep;
                break;
            }
        }
        String departmentStr = selectedDepartment != null ? selectedDepartment.name() : "";
        String location = editLocation.getText().toString();
        String address = editAddress.getText().toString();

        UserManager.saveUser(
            requireContext(),
            email,
            departmentStr,
            location,
            address,
            () -> {
                loadUserData();
                StatusDialogFragment loadingDialog = StatusDialogFragment.newInstance(DialogType.SUCCESS, "¡Datos guardados con éxito!");
                loadingDialog.show(getParentFragmentManager(), "loadingDialog");
            }
        );
    }
}