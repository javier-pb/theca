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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.theca.backend.dto.autor.AsociarRecursosDTO;
import com.theca.backend.dto.autor.CreateAutorDTO;
import com.theca.backend.dto.autor.UpdateAutorDTO;
import com.theca.backend.entity.Autor;
import com.theca.backend.entity.Recurso;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.AutorRepository;
import com.theca.backend.repository.RecursoRepository;

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
    
    @Autowired
    private RecursoRepository recursoRepository;

    public AutorController(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    // Método auxiliar para obtener el userId del token:
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object details = auth.getDetails();
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) details;
            return (String) detailsMap.get("userId");
        }
        return null;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los autores", description = "Devuelve una lista de todos los autores")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de autores obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<Autor> getAll() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return List.of();
        }
        return autorRepository.findByUsuarioId(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener autor por ID", description = "Devuelve un autor específico según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autor obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Autor> getById(@PathVariable String id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/recursos")
    @Operation(summary = "Obtener recursos asociados a un autor", description = "Devuelve una lista de recursos asociados al autor")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de recursos obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<Recurso>> getRecursosAsociados(@PathVariable String id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(userId))
                .map(autor -> {
                    List<Recurso> recursos = recursoRepository.findByAutoresId(id);
                    return ResponseEntity.ok(recursos);
                })
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
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body("Usuario no autenticado");
        }
        
        if (autorRepository.existsByNombreAndUsuarioId(dto.getNombre(), userId)) {
            return ResponseEntity.badRequest()
                .body("Ya existe un autor con el nombre '" + dto.getNombre() + "'");
        }
        
        Autor autor = new Autor();
        autor.setNombre(dto.getNombre());
        autor.setUsuarioId(userId);
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
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body("Usuario no autenticado");
        }
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(userId))
                .map(autorExistente -> {
                    if (dto.getNombre() != null && !dto.getNombre().equals(autorExistente.getNombre())) {
                        if (autorRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), userId, id)) {
                            return ResponseEntity.badRequest()
                                .body("Ya existe un autor con el nombre '" + dto.getNombre() + "'");
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
    public ResponseEntity<?> delete(@PathVariable String id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body("Usuario no autenticado");
        }
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(userId))
                .map(autor -> {
                    List<Recurso> recursos = recursoRepository.findByAutoresId(id);
                    for (Recurso recurso : recursos) {
                        if (recurso.getAutores() != null) {
                            recurso.setAutores(recurso.getAutores().stream()
                                .filter(a -> !a.getId().equals(id))
                                .collect(Collectors.toList()));
                            recurso.setFechaModificacion(LocalDateTime.now());
                            recursoRepository.save(recurso);
                        }
                    }
                    autorRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/recursos")
    @Operation(summary = "Asociar recursos a un autor", description = "Asocia una lista de recursos al autor especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recursos asociados exitosamente"),
        @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> asociarRecursos(@PathVariable String id, @RequestBody AsociarRecursosDTO dto) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body("Usuario no autenticado");
        }
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(userId))
                .map(autor -> {
                    List<String> recursosIds = dto.getRecursosIds();
                    
                    for (String recursoId : recursosIds) {
                        recursoRepository.findById(recursoId).ifPresent(recurso -> {
                            if (recurso.getAutores() == null) {
                                recurso.setAutores(new ArrayList<>());
                            }
                            boolean yaExiste = recurso.getAutores().stream()
                                .anyMatch(a -> a.getId().equals(id));
                            if (!yaExiste) {
                                recurso.getAutores().add(autor);
                                recurso.setFechaModificacion(LocalDateTime.now());
                                recursoRepository.save(recurso);
                            }
                        });
                    }
                    
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/recursos")
    @Operation(summary = "Desasociar recursos de un autor", description = "Desasocia una lista de recursos del autor especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recursos desasociados exitosamente"),
        @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> desasociarRecursos(@PathVariable String id, @RequestBody AsociarRecursosDTO dto) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body("Usuario no autenticado");
        }
        
        return autorRepository.findById(id)
                .filter(autor -> autor.getUsuarioId().equals(userId))
                .map(autor -> {
                    List<String> recursosIds = dto.getRecursosIds();
                    
                    for (String recursoId : recursosIds) {
                        recursoRepository.findById(recursoId).ifPresent(recurso -> {
                            if (recurso.getAutores() != null) {
                                recurso.setAutores(recurso.getAutores().stream()
                                    .filter(a -> !a.getId().equals(id))
                                    .collect(Collectors.toList()));
                                recurso.setFechaModificacion(LocalDateTime.now());
                                recursoRepository.save(recurso);
                            }
                        });
                    }
                    
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

}