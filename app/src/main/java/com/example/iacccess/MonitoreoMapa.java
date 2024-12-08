package com.example.iacccess;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MonitoreoMapa extends Fragment implements OnMapReadyCallback {

    private TextView txtIdVisitante, txtInformacion;  // Para mostrar el ID del visitante
    private MapView mapView;         // Mapa de Google Maps
    private GoogleMap googleMap;     // Objeto del mapa
    private FirebaseFirestore db;    // Conexión a Firestore
    private Marker visitanteMarker;  // Marcador en el mapa
    private String idDocumentoVisita;  // ID del documento de la visita recibido



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_monitoreo_mapa, container, false);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Vincular vistas
        txtIdVisitante = view.findViewById(R.id.txtPrueba);
        txtInformacion = view.findViewById(R.id.txtInformacion);
        mapView = view.findViewById(R.id.mapView);

        // Recuperar el ID del documento de la visita desde los argumentos
        Bundle args = getArguments();
        if (args != null) {
            idDocumentoVisita = args.getString("idVisita");  // Ahora se recibe el ID del documento de la visita
        } else {
            txtIdVisitante.setText("No se pasó el ID de la visita.");
            return view;
        }

        // Verificar si el ID de la visita se ha recibido correctamente
        if (idDocumentoVisita == null || idDocumentoVisita.isEmpty()) {
            Toast.makeText(getContext(), "ID de visita no recibido", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Configurar MapView y obtener el mapa cuando esté listo
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Cargar la información de la visita
        cargarInformacionVisita();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Cargar la ubicación de la visita usando el ID del documento
        cargarUbicacionVisita();
    }

    private void cargarUbicacionVisita() {
        try {
            if (idDocumentoVisita != null) {
                // Aquí cargamos la información de la visita desde Firestore
                db.collection("visitas").document(idDocumentoVisita)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            try {
                                if (documentSnapshot.exists()) {
                                    // Extraer la ubicación (por ejemplo, las coordenadas)
                                    Double lat = documentSnapshot.getDouble("latitud");
                                    Double lng = documentSnapshot.getDouble("longitud");

                                    // Validar que las coordenadas no sean nulas
                                    if (lat != null && lng != null) {
                                        // Mostrar marcador en el mapa
                                        LatLng visitaLatLng = new LatLng(lat, lng);
                                        visitanteMarker = googleMap.addMarker(new MarkerOptions()
                                                .position(visitaLatLng)
                                                .title("Visita: " + idDocumentoVisita));

                                        // Mover la cámara a la ubicación de la visita
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(visitaLatLng, 15));
                                    } else {
                                        Toast.makeText(getContext(), "Coordenadas no disponibles", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "No se encontró la ubicación de la visita", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Error al procesar la ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al cargar la ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error inesperado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarInformacionVisita() {
        // Recuperamos el documento de la visita usando el ID
        db.collection("visitas").document(idDocumentoVisita)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String idVisitante = documentSnapshot.getString("idVisitante");
                        String fechaEntrada = documentSnapshot.getString("fechaHoraEntrada");
                        String motivo = documentSnapshot.getString("motivo");

                        // Buscar al usuario correspondiente al idVisitante
                        if (idVisitante != null) {
                            db.collection("usuarios").document(idVisitante)
                                    .get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        if (userSnapshot.exists()) {
                                            String nombreVisitante = userSnapshot.getString("nombre");
                                            String apellidoVisitante = userSnapshot.getString("apellido");

                                            // Imprimir la información en el TextView
                                            String informacion = "Visitante: " + nombreVisitante + " " + apellidoVisitante +
                                                    "\nFecha de Entrada: " + fechaEntrada +
                                                    "\nMotivo: " + motivo;
                                            txtInformacion.setText(informacion);
                                        } else {
                                            txtInformacion.setText("No se encontró el visitante.");
                                        }
                                    })
                                    .addOnFailureListener(e -> txtInformacion.setText("Error al cargar el visitante"));
                        } else {
                            txtInformacion.setText("ID de visitante no encontrado.");
                        }
                    } else {
                        txtInformacion.setText("No se encontró la visita.");
                    }
                })
                .addOnFailureListener(e -> txtInformacion.setText("Error al cargar la visita"));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.monitorearMapa));
        }
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}