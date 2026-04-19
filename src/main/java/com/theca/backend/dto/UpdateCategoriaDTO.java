/**
 * Descripción: DTO para actualizar una Categoría.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 */

package com.theca.backend.dto;

import com.theca.backend.enums.EstadoSincronizacion;

public class UpdateCategoriaDTO {

    private String nombre;
    private String categoriaPadreId;
    private EstadoSincronizacion estadoSincronizacion;

    public UpdateCategoriaDTO() {}

    public String getNombre() {
    	return nombre;
    }
    
    public void setNombre(String nombre) {
    	this.nombre = nombre;
    }

    public String getCategoriaPadreId() {
    	return categoriaPadreId;
    }
    
    public void setCategoriaPadreId(String categoriaPadreId) {
    	this.categoriaPadreId = categoriaPadreId;
    }

    public EstadoSincronizacion getEstadoSincronizacion() {
    	return estadoSincronizacion;
    }
    
    public void setEstadoSincronizacion(EstadoSincronizacion estadoSincronizacion) {
    	this.estadoSincronizacion = estadoSincronizacion;
    }

}
