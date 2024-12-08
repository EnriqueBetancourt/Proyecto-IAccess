package com.example.iacccess;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistorialVisitasResidente#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistorialVisitasResidente extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private HistorialVisitasAdapter visitasAdapter;
    private List<HistorialVisitas> historialVisitasList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public HistorialVisitasResidente() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistorialVisitasResidente.
     */
    // TODO: Rename and change types and number of parameters
    public static HistorialVisitasResidente newInstance(String param1, String param2) {
        HistorialVisitasResidente fragment = new HistorialVisitasResidente();
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
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.historialResidente));
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_historial_visitas_residente, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewVisitas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Intentar cargar los datos del historial de visitas
        try {
            String userUID = mAuth.getCurrentUser().getUid();

            // Consultar las visitas del visitante o residente
            db.collection("historialVisitas").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HistorialVisitas visita = document.toObject(HistorialVisitas.class);

                        // Filtrar por visitante o residente
                        if (visita.getIdVisitante().equals(userUID) || visita.getIdResidente().equals(userUID)) {
                            historialVisitasList.add(visita);
                        }
                    }

                    // Comprobar si no hay datos
                    if (historialVisitasList.isEmpty()) {
                        Log.d("HistorialVisitas", "No hay historial de visitas para este usuario.");
                    }

                    // Actualizar el adapter
                    visitasAdapter = new HistorialVisitasAdapter(historialVisitasList, userUID);
                    recyclerView.setAdapter(visitasAdapter);

                } else {
                    // Error en la consulta
                    Log.e("Firestore Error", "Error getting documents: ", task.getException());
                }
            });
        } catch (Exception e) {
            // Mostrar cualquier excepción que ocurra en este fragmento
            Log.e("Fragment Error", "Error en onViewCreated: ", e);
        }
    }
}