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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.theca.backend.dto.tipo.UpdateTipoDTO;
import com.theca.backend.entity.Tipo;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.TipoRepository;

@RestController
@RequestMapping("/api/tipos")
public class TipoController {

    private final TipoRepository tipoRepository;

    public TipoController(TipoRepository tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    @GetMapping
    public List<Tipo> getAll() {
        return tipoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tipo> getById(@PathVariable String id) {
        return tipoRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tipo create(@RequestBody Tipo tipo) {
        tipo.setFechaModificacion(LocalDateTime.now());
        tipo.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        return tipoRepository.save(tipo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tipo> update(@PathVariable String id, @RequestBody UpdateTipoDTO tipoActualizado) {
        return tipoRepository.findById(id).map(tipoExistente -> {
            if (tipoActualizado.getNombre() != null) {
                tipoExistente.setNombre(tipoActualizado.getNombre());
            }
            if (tipoActualizado.getEstadoSincronizacion() != null) {
                tipoExistente.setEstadoSincronizacion(tipoActualizado.getEstadoSincronizacion());
            }
            tipoExistente.setFechaModificacion(LocalDateTime.now());
            return ResponseEntity.ok(tipoRepository.save(tipoExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (tipoRepository.existsById(id)) {
            tipoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
