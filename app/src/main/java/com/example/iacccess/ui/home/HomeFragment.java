package com.example.iacccess.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.iacccess.MainActivity;
import com.example.iacccess.R;
import com.example.iacccess.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Button btnProcesarVisita, btnCompletarPerfil, btnVerHistorial;

    private FragmentHomeBinding binding;

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.Inicio));
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Referencias a tus vistas
        TextView nombreTextView = view.findViewById(R.id.labelFraccionamiento);
        ImageView fotoImageView = view.findViewById(R.id.imgAccesoQR);
        btnProcesarVisita = view.findViewById(R.id.btnProcesarVista);
        btnCompletarPerfil = view.findViewById(R.id.btnCompletarPerfil);
        btnVerHistorial = view.findViewById(R.id.btnVerHistorial);
        final NavController navController = Navigation.findNavController(view);


        // Obtener el usuario actual
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Consultar Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("usuarios").document(userId);

            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Obtener los datos
                            String nombre = documentSnapshot.getString("nombre");
                            String apellido = documentSnapshot.getString("apellido");
                            String fotoUrl = documentSnapshot.getString("fotoPerfil");
                            String curp = documentSnapshot.getString("curp");
                            String fotoIne = documentSnapshot.getString("fotoIne");

                            // Actualizar vistas
                            if (nombre != null) {
                                nombreTextView.setText(nombre + " " + apellido);
                            }

                            if (fotoUrl != null) {
                                Glide.with(requireContext())
                                        .load(fotoUrl)
                                        .into(fotoImageView);
                            }

                            if ((curp == null || curp.isEmpty()) && (fotoIne == null || fotoIne.isEmpty())){
                                btnProcesarVisita.setEnabled(false);
                            }  else {
                                btnProcesarVisita.setEnabled(true);
                            }
                        } else {
                            Toast.makeText(getContext(), "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al cargar datos del usuario.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
        }

        btnCompletarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.completarPerfil);
            }
        });

        btnProcesarVisita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.procesarVIsita);
            }
        });

        btnVerHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.historialVisitasVisitante);
            }
        });
    }
}