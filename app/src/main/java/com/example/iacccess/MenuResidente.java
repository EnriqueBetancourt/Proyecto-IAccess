package com.example.iacccess;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenuResidente#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuResidente extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button btnSoliciarAcceso, btnAbrirPuerta, btnVerHistorial;
    private TextView labelFraccionamiento;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MenuResidente() {
        // Required empty public constructor
    }

    public static MenuResidente newInstance(String param1, String param2) {
        MenuResidente fragment = new MenuResidente();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.Presentación));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu_residente, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSoliciarAcceso = view.findViewById(R.id.botonSolicitarAcceso);
        btnAbrirPuerta = view.findViewById(R.id.botonAbrirPuerta);
        btnVerHistorial = view.findViewById(R.id.botonVerHistorial);
        labelFraccionamiento = view.findViewById(R.id.labelFraccionamiento);

        final NavController navController = Navigation.findNavController(view);

        // Navegar a la pantalla para solicitar acceso
        btnSoliciarAcceso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.solicitar_acceso);
            }
        });

        // Navegar a la pantalla para abrir la puerta
        btnAbrirPuerta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.abrir_puerta);
            }
        });

        // Deshabilitado por ahora: Navegar al historial de visitas

        btnVerHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.historialVisitasResidente);
            }
        });


        // Obtener UID del usuario actual
        String userUID = mAuth.getCurrentUser().getUid();

        // Consultar los documentos de la colección 'fraccionamientos'
        CollectionReference fraccionamientosRef = db.collection("fraccionamientos");
        fraccionamientosRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Recorrer todos los documentos de la colección
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Obtener los datos del documento
                    String nombreFraccionamiento = document.getString("nombre");
                    String direccion = document.getString("direccion");
                    // Obtener el arreglo de 'residentes' de cada documento
                    Object residentesObj = document.get("residentes");
                    if (residentesObj instanceof java.util.List) {
                        java.util.List<String> residentes = (java.util.List<String>) residentesObj;
                        // Verificar si el UID está en el arreglo de residentes
                        if (residentes.contains(userUID)) {
                            // Si es residente, mostrar el nombre del fraccionamiento en el TextView
                            labelFraccionamiento.setText(nombreFraccionamiento);
                            break; // No es necesario seguir buscando
                        }
                    }
                }
            } else {
                // Manejar el error si la consulta falla
                labelFraccionamiento.setText("Error al cargar fraccionamiento.");
            }
        });
    }
}
