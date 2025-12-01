package uy.edu.tse.hcen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import uy.edu.tse.hcen.R;

public class HelpFragment extends Fragment {

    private LinearLayout containerFaq;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        containerFaq = view.findViewById(R.id.containerFaq);

        addFaqItem("¿Qué es la Historia Clínica Electrónica Nacional (HCEN)?",
                "HCEN es un sistema que permite integrar y compartir de forma segura la información de salud de los usuarios entre los prestadores públicos y privados del Sistema Nacional Integrado de Salud (SNIS) en todo el país.");
        addFaqItem("¿Quiénes pueden acceder a mi historia clínica?",
                "Solo los profesionales de la salud que te estén atendiendo y cuenten con tu consentimiento pueden acceder a tu información. Todo acceso queda registrado y auditado.");
        addFaqItem("¿Cómo doy o retiro mi consentimiento para compartir mi historia clínica?",
                "Podés otorgar o revocar tu consentimiento en cualquier momento a través del Portal Web, o presencialmente en tu prestador de salud.");
        addFaqItem("¿Qué tipo de información se guarda en la HCEN?",
                "Se almacenan datos clínicos relevantes como diagnósticos, medicaciones, alergias, resultados de estudios, antecedentes y otros registros vinculados a tu atención médica.");
        addFaqItem("¿Mi información está segura?",
                "Sí. La HCEN cumple con las normas nacionales de protección de datos personales y con estándares internacionales de seguridad y confidencialidad.");
        addFaqItem("¿Puedo ver mi historia clínica?",
                "Sí. Podés acceder a tu historia clínica a través del menú principal de la aplicación o en nuestro portal web.");
        addFaqItem("¿Qué pasa si cambio de prestador de salud?",
                "Tu información se mantiene disponible dentro del sistema nacional, por lo que tu nuevo prestador podrá acceder a tus datos clínicos (si das consentimiento), sin que tengas que empezar desde cero.");
        addFaqItem("¿Qué hago si encuentro un error en mi historia clínica?",
                "Debés comunicarte con tu prestador de salud para solicitar la corrección. Solo los profesionales autorizados pueden modificar la información registrada.");
        addFaqItem("¿La HCEN reemplaza a la historia clínica de mi prestador?",
                "No. Cada institución mantiene su propia historia clínica interna, pero la HCEN permite que se compartan los datos esenciales de forma estandarizada y segura entre todos los prestadores.");
        addFaqItem("¿Qué leyes regulan la HCEN?",
                "La HCEN está regulada por la Ley N.º 18.335 (Derechos y Obligaciones de los Pacientes), la Ley N.º 18.331 (Protección de Datos Personales) y los decretos del Ministerio de Salud Pública (MSP) relacionados con la interoperabilidad del SNIS.");
        addFaqItem("¿Puedo saber quién accedió a mi información?",
                "Sí. El sistema mantiene un registro de todos los accesos, y podés solicitar un informe de auditoría a través de tu prestador.");
        addFaqItem("¿La HCEN funciona si no tengo conexión a Internet?",
                "No directamente. La información se almacena en servidores seguros y requiere conexión para sincronizar o consultar los datos actualizados.");
        addFaqItem("¿Qué pasa si no quiero participar en la HCEN?",
                "Tenés derecho a no otorgar consentimiento, pero esto puede limitar la posibilidad de que tus profesionales de salud accedan a información importante para tu atención.");
        addFaqItem("¿La aplicación guarda mis datos personales?",
                "La aplicación no almacena localmente tu historia clínica completa. Solo accede a la información del sistema de forma segura y temporal, mediante tu autenticación.");
        addFaqItem("¿A quién puedo contactar si tengo dudas o problemas?",
                "Podés comunicarte con el Ministerio de Salud Pública (MSP) a través del sitio oficial o con el área de atención al usuario de tu prestador de salud.");

        return view;
    }

    private void addFaqItem(String question, String answer) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View item = inflater.inflate(R.layout.item_faq_expandable, containerFaq, false);

        TextView txtQuestion = item.findViewById(R.id.txtQuestion);
        TextView txtAnswer = item.findViewById(R.id.txtAnswer);
        ImageView imgArrow = item.findViewById(R.id.imgArrow);
        LinearLayout header = item.findViewById(R.id.headerLayout);

        txtQuestion.setText(question);
        txtAnswer.setText(answer);

        header.setOnClickListener(v -> {
            if (txtAnswer.getVisibility() == View.GONE) {
                txtAnswer.setVisibility(View.VISIBLE);
                imgArrow.setImageResource(R.drawable.ic_arrow_up);
            } else {
                txtAnswer.setVisibility(View.GONE);
                imgArrow.setImageResource(R.drawable.ic_arrow_down);
            }
        });

        containerFaq.addView(item);
    }
}
