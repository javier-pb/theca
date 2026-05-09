/**
 * Descripción: DTO para la respuesta del perfil de usuario.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 9 may 2026
 */

package com.theca.backend.dto.usuario;

public class UsuarioPerfilDTO {
	
    private String id;
    private String nombre;
    private String correo;
    private String fechaCreacion;

    public UsuarioPerfilDTO() {}

    public UsuarioPerfilDTO(String id, String nombre, String correo, String fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.fechaCreacion = fechaCreacion;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
}