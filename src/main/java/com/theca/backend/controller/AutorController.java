/**
 * Descripción: controlador de la entidad Autor.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 * 
 */

package com.theca.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.theca.backend.dto.autor.CreateAutorDTO;
import com.theca.backend.dto.autor.UpdateAutorDTO;
import com.theca.backend.entity.Autor;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.AutorRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Autores", description = "Endpoints para gestionar autores")
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorRepository autorRepository;

    public AutorController(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los autores del usuario autenticado", description = "Devuelve una lista de todos los autores del usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de autores obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<Autor> getAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return autorRepository.findByUsuarioId(username);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener autor por ID", description = "Devuelve un autor específico según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autor obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Autor> getById(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(username))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un nuevo autor", description = "Crea un nuevo autor con los datos proporcionados")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Autor creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> create(@Valid @RequestBody CreateAutorDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        if (autorRepository.existsByNombreAndUsuarioId(dto.getNombre(), username)) {
            return ResponseEntity.badRequest().body("Ya existe un autor con el nombre '" + dto.getNombre() + "'");
        }
        
        Autor autor = new Autor();
        autor.setNombre(dto.getNombre());
        autor.setUsuarioId(username);
        autor.setFechaModificacion(LocalDateTime.now());
        autor.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(autorRepository.save(autor));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un autor existente", description = "Actualiza un autor existente con los datos proporcionados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autor actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody UpdateAutorDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(username))
                .map(autorExistente -> {
                    if (dto.getNombre() != null && !dto.getNombre().equals(autorExistente.getNombre())) {
                        if (autorRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), username, id)) {
                            return ResponseEntity.badRequest().body("Ya existe un autor con el nombre '" + dto.getNombre() + "'");
                        }
                        autorExistente.setNombre(dto.getNombre());
                    }
                    
                    if (dto.getEstadoSincronizacion() != null) {
                        autorExistente.setEstadoSincronizacion(dto.getEstadoSincronizacion());
                    }
                    autorExistente.setFechaModificacion(LocalDateTime.now());
                    return ResponseEntity.ok(autorRepository.save(autorExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un autor", description = "Elimina un autor específico según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Autor eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(username))
                .map(autor -> {
                    autorRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/recursos")
    @Operation(summary = "Obtener recursos asociados a un autor", description = "Devuelve una lista de recursos que tienen este autor")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de recursos obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> getRecursosAsociados(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(username))
                .map(autor -> {
                    return ResponseEntity.ok().body(java.util.Collections.emptyList());
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
}