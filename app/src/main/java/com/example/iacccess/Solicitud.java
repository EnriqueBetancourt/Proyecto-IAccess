package com.example.iacccess;

public class Solicitud {
    private String idResidente;
    private String nombre;
    private String motivo;
    private long timestamp;
    private String documentId; // Nuevo campo
    public Solicitud(String idResidente, String nombre, String motivo, long timestamp, String documentId) {
        this.idResidente = idResidente;
        this.nombre = nombre;
        this.motivo = motivo;
        this.timestamp = timestamp;
        this.documentId = documentId; // Asignar el ID del documento
    }

    public String getDocumentId() {
        return documentId;
    }
    // Getters y Setters
    public String getIdResidente() {
        return idResidente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMotivo() {
        return motivo;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

