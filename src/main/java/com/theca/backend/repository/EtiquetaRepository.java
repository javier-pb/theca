/**
 * Descripción: repositorio de la entidad Etiqueta.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 * 
 */

package com.theca.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.theca.backend.entity.Etiqueta;

@Repository
public interface EtiquetaRepository extends MongoRepository<Etiqueta, String> {
	
	// Método para encontrar un usuario:
    List<Etiqueta> findByUsuarioId(String usuarioId);
    // Métodos para saber si existe un usuario:
    boolean existsByNombreAndUsuarioId(String nombre, String usuarioId);
    boolean existsByNombreAndUsuarioIdAndIdNot(String nombre, String usuarioId, String id);
    
}
