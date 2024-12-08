package com.example.iacccess;

public class Visita {
    private String idResidente;
    private String idVisitante;
    private String idPortero;

    private String idDocumento;
    private String motivo;
    private String fechaHoraEntrada;
    private String fechaHoraSalida;
    private double latitud;
    private double longitud;


    // Constructor vacío requerido para Firebase
    public Visita() {}
    private String nombreVisitante;

    // Constructor
    public Visita(String idResidente, String idVisitante, String idPortero, String motivo,
                  String fechaHoraEntrada, String idDocumento, double latitud, double longitud) {
        this.idResidente = idResidente;
        this.idVisitante = idVisitante;
        this.idPortero = idPortero;
        this.motivo = motivo;
        this.fechaHoraEntrada = fechaHoraEntrada;
        this.idDocumento = idDocumento;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Getters y setters

    public String getNombreVisitante() {
        return nombreVisitante;
    }

    public void setNombreVisitante(String nombreVisitante) {
        this.nombreVisitante = nombreVisitante;
    }

    public void setIdDocumento(String idDocumento) {
        this.idDocumento = idDocumento;
    }
    // Getters y setters
    public String getIdResidente() {
        return idResidente;
    }

    public void setIdResidente(String idResidente) {
        this.idResidente = idResidente;
    }

    public String getIdVisitante() {
        return idVisitante;
    }

    public void setIdVisitante(String idVisitante) {
        this.idVisitante = idVisitante;
    }

    public String getIdPortero() {
        return idPortero;
    }

    public void setIdPortero(String idPortero) {
        this.idPortero = idPortero;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public void setFechaHoraEntrada(String fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public String getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(String fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getIdDocumento() {
        return idDocumento;
    }
}
