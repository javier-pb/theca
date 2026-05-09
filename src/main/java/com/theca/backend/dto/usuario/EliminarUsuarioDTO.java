/**
 * Descripción: DTO para eliminar cuenta con verificación de contraseña.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 9 may 2026
 */

package com.theca.backend.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public class EliminarUsuarioDTO {
    
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;

    public EliminarUsuarioDTO() {}

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
}