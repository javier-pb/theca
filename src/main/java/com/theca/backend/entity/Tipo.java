/**
 * Descripción: entidad Tipo.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 18 abr 2026
 * 
 */

package com.theca.backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.theca.backend.enums.EstadoSincronizacion;

import jakarta.validation.constraints.NotBlank;

@Document(collection = "tipos")
public class Tipo {
    
    @Id
    private String id;
    @NotBlank(message = "El nombre del tipo de recurso es obligatorio")
    private String nombre;
    private byte[] imagen;
    private boolean esPredeterminado;
    private LocalDateTime fechaModificacion;
    private EstadoSincronizacion estadoSincronizacion;
    private String usuarioId;
    
    public Tipo() {}
    
    public Tipo(String id, @NotBlank(message = "El nombre del tipo de recurso es obligatorio") String nombre, byte[] imagen,
    			boolean esPredeterminado, LocalDateTime fechaModificacion, EstadoSincronizacion estadoSincronizacion, String usuarioId) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.imagen = imagen;
		this.esPredeterminado = esPredeterminado;
		this.fechaModificacion = fechaModificacion;
		this.estadoSincronizacion = estadoSincronizacion;
		this.usuarioId = usuarioId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public byte[] getImagen() {
		return imagen;
	}

	public void setImagen(byte[] imagen) {
		this.imagen = imagen;
	}

	public boolean isEsPredeterminado() {
		return esPredeterminado;
	}

	public void setEsPredeterminado(boolean esPredeterminado) {
		this.esPredeterminado = esPredeterminado;
	}

	public LocalDateTime getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(LocalDateTime fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public EstadoSincronizacion getEstadoSincronizacion() {
		return estadoSincronizacion;
	}

	public void setEstadoSincronizacion(EstadoSincronizacion estadoSincronizacion) {
		this.estadoSincronizacion = estadoSincronizacion;
	}

	public String getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(String usuarioId) {
		this.usuarioId = usuarioId;
	}
    
}