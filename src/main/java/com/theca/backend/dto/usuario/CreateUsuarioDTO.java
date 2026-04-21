/**
 * Descripción: DTO para crear un nuevo Usuario.
 *
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 * 
 */
package com.theca.backend.dto.usuario;

public class CreateUsuarioDTO {

    private String nombre;
    private String correo;
    private String contrasena;

    public CreateUsuarioDTO() {}

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

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
