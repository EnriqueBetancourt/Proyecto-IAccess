package com.example.iacccess;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistorialVisitasVisitante extends Fragment {

    private RecyclerView recyclerView;
    private HistorialVisitasAdapter visitasAdapter;
    private List<HistorialVisitas> historialVisitasList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public HistorialVisitasVisitante() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_historial_visitas_visitante, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.historialVisitante));
        }
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