package com.example.iacccess;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.ViewHolder> {

    private Context context;
    private List<Solicitud> solicitudList;
    private RegistrarAcceso fragment;
    private List<Solicitud> selectedList = new ArrayList<>();
    private String celularSeleccionado; // Almacena el celular obtenido de la solicitud seleccionada

    public SolicitudAdapter(RegistrarAcceso fragment, List<Solicitud> solicitudList) {
        this.context = fragment.getContext();
        this.fragment = fragment;
        this.solicitudList = solicitudList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.solicitudes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Solicitud solicitud = solicitudList.get(position);

        holder.textNombre.setText(solicitud.getNombre());
        holder.textMotivo.setText(solicitud.getMotivo());

        holder.checkBox.setOnCheckedChangeListener(null); // Elimina listeners previos
        holder.checkBox.setChecked(false); // Resetea el estado del checkbox

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedList.add(solicitud); // Agrega la solicitud a la lista de seleccionados
                obtenerCelularUsuario(solicitud.getIdResidente());
            } else {
                selectedList.remove(solicitud); // Elimina la solicitud de la lista si no está seleccionada
            }
        });
    }

    private void obtenerCelularUsuario(String idResidente) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(idResidente)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String celular = documentSnapshot.getString("celular");
                        if (celular != null && !celular.isEmpty()) {
                            celularSeleccionado = celular;
                            fragment.setCelularLlamada(celular); // Envía el celular al fragmento
                        } else {
                            Log.e("SolicitudAdapter", "El celular está vacío.");
                        }
                    } else {
                        Log.e("SolicitudAdapter", "No se encontró el documento.");
                    }
                })
                .addOnFailureListener(e -> Log.e("SolicitudAdapter", "Error al obtener el celular: ", e));
    }

    @Override
    public int getItemCount() {
        return solicitudList.size();
    }

    public List<Solicitud> getSelectedList() {
        return selectedList;
    }

    // Nuevo método para eliminar una solicitud de la lista
    public void eliminarSolicitud(Solicitud solicitud) {
        solicitudList.remove(solicitud);
        notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textNombre, textMotivo;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombre);
            textMotivo = itemView.findViewById(R.id.textMotivo);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
