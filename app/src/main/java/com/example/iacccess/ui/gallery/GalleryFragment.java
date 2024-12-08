package com.example.iacccess.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.iacccess.R;
import com.example.iacccess.databinding.FragmentGalleryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private Button registerButton;
    private EditText txtCodigoPortero;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.accederPortero));
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "El usuario no está autenticado.", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Verificar si el usuario ya tiene el rol de portero
            db.collection("usuarios").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            if (documentSnapshot.exists()) {
                                Object rolesObj = documentSnapshot.get("roles");
                                if (rolesObj instanceof Map) {
                                    Map<String, Object> rolesMap = (Map<String, Object>) rolesObj;
                                    Object porteroRole = rolesMap.get("portero");

                                    if (porteroRole != null) {
                                        // Si ya tiene el rol de portero, redirigir al menú
                                        Navigation.findNavController(view).navigate(R.id.menuPortero2);
                                        return;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error al verificar rol: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al verificar el rol del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            // Configurar botón de registro
            registerButton = view.findViewById(R.id.registerButton);
            txtCodigoPortero = view.findViewById(R.id.txtCodigoPortero);

            registerButton.setOnClickListener(v -> {
                String codigoIngresado = txtCodigoPortero.getText().toString();

                if (codigoIngresado.isEmpty()) {
                    Toast.makeText(getContext(), "Por favor, ingresa un código.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar si el código ingresado pertenece a un fraccionamiento
                db.collection("fraccionamientos")
                        .whereEqualTo("codigoPortero", codigoIngresado)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Código válido, obtener el ID del fraccionamiento
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                String fraccionamientoId = document.getId();

                                // Actualizar el fraccionamiento para agregar al portero
                                db.collection("fraccionamientos").document(fraccionamientoId)
                                        .update("porteros", FieldValue.arrayUnion(userId))
                                        .addOnSuccessListener(aVoid -> {
                                            // Actualizar el rol del usuario a "portero"
                                            db.collection("usuarios").document(userId)
                                                    .update("roles.portero", true) // Marcar portero como `true`
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Toast.makeText(getContext(), "¡Registrado como portero exitosamente!", Toast.LENGTH_SHORT).show();

                                                        // Redirigir al menú de portero
                                                        Navigation.findNavController(view).navigate(R.id.menuPortero2);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getContext(), "Error al asignar rol de portero: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Error al registrar en fraccionamiento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(getContext(), "El código ingresado no es válido.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al buscar el código: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error inesperado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
