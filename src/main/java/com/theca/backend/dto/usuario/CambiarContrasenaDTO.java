/**
 * Descripción: DTO para cambiar la contraseña del usuario.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 9 may 2026
 */

package com.theca.backend.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CambiarContrasenaDTO {
    
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String contrasenaActual;
    
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String nuevaContrasena;

    public CambiarContrasenaDTO() {}

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public void setContrasenaActual(String contrasenaActual) {
        this.contrasenaActual = contrasenaActual;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }
    
}