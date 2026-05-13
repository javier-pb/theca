/**
 * Descripción: DTO para asociar autores a los recursos.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 13 may 2026
 * 
 */

package com.theca.backend.dto.autor;

import java.util.List;

public class AsociarRecursosDTO {
	
    private List<String> recursosIds;

    public AsociarRecursosDTO() {}

    public List<String> getRecursosIds() {
        return recursosIds;
    }

    public void setRecursosIds(List<String> recursosIds) {
        this.recursosIds = recursosIds;
    }
    
}
