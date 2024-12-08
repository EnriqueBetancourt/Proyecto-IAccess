    package com.example.iacccess;

    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.fragment.app.Fragment;

    import com.bumptech.glide.Glide;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.QueryDocumentSnapshot;
    import com.google.firebase.firestore.QuerySnapshot;

    public class ProcesarVisita extends Fragment {

        private static final String ARG_PARAM1 = "param1";
        private static final String ARG_PARAM2 = "param2";

        private String mParam1;
        private String mParam2;
        FirebaseAuth mAuth;
        FirebaseUser currentUser;
        ImageView imgAccesoQr;
        TextView txtVisita, txtEntrada, txtSalida;

        public ProcesarVisita() {
            // Required empty public constructor
        }

        public static ProcesarVisita newInstance(String param1, String param2) {
            ProcesarVisita fragment = new ProcesarVisita();
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
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.btnProcesarVisita));
            }
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_procesar_visita, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            imgAccesoQr = view.findViewById(R.id.imgAccesoQR);
            txtEntrada = view.findViewById(R.id.txtEntrada);
            txtVisita = view.findViewById(R.id.txtVisita);

            // Obtener el usuario actual
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Consultar Firestore para obtener el código QR
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("usuarios").document(userId);

                userRef.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Obtener los datos
                                String imgQR = documentSnapshot.getString("codigoQR");

                                // Actualizar vistas con el código QR
                                if (imgQR != null) {
                                    Glide.with(requireContext())
                                            .load(imgQR)
                                            .into(imgAccesoQr);
                                }
                            } else {
                                Toast.makeText(getContext(), "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al cargar datos del usuario.", Toast.LENGTH_SHORT).show();
                        });

                // Verificar si el usuario está de visita
                verificarVisita(userId);

            } else {
                Toast.makeText(getContext(), "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            }
        }

        private void verificarVisita(String userId) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Usamos addSnapshotListener para escuchar los cambios en la colección "visitas" en tiempo real
            db.collection("visitas")
                    .whereEqualTo("idVisitante", userId)  // Verificamos si el idVisitante es igual al UID del usuario actual
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            Toast.makeText(getContext(), "Error al obtener las visitas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean encontrado = false;

                        // Recorrer los documentos obtenidos
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String visitaStatus = document.getString("idVisitante");
                            String fechaEntrada = document.getString("fechaHoraEntrada");

                            // Si el idVisitante coincide con el usuario actual, entonces está de visita
                            if (userId.equals(visitaStatus)) {
                                txtVisita.setText("SI");  // El usuario está de visita
                                txtEntrada.setText(fechaEntrada);  // Mostrar la hora de entrada
                                encontrado = true;
                                break;
                            }
                        }

                        // Si no encuentra al usuario como visitante
                        if (!encontrado) {
                            txtVisita.setText("NO");  // El usuario no está de visita
                            txtEntrada.setText("");   // Vaciar la hora de entrada
                        }
                    });
        }

    }
