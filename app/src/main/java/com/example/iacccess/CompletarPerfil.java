package com.example.iacccess;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CompletarPerfil#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompletarPerfil extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView fotoImageView;
    private Uri imageUri,ineImageUri;
    EditText txtCurp;
    Button btnCambiarFoto, btnSeleccionarIne, btnCompletarPerfil;
    TextView labelNombreImagen;



    public CompletarPerfil() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CompletarPerfil.
     */
    // TODO: Rename and change types and number of parameters
    public static CompletarPerfil newInstance(String param1, String param2) {
        CompletarPerfil fragment = new CompletarPerfil();
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
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.labelCompletarPerfil));
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_completar_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto);
        fotoImageView = view.findViewById(R.id.imgPerfil);

        btnSeleccionarIne = view.findViewById(R.id.btnSubirImagen);
        btnCompletarPerfil = view.findViewById(R.id.btnCompletarPerfil);
        txtCurp = view.findViewById(R.id.txtCurp);
        labelNombreImagen = view.findViewById(R.id.labelNombreImagen);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        ineImageUri = result.getData().getData();
                        String fileName = ineImageUri.getLastPathSegment();
                        labelNombreImagen.setText(fileName != null ? fileName : "Archivo seleccionado");
                    }
                }
        );

        // Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("usuarios").document(userId);

            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Obtener datos del usuario
                            String fotoUrl = documentSnapshot.getString("fotoPerfil");
                            String curp = documentSnapshot.getString("curp");

                            // Mostrar foto de perfil si existe
                            if (fotoUrl != null) {
                                Glide.with(requireContext())
                                        .load(fotoUrl)
                                        .into(fotoImageView);
                            }

                            // Mostrar CURP si existe
                            if (curp != null && !curp.isEmpty()) {
                                txtCurp.setText(curp);
                            } else {
                                txtCurp.setText(""); // Mostrar vacío si no hay CURP
                                Log.e("CompletarPerfil", "CURP no encontrada.");
                            }
                        } else {
                            // Documento no encontrado, inicializar datos
                            Log.e("CompletarPerfil", "Documento del usuario no existe.");
                            inicializarDatosUsuario(userId, userRef);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al cargar datos del usuario.", Toast.LENGTH_SHORT).show();
                        Log.e("CompletarPerfil", "Error al obtener el documento", e);
                    });
        } else {
            Toast.makeText(getContext(), "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
        }

        // Pedir permisos si no están otorgados
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        // Evento para seleccionar foto
        btnCambiarFoto.setOnClickListener(v -> abrirGaleria());

        btnSeleccionarIne.setOnClickListener(v -> abrirGaleriaIne());

        // Evento para completar el perfil
        btnCompletarPerfil.setOnClickListener(v -> guardarInformacion());
    }

    /**
     * Inicializa un documento vacío en Firestore para nuevos usuarios
     */
    private void inicializarDatosUsuario(String userId, DocumentReference userRef) {
        Map<String, Object> defaultData = new HashMap<>();
        defaultData.put("fotoPerfil", null);
        defaultData.put("curp", "");

        userRef.set(defaultData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Usuario inicializado correctamente.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al inicializar datos del usuario.", Toast.LENGTH_SHORT).show();
                    Log.e("CompletarPerfil", "Error al inicializar documento", e);
                });
    }


    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void abrirGaleriaIne() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Mostrar la imagen seleccionada en el ImageView
            Glide.with(this).load(imageUri).into(fotoImageView);

            // Subir la imagen a Firebase Storage
            subirImagenAFirebase(imageUri);
        }
    }

    private void subirImagenAFirebase(Uri imageUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child("usuarios/" + userId + "/fotoPerfil.jpg");

        userImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener la URL de la imagen subida
                    userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Guardar la URL de la imagen en Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("usuarios").document(userId)
                                .update("fotoPerfil", uri.toString())
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Foto subida con éxito", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar URL en Firestore", Toast.LENGTH_SHORT).show());
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show());
    }

    private void guardarInformacion() {
        String curp = txtCurp.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Validar que ambos campos estén completos
        if (curp.isEmpty() || ineImageUri == null) {
            Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference ineImageRef = storageRef.child("usuarios/" + userId + "/ine.jpg");

        // Subir imagen de INE
        ineImageRef.putFile(ineImageUri)
                .addOnSuccessListener(taskSnapshot -> ineImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String ineImageUrl = uri.toString();

                    // Guardar CURP y URL de INE en Firestore
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("curp", curp);
                    updates.put("fotoINE", ineImageUrl);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("usuarios").document(userId).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Información guardada con éxito", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al guardar la información", Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }
}