package com.example.iacccess;

public class HistorialVisitas {
    private String fechaHoraEntrada;
    private String fechaHoraSalida;
    private String idPortero;
    private String idResidente;
    private String idVisitante;
    private String motivo;
    private String fraccionamiento;
    private String nombreVisitante;
    private String nombreResidente;

    private String idFraccionamiento;

    // Constructor
    public HistorialVisitas(String fechaHoraEntrada, String fechaHoraSalida, String idPortero, String idResidente, String idVisitante, String motivo, String fraccionamiento) {
        this.fechaHoraEntrada = fechaHoraEntrada;
        this.fechaHoraSalida = fechaHoraSalida;
        this.idPortero = idPortero;
        this.idResidente = idResidente;
        this.idVisitante = idVisitante;
        this.motivo = motivo;
        this.fraccionamiento = fraccionamiento;
    }

    public HistorialVisitas() {}

    // Getters
    public String getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public String getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public String getFraccionamiento() {
        return fraccionamiento;
    }

    public String getIdVisitante() {
        return idVisitante;
    }

    public String getIdResidente() {
        return idResidente;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getNombreVisitante() {
        return nombreVisitante;
    }

    public void setNombreVisitante(String nombreVisitante) {
        this.nombreVisitante = nombreVisitante;
    }

    public String getNombreResidente() {
        return nombreResidente;
    }

    public void setNombreResidente(String nombreResidente) {
        this.nombreResidente = nombreResidente;
    }

    public String getIdFraccionamiento() {
        return idFraccionamiento;
    }

    public void setIdFraccionamiento(String idFraccionamiento) {
        this.idFraccionamiento = idFraccionamiento;
    }
}