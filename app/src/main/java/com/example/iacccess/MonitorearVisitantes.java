package com.example.iacccess;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MonitorearVisitantes extends Fragment implements VisitaAdapter.OnVisitaSelectedListener {

    private RecyclerView recyclerView;
    private VisitaAdapter visitaAdapter;
    private List<Visita> visitaList;
    private FirebaseFirestore db;

    private Button btnMonitorear, btnRegistrarSalida;
    private String idDocumentoSeleccionado;  // Variable para almacenar el ID del documento seleccionado

    public MonitorearVisitantes() {
    }

    public static MonitorearVisitantes newInstance(String param1, String param2) {
        MonitorearVisitantes fragment = new MonitorearVisitantes();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.MonitorearVisitante));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitorear_visitantes, container, false);
        return view;
    }

    private void cargarVisitas() {
        db.collection("visitas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    visitaList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String idResidente = document.getString("idResidente");
                        String idVisitante = document.getString("idVisitante");
                        String motivo = document.getString("motivo");
                        String fechaHoraEntrada = document.getString("fechaHoraEntrada");
                        String idDocumento = document.getId();
                        String idPortero = document.getString("idPortero");
                        double latitud = document.contains("latitud") ? document.getDouble("latitud") : 0.0;
                        double longitud = document.contains("longitud") ? document.getDouble("longitud") : 0.0;

                        // Crear objeto Visita
                        Visita visita = new Visita(idResidente, idVisitante, idPortero, motivo, fechaHoraEntrada, idDocumento, latitud, longitud);

                        // Obtener el nombre del visitante desde la colección de usuarios
                        db.collection("usuarios")
                                .document(idVisitante)
                                .get()
                                .addOnSuccessListener(visitanteSnapshot -> {
                                    if (visitanteSnapshot.exists()) {
                                        String nombre = visitanteSnapshot.getString("nombre");
                                        String apellido = visitanteSnapshot.getString("apellido");
                                        visita.setNombreVisitante(nombre + " " + apellido);  // Establecer nombre completo
                                    } else {
                                        visita.setNombreVisitante("Visitante desconocido");
                                    }

                                    visitaList.add(visita);

                                    // Solo notificar al adaptador cuando todas las visitas se han cargado
                                    if (visitaList.size() == queryDocumentSnapshots.size()) {
                                        visitaAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("MonitorearVisitantes", "Error al obtener nombre del visitante: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar visitas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnMonitorear = view.findViewById(R.id.btnMonitorear);
        btnRegistrarSalida = view.findViewById(R.id.btnRegistrarSalida);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        visitaList = new ArrayList<>();
        visitaAdapter = new VisitaAdapter(visitaList, this);  // Asegúrate de pasar this (el fragmento implementa la interfaz)
        recyclerView.setAdapter(visitaAdapter);

        db = FirebaseFirestore.getInstance();
        cargarVisitas();

        btnMonitorear.setOnClickListener(v -> {
            if (idDocumentoSeleccionado != null && !idDocumentoSeleccionado.isEmpty()) {
                Bundle args = new Bundle();
                args.putString("idVisita", idDocumentoSeleccionado);  // Pasar el ID del documento al siguiente fragmento
                Navigation.findNavController(v).navigate(R.id.monitoreoMapa, args);
            } else {
                Toast.makeText(getContext(), "Seleccione una visita", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegistrarSalida.setOnClickListener(v -> {
            if (idDocumentoSeleccionado != null && !idDocumentoSeleccionado.isEmpty()) {
                // Obtener los datos de la visita seleccionada
                db.collection("visitas")
                        .document(idDocumentoSeleccionado)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Obtener los datos del documento
                                String idResidente = documentSnapshot.getString("idResidente");
                                String idVisitante = documentSnapshot.getString("idVisitante");
                                String idPortero = documentSnapshot.getString("idPortero");
                                String motivo = documentSnapshot.getString("motivo");
                                String fechaHoraEntrada = documentSnapshot.getString("fechaHoraEntrada");

                                // Registrar la hora de salida actual
                                String fechaHoraSalida = obtenerHoraActual(); // Función para obtener la hora actual

                                // Consultar la información del portero para obtener el idFraccionamiento
                                db.collection("fraccionamientos")  // Buscar en 'fraccionamientos' usando idPortero
                                        .get()
                                        .addOnSuccessListener(fraccionamientos -> {
                                            String idFraccionamiento = null;

                                            for (QueryDocumentSnapshot fraccionamiento : fraccionamientos) {
                                                List<String> porteros = (List<String>) fraccionamiento.get("porteros");
                                                if (porteros != null && porteros.contains(idPortero)) {
                                                    idFraccionamiento = fraccionamiento.getId();
                                                    break; // Salir del bucle al encontrar el fraccionamiento
                                                }
                                            }

                                            if (idFraccionamiento != null) {
                                                // Crear un mapa con los datos
                                                HashMap<String, Object> historialData = new HashMap<>();
                                                historialData.put("idResidente", idResidente);
                                                historialData.put("idVisitante", idVisitante);
                                                historialData.put("idPortero", idPortero);
                                                historialData.put("motivo", motivo);
                                                historialData.put("fechaHoraEntrada", fechaHoraEntrada);
                                                historialData.put("fechaHoraSalida", fechaHoraSalida);
                                                historialData.put("idFraccionamiento", idFraccionamiento); // Agregar idFraccionamiento

                                                // Guardar los datos en la colección 'historialVisitas'
                                                db.collection("historialVisitas")
                                                        .add(historialData)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Eliminar el documento original de la colección 'visitas'
                                                            db.collection("visitas")
                                                                    .document(idDocumentoSeleccionado)
                                                                    .delete()
                                                                    .addOnSuccessListener(aVoid1 -> {
                                                                        Toast.makeText(getContext(), "Salida registrada y visita movida al historial.", Toast.LENGTH_SHORT).show();

                                                                        // Eliminar la visita de la lista directamente
                                                                        for (int i = 0; i < visitaList.size(); i++) {
                                                                            if (visitaList.get(i).getIdDocumento().equals(idDocumentoSeleccionado)) {
                                                                                visitaList.remove(i);
                                                                                visitaAdapter.notifyItemRemoved(i); // Notificar el cambio en el RecyclerView
                                                                                break;
                                                                            }
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(getContext(), "Error al eliminar la visita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    });
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(getContext(), "Error al guardar en el historial: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                Toast.makeText(getContext(), "No se encontró el fraccionamiento para este portero.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Error al obtener los fraccionamientos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(getContext(), "Documento no encontrado.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al obtener los datos de la visita: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getContext(), "Seleccione una visita para registrar la salida.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onVisitaSelected(String idDocumentoVisita) {
        idDocumentoSeleccionado = idDocumentoVisita;  // Guardar el ID del documento seleccionado
        Toast.makeText(getContext(), "ID Seleccionado: " + idDocumentoVisita, Toast.LENGTH_SHORT).show();  // Mensaje para verificar
    }

    private String obtenerHoraActual() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

}
