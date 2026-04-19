/**
 * Descripción: DTO para crear una nueva Etiqueta.
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 * 
 */
package com.theca.backend.dto;

public class CreateEtiquetaDTO {

    private String nombre;

    public CreateEtiquetaDTO() {}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
