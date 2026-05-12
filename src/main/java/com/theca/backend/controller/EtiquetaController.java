/**
 * Descripción: controlador de la entidad Etiqueta.
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

import com.theca.backend.dto.etiqueta.CreateEtiquetaDTO;
import com.theca.backend.dto.etiqueta.UpdateEtiquetaDTO;
import com.theca.backend.entity.Etiqueta;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.EtiquetaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Etiquetas", description = "Endpoints para gestionar etiquetas")
@RequestMapping("/api/etiquetas")
public class EtiquetaController {

    private final EtiquetaRepository etiquetaRepository;

    public EtiquetaController(EtiquetaRepository etiquetaRepository) {
        this.etiquetaRepository = etiquetaRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las etiquetas del usuario autenticado", description = "Devuelve una lista de todas las etiquetas del usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de etiquetas obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<Etiqueta> getAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return etiquetaRepository.findByUsuarioId(username);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener etiqueta por ID", description = "Devuelve una etiqueta específica según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Etiqueta obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Etiqueta no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Etiqueta> getById(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return etiquetaRepository.findById(id)
                .filter(etiqueta -> etiqueta.getUsuarioId().equals(username))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear nueva etiqueta", description = "Crea una nueva etiqueta con los datos proporcionados")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Etiqueta creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> create(@Valid @RequestBody CreateEtiquetaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        if (etiquetaRepository.existsByNombreAndUsuarioId(dto.getNombre(), username)) {
            return ResponseEntity.badRequest().body("Ya existe una etiqueta con el nombre '" + dto.getNombre() + "'");
        }
        
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre(dto.getNombre());
        etiqueta.setUsuarioId(username);
        etiqueta.setFechaModificacion(LocalDateTime.now());
        etiqueta.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(etiquetaRepository.save(etiqueta));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar etiqueta existente", description = "Actualiza una etiqueta existente con los datos proporcionados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Etiqueta actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Etiqueta no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> update(@PathVariable String id,
                                    @Valid @RequestBody UpdateEtiquetaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return etiquetaRepository.findById(id)
                .filter(etiqueta -> etiqueta.getUsuarioId().equals(username))
                .map(etiquetaExistente -> {
                    if (dto.getNombre() != null && !dto.getNombre().equals(etiquetaExistente.getNombre())) {
                        if (etiquetaRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), username, id)) {
                            return ResponseEntity.badRequest().body("Ya existe una etiqueta con el nombre '" + dto.getNombre() + "'");
                        }
                        etiquetaExistente.setNombre(dto.getNombre());
                    }
                    
                    if (dto.getEstadoSincronizacion() != null) {
                        etiquetaExistente.setEstadoSincronizacion(dto.getEstadoSincronizacion());
                    }
                    etiquetaExistente.setFechaModificacion(LocalDateTime.now());
                    return ResponseEntity.ok(etiquetaRepository.save(etiquetaExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar etiqueta por ID", description = "Elimina una etiqueta específica según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Etiqueta eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Etiqueta no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return etiquetaRepository.findById(id)
                .filter(etiqueta -> etiqueta.getUsuarioId().equals(username))
                .map(etiqueta -> {
                    etiquetaRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/recursos")
    @Operation(summary = "Obtener recursos asociados a una etiqueta", description = "Devuelve una lista de recursos que tienen esta etiqueta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de recursos obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Etiqueta no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> getRecursosAsociados(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return etiquetaRepository.findById(id)
                .filter(etiqueta -> etiqueta.getUsuarioId().equals(username))
                .map(etiqueta -> {
                    return ResponseEntity.ok().body(java.util.Collections.emptyList());
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
}