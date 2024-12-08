package com.example.iacccess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Registrarse extends AppCompatActivity {

    Button btnRegistrarse;
    ImageButton btnRegresar;
    EditText txtCorreo, txtContrasenia, txtCelular, txtApellido, txtNombre;
    CheckBox cbVerContrasenia;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    StorageReference storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        txtContrasenia = findViewById(R.id.txtContrasenia);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtNombre = findViewById(R.id.txtNombre);
        txtApellido = findViewById(R.id.txtApellido);
        txtCelular = findViewById(R.id.txtCelular);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        cbVerContrasenia = findViewById(R.id.cbVerContrasenia);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo, contrasenia, nombre, apellido, celular;
                correo = String.valueOf(txtCorreo.getText());
                contrasenia = String.valueOf(txtContrasenia.getText());
                nombre = String.valueOf(txtNombre.getText());
                apellido = String.valueOf(txtApellido.getText());
                celular = String.valueOf(txtCelular.getText());
                if (TextUtils.isEmpty(correo)) {
                    Toast.makeText(Registrarse.this, "Ingresa tu correo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(contrasenia)) {
                    Toast.makeText(Registrarse.this, "Ingresa tu contrasenia", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(correo, contrasenia)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Usuario autenticado con éxito
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    String userId = user.getUid();
                                    String fotoDefecto = "https://firebasestorage.googleapis.com/v0/b/i-access-7d628.firebasestorage.app/o/default%2Fuser.png?alt=media&token=02909469-94a1-4b7f-b624-068b325cad88";

                                    Map<String, Object> userInfo = new HashMap<>();
                                    userInfo.put("nombre", nombre);
                                    userInfo.put("apellido", apellido);
                                    userInfo.put("celular", celular);
                                    userInfo.put("correo", correo);
                                    userInfo.put("curp", null);
                                    userInfo.put("fotoINE", null);
                                    userInfo.put("fotoPerfil", fotoDefecto);

                                    // Agregar roles iniciales como nulos
                                    Map<String, Object> roles = new HashMap<>();
                                    roles.put("residente", null);
                                    roles.put("portero", null);
                                    userInfo.put("roles", roles);

                                    // Generar QR con el userId
                                    Bitmap qrCodeBitmap = generarQRCode(userId);

                                    if (qrCodeBitmap != null) {
                                        // Convertir el QR a bytes para subirlo a Firebase Storage
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                        byte[] qrData = baos.toByteArray();

                                        // Subir QR a Firebase Storage
                                        StorageReference qrRef = FirebaseStorage.getInstance().getReference().child("usuarios/" + userId + "/qrCode.png");

                                        qrRef.putBytes(qrData)
                                                .addOnSuccessListener(taskSnapshot -> {
                                                    // Obtener la URL del QR subido
                                                    qrRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                        String qrUrl = uri.toString();
                                                        userInfo.put("codigoQR", qrUrl); // Agregar la URL del QR al mapa

                                                        // Guardar la información en Firestore
                                                        db.collection("usuarios").document(userId).set(userInfo)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Toast.makeText(Registrarse.this, "Usuario registrado con éxito.", Toast.LENGTH_SHORT).show();
                                                                    // Redirigir al menú principal
                                                                    Intent intent = new Intent(Registrarse.this, MenuPrincipal.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(Registrarse.this, "Error al guardar los datos adicionales.", Toast.LENGTH_SHORT).show();
                                                                });
                                                    });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(Registrarse.this, "Error al subir el QR a Firebase Storage.", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(Registrarse.this, "Error al generar el QR.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                // Fallo al registrar al usuario
                                Toast.makeText(Registrarse.this, "Error al registrar el usuario.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Registrarse.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        cbVerContrasenia.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txtContrasenia.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                txtContrasenia.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            txtContrasenia.setSelection(txtContrasenia.getText().length());
        });
    }

    private Bitmap generarQRCode(String contenido) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(contenido, BarcodeFormat.QR_CODE, 400, 400);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}