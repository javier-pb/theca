/**
 * Descripción: repositorio de la entidad Autor.
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

import com.theca.backend.entity.Autor;

@Repository
public interface AutorRepository extends MongoRepository<Autor, String> {
	
	// Método para obtener los autores de un usuario:
    List<Autor> findByUsuarioId(String usuarioId);
    // Métodos para comprobar si existe un autor:
    boolean existsByNombreAndUsuarioId(String nombre, String usuarioId);
    boolean existsByNombreAndUsuarioIdAndIdNot(String nombre, String usuarioId, String id);
    
}
