/**
 * Descripción: repositorio de la entidad Usuario.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 18 abr 2026
 * 
 */

package com.theca.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.theca.backend.entity.Usuario;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {}
