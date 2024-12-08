package com.example.iacccess;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SolicitarAcceso extends Fragment {

    private EditText editTextNombre, editTextMotivo;
    private Button btnSolicitarAcceso, btnCompartir;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public SolicitarAcceso() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_solicitar_acceso, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.solicitarAcceso));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Referencias a los elementos de la interfaz
        editTextNombre = view.findViewById(R.id.txtNombreVisitante);
        editTextMotivo = view.findViewById(R.id.txtMotivo);
        btnSolicitarAcceso = view.findViewById(R.id.btnSolicitarAcceso);
        btnCompartir = view.findViewById(R.id.btnCompartir);

        btnSolicitarAcceso.setOnClickListener(v -> guardarSolicitud());
        btnCompartir.setOnClickListener(v -> generarYCompartirQR());
    }

    private void guardarSolicitud() {
        String nombre = editTextNombre.getText().toString().trim();
        String motivo = editTextMotivo.getText().toString().trim();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "Usuario desconocido";

        if (nombre.isEmpty() || motivo.isEmpty()) {
            Toast.makeText(getContext(), "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un mapa de datos
        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("nombre", nombre);
        solicitud.put("motivo", motivo);
        solicitud.put("idResidente", userId);
        solicitud.put("timestamp", System.currentTimeMillis());

        // Guardar en Firestore
        db.collection("solicitudes")
                .add(solicitud)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getContext(), "Solicitud enviada", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al enviar solicitud: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        editTextMotivo.setText("");
        editTextNombre.setText("");
    }

    private void generarYCompartirQR() {
        String nombre = editTextNombre.getText().toString().trim();
        String motivo = editTextMotivo.getText().toString().trim();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "Usuario desconocido";
        long timestamp = System.currentTimeMillis();

        if (nombre.isEmpty() || motivo.isEmpty()) {
            Toast.makeText(getContext(), "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Datos para el QR
        String qrData = "Nombre: " + nombre + "\nMotivo: " + motivo + "\nID Residente: " + userId + "\nTimestamp: " + timestamp;

        try {
            // Generar QR
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);

            // Guardar QR temporalmente
            File qrFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "solicitud_qr.png");
            FileOutputStream fos = new FileOutputStream(qrFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            // Crear URI para compartir
            Uri qrUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", qrFile);

            // Abrir intent para compartir
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, qrUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Aquí está el QR para la solicitud de acceso.");
            startActivity(Intent.createChooser(shareIntent, "Compartir QR mediante"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al generar QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
