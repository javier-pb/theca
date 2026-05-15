/**
 * Descripción: repositorio de la entidad Tipo.
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

import com.theca.backend.entity.Tipo;

@Repository
public interface TipoRepository extends MongoRepository<Tipo, String> {
	
    // Buscar tipos por usuario:
    List<Tipo> findByUsuarioId(String usuarioId);
    
    // Verificar si existe un tipo con el mismo nombre para un usuario:
    boolean existsByNombreAndUsuarioId(String nombre, String usuarioId);
    boolean existsByNombreAndUsuarioIdAndIdNot(String nombre, String usuarioId, String id);
	
}
