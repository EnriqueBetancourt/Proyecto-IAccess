package com.example.iacccess.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.iacccess.R;
import com.example.iacccess.databinding.FragmentSlideshowBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private Button registerButton;
    private EditText txtCodigoResidente;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.accederResidente));
        }
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

            // Verificar si el usuario ya tiene rol de residente
            db.collection("usuarios").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            if (documentSnapshot.exists()) {
                                Object rolesObj = documentSnapshot.get("roles");
                                if (rolesObj instanceof Map) {
                                    Map<String, Object> rolesMap = (Map<String, Object>) rolesObj;
                                    Object residenteRole = rolesMap.get("residente");

                                    if (residenteRole != null) {
                                        // Si ya tiene el rol de residente, redirigir al menú
                                        Navigation.findNavController(view).navigate(R.id.menu_residente);
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
            txtCodigoResidente = view.findViewById(R.id.txtCodigoResidente);

            registerButton.setOnClickListener(v -> {
                String codigoIngresado = txtCodigoResidente.getText().toString();

                if (codigoIngresado.isEmpty()) {
                    Toast.makeText(getContext(), "Por favor, ingresa un código.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar si el código ingresado pertenece a un fraccionamiento
                db.collection("fraccionamientos")
                        .whereEqualTo("codigoResidente", codigoIngresado)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Código válido, obtener el ID del fraccionamiento
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                String fraccionamientoId = document.getId();

                                // Actualizar el fraccionamiento para agregar al residente
                                db.collection("fraccionamientos").document(fraccionamientoId)
                                        .update("residentes", FieldValue.arrayUnion(userId))
                                        .addOnSuccessListener(aVoid -> {
                                            // Actualizar el rol del usuario a "residente"
                                            db.collection("usuarios").document(userId)
                                                    .update("roles.residente", true) // Marcar residente como `true`
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Toast.makeText(getContext(), "¡Registrado como residente exitosamente!", Toast.LENGTH_SHORT).show();

                                                        // Redirigir al menú de residente
                                                        Navigation.findNavController(view).navigate(R.id.menu_residente);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getContext(), "Error al asignar rol de residente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}