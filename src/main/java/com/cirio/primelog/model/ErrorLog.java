package com.cirio.primelog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "errores")
public class ErrorLog {

    @Id
    private String id;
    private String titulo;
    private String descripcionContexto; // Qué estabas intentando hacer
    private String solucionAplicada;    // Cómo lo arreglaste
    private LocalDateTime fechaRegistro;
    private String etiqueta;
    private String usuarioId;


    public ErrorLog(String id, String titulo, String descripcionContexto, String solucionAplicada, LocalDateTime fechaRegistro) {
        this.id = id;
        this.titulo = titulo;
        this.descripcionContexto = descripcionContexto;
        this.solucionAplicada = solucionAplicada;
        this.fechaRegistro = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcionContexto() {
        return descripcionContexto;
    }

    public void setDescripcionContexto(String descripcionContexto) {
        this.descripcionContexto = descripcionContexto;
    }

    public String getSolucionAplicada() {
        return solucionAplicada;
    }

    public void setSolucionAplicada(String solucionAplicada) {
        this.solucionAplicada = solucionAplicada;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    public String getEtiqueta() {
        return etiqueta;
    }
    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
