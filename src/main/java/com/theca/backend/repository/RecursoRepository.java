/**
 * Descripción: repositorio de la entidad Recurso.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 18 abr 2026
 * 
 */

package com.theca.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.theca.backend.entity.Recurso;
import com.theca.backend.entity.Usuario;

@Repository
public interface RecursoRepository extends MongoRepository<Recurso, String> {
    
	// Buscar todos los recursos de un usuario específico:
    List<Recurso> findByUsuario(Usuario usuario);
    
    // Buscar recursos de un usuario por su ID:
    List<Recurso> findByUsuarioId(String usuarioId);
    
    // Métodos para asociar/desasociar autores:
    @Query("{ 'id': { $in: ?0 } }")
    void asociarAutor(String autorId, List<String> recursosIds);

    @Query("{ 'id': { $in: ?0 } }")
    void desasociarAutor(String autorId, List<String> recursosIds);
    
    // Buscar recursos que tengan un autor específico por su ID:
    @Query("{ 'autores.id': ?0 }")
    List<Recurso> findByAutoresId(String autorId);
    
}