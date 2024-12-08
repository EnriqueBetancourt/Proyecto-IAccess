package com.example.iacccess;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VisitaAdapter extends RecyclerView.Adapter<VisitaAdapter.VisitaViewHolder> {

    private List<Visita> visitasList;
    private OnVisitaSelectedListener listener;  // Interfaz para pasar el idDocumentoVisita al fragmento

    // Constructor para pasar el listener desde el fragmento
    public VisitaAdapter(List<Visita> visitasList, OnVisitaSelectedListener listener) {
        this.visitasList = visitasList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VisitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.visitas, parent, false);
        return new VisitaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitaViewHolder holder, int position) {
        Visita visita = visitasList.get(position);

        // Mostrar el nombre completo del visitante en lugar del id
        holder.textNombre.setText(visita.getNombreVisitante());
        holder.textMotivo.setText(visita.getMotivo());
        holder.textEntrada.setText("Entrada: " + visita.getFechaHoraEntrada());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                listener.onVisitaSelected(visita.getIdDocumento());
            }
        });
    }


    @Override
    public int getItemCount() {
        return visitasList.size();
    }

    public static class VisitaViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, textMotivo, textEntrada;
        CheckBox checkBox;

        public VisitaViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombre);
            textMotivo = itemView.findViewById(R.id.textMotivo);
            textEntrada = itemView.findViewById(R.id.textEntrada);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    // Interfaz para pasar el idDocumentoVisita al fragmento
    public interface OnVisitaSelectedListener {
        void onVisitaSelected(String idDocumentoVisita);  // Cambié la firma para recibir el idDocumentoVisita
    }
}
