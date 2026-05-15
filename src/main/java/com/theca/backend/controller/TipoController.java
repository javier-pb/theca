/**
 * Descripción: controlador de la entidad Tipo.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 * 
 */

package com.theca.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

import com.theca.backend.dto.tipo.CreateTipoDTO;
import com.theca.backend.dto.tipo.UpdateTipoDTO;
import com.theca.backend.entity.Tipo;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.TipoRepository;
import com.theca.backend.security.jwt.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Tipos", description = "Endpoints para gestionar tipos de recursos")
@RequestMapping("/api/tipos")
public class TipoController {

    private final TipoRepository tipoRepository;
    private final JwtUtils jwtUtils;

    public TipoController(TipoRepository tipoRepository, JwtUtils jwtUtils) {
        this.tipoRepository = tipoRepository;
        this.jwtUtils = jwtUtils;
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
    @Operation(summary = "Obtener todos los tipos del usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de tipos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public List<Tipo> getAll() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return List.of();
        }
        return tipoRepository.findByUsuarioId(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo por ID", description = "Devuelve un tipo de recurso específico según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tipo obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Tipo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Tipo> getById(@PathVariable String id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        
        return tipoRepository.findById(id)
                .filter(tipo -> tipo.getUsuarioId().equals(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un nuevo tipo", description = "Crea un nuevo tipo de recurso")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tipo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> create(@Valid @RequestBody CreateTipoDTO dto) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body("Usuario no autenticado");
        }
        
        if (tipoRepository.existsByNombreAndUsuarioId(dto.getNombre(), userId)) {
            return ResponseEntity.badRequest().body("Ya existe un tipo con el nombre '" + dto.getNombre() + "'");
        }
        
        Tipo tipo = new Tipo();
        tipo.setNombre(dto.getNombre());
        tipo.setImagen(dto.getImagen());
        tipo.setUsuarioId(userId);
        tipo.setFechaModificacion(LocalDateTime.now());
        tipo.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        tipo.setEsPredeterminado(false);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoRepository.save(tipo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un tipo existente", description = "Actualiza un tipo de recurso existente con los datos proporcionados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tipo actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Tipo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody UpdateTipoDTO dto) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body("Usuario no autenticado");
        }
        
        return tipoRepository.findById(id)
                .filter(tipo -> tipo.getUsuarioId().equals(userId))
                .map(tipoExistente -> {
                    if (dto.getNombre() != null && !dto.getNombre().equals(tipoExistente.getNombre())) {
                        if (tipoRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), userId, id)) {
                            return ResponseEntity.badRequest().body("Ya existe un tipo con el nombre '" + dto.getNombre() + "'");
                        }
                        tipoExistente.setNombre(dto.getNombre());
                    }
                    
                    if (dto.getImagen() != null) {
                        tipoExistente.setImagen(dto.getImagen());
                    }
                    
                    if (dto.getEstadoSincronizacion() != null) {
                        tipoExistente.setEstadoSincronizacion(dto.getEstadoSincronizacion());
                    }
                    tipoExistente.setFechaModificacion(LocalDateTime.now());
                    return ResponseEntity.ok(tipoRepository.save(tipoExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un tipo", description = "Elimina un tipo de recurso existente según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tipo eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Tipo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> delete(@PathVariable String id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().body("Usuario no autenticado");
        }
        
        return tipoRepository.findById(id)
                .filter(tipo -> tipo.getUsuarioId().equals(userId))
                .map(tipo -> {
                    if (tipo.isEsPredeterminado()) {
                        return ResponseEntity.badRequest().body("No se puede eliminar un tipo predeterminado");
                    }
                    tipoRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
}