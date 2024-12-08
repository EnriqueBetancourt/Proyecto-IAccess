package com.example.iacccess;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistrarAcceso extends Fragment {

    private RecyclerView recyclerView;
    private SolicitudAdapter adapter;
    private List<Solicitud> solicitudList;
    private FirebaseFirestore db;
    private TextView txtInformacion;
    private Button btnLlamar, btnRegistrarVisita;
    private String idUsuarioEscaneado;
    private String idPortero;  // Esta será la variable que obtiene el id del portero directamente de Firebase

    private FusedLocationProviderClient fusedLocationClient;

    public RegistrarAcceso() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registrar_acceso, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.RegistrarAcceso));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configuración de RecyclerView y sus elementos
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        solicitudList = new ArrayList<>();
        adapter = new SolicitudAdapter(this, solicitudList);
        recyclerView.setAdapter(adapter);

        Button btnEscanearQR = view.findViewById(R.id.btnRegistrarSalida);
        txtInformacion = view.findViewById(R.id.txtInfo);
        btnLlamar = view.findViewById(R.id.btnLlamada);
        btnRegistrarVisita = view.findViewById(R.id.btnRegistrarVisita);

        btnEscanearQR.setOnClickListener(v -> iniciarEscaneoQR());
        btnLlamar.setOnClickListener(v -> realizarLlamada());
        btnRegistrarVisita.setOnClickListener(v -> registrarVisita());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        cargarSolicitudes();

        // Obtener el ID del portero directamente desde Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            idPortero = user.getUid();  // Obtener el ID del portero directamente
            txtInformacion.setText("ID del portero: " + idPortero);  // Muestra el ID en el TextView
        } else {
            txtInformacion.setText("No hay usuario autenticado");
        }
    }

    private void cargarSolicitudes() {
        db = FirebaseFirestore.getInstance();
        db.collection("solicitudes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    solicitudList.clear(); // Limpiar lista antes de cargar
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String idResidente = document.getString("idResidente");
                        String nombre = document.getString("nombre");
                        String motivo = document.getString("motivo");
                        long timestamp = document.getLong("timestamp");
                        String documentId = document.getId(); // Obtener ID del documento

                        Solicitud solicitud = new Solicitud(idResidente, nombre, motivo, timestamp, documentId);
                        solicitudList.add(solicitud);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void realizarLlamada() {
        // Verificar si hay una solicitud seleccionada
        List<Solicitud> seleccionados = adapter.getSelectedList();
        if (seleccionados.isEmpty()) {
            Toast.makeText(getContext(), "Selecciona una solicitud para realizar la llamada", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tomar la primera solicitud seleccionada
        Solicitud solicitudSeleccionada = seleccionados.get(0);
        String idResidente = solicitudSeleccionada.getIdResidente();

        // Consultar Firestore para obtener el número de celular del residente
        db.collection("usuarios").document(idResidente)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String celularResidente = documentSnapshot.getString("celular");

                        if (celularResidente != null && !celularResidente.isEmpty()) {
                            // Realizar la llamada
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + celularResidente));
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), "El residente no tiene un número registrado.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Residente no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al obtener datos del residente: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void iniciarEscaneoQR() {
        IntentIntegrator intentIntegrator = IntentIntegrator.forSupportFragment(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.setPrompt("Escanea un código QR");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                idUsuarioEscaneado = result.getContents();
                obtenerDatosUsuario(idUsuarioEscaneado);
            } else {
                Toast.makeText(getContext(), "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registrarVisita() {
        // Verificar si el idPortero es null o vacío
        if (idPortero == null || idPortero.isEmpty()) {
            Toast.makeText(getContext(), "El ID del portero no está disponible. Asegúrate de estar logueado correctamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (location != null) {
                double latitud = location.getLatitude();
                double longitud = location.getLongitude();

                List<Solicitud> seleccionados = adapter.getSelectedList();
                if (seleccionados.isEmpty() || idUsuarioEscaneado == null || idUsuarioEscaneado.isEmpty()) {
                    Toast.makeText(getContext(), "Selecciona una solicitud y escanea un código QR", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Solicitud solicitud : seleccionados) {
                    FirebaseUser userPortero = FirebaseAuth.getInstance().getCurrentUser();
                    String idResidente = solicitud.getIdResidente();
                    String motivo = solicitud.getMotivo();
                    String fechaHoraEntrada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    // Registrar la visita y obtener el idDocumento generado
                    db.collection("visitas").add(new Visita(
                                    idResidente,
                                    idUsuarioEscaneado,
                                    user.getUid(),
                                    motivo,
                                    fechaHoraEntrada,
                                    user.getUid(),
                                    latitud,
                                    longitud
                            ))
                            .addOnSuccessListener(documentReference -> {
                                String idDocumento = documentReference.getId();  // Obtener el ID del documento creado
                                // Ahora actualizamos la visita con el idDocumento
                                Visita visitaConId = new Visita(
                                        idResidente,
                                        idUsuarioEscaneado,
                                        user.getUid(),
                                        motivo,
                                        fechaHoraEntrada,
                                        idDocumento,  // Asignar el idDocumento aquí
                                        latitud,
                                        longitud
                                );

                                // Actualizar la visita en Firestore
                                db.collection("visitas").document(idDocumento).set(visitaConId)
                                        .addOnSuccessListener(aVoid -> {
                                            db.collection("solicitudes").document(solicitud.getDocumentId())
                                                    .delete()
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        adapter.eliminarSolicitud(solicitud);
                                                        Toast.makeText(getContext(), "Visita registrada y solicitud eliminada", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al eliminar solicitud: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar la visita: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al registrar la visita: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(getContext(), "No se pudo obtener la ubicación actual.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void obtenerDatosUsuario(String uid) {
        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String apellido = documentSnapshot.getString("apellido");
                        String celularResidente = documentSnapshot.getString("celular");

                        if (nombre != null && celularResidente != null) {
                            String nombreCompleto = nombre + " " + apellido;
                            txtInformacion.setText("Nombre del visitante: " + nombreCompleto + "\nCelular: " + celularResidente);
                        } else {
                            txtInformacion.setText("Información incompleta del residente.");
                        }
                    } else {
                        Toast.makeText(getContext(), "Residente no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al obtener datos del residente: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void setCelularLlamada(String celular) {
        txtInformacion.setText("Número de celular: " + celular);
    }
}
