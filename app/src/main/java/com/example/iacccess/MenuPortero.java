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
import com.google.firebase.firestore.QuerySnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenuPortero#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuPortero extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Button btnRegistrarAcceso, btnMonitorearVisitante;
    TextView labelFraccionamiento;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public MenuPortero() {
        // Required empty public constructor
    }

    public static MenuPortero newInstance(String param1, String param2) {
        MenuPortero fragment = new MenuPortero();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(getString(R.string.Galería));
            }
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu_portero, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.Galería));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NavController navController = Navigation.findNavController(view);

        btnMonitorearVisitante = view.findViewById(R.id.btnAccesoRegistros);
        btnRegistrarAcceso = view.findViewById(R.id.btnProcesarVista);
        labelFraccionamiento = view.findViewById(R.id.labelFraccionamiento);

        btnRegistrarAcceso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.monitorearVisitantes);
            }
        });

        btnMonitorearVisitante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.registrarAcceso);
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
                    // Obtener el arreglo de 'porteros' de cada documento
                    Object porterosObj = document.get("porteros");
                    if (porterosObj instanceof java.util.List) {
                        java.util.List<String> porteros = (java.util.List<String>) porterosObj;
                        // Verificar si el UID está en el arreglo de porteros
                        if (porteros.contains(userUID)) {
                            // Si es portero, mostrar el nombre del fraccionamiento en el TextView
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
