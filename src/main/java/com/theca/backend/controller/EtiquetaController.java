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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.theca.backend.entity.Etiqueta;
import com.theca.backend.dto.UpdateEtiquetaDTO;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.EtiquetaRepository;

@RestController
@RequestMapping("/api/etiquetas")
public class EtiquetaController {

    private final EtiquetaRepository etiquetaRepository;

    public EtiquetaController(EtiquetaRepository etiquetaRepository) {
        this.etiquetaRepository = etiquetaRepository;
    }

    @GetMapping
    public List<Etiqueta> getAll() {
        return etiquetaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Etiqueta> getById(@PathVariable String id) {
        return etiquetaRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Etiqueta create(@RequestBody Etiqueta etiqueta) {
        etiqueta.setFechaModificacion(LocalDateTime.now());
        etiqueta.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        return etiquetaRepository.save(etiqueta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Etiqueta> update(@PathVariable String id, @RequestBody UpdateEtiquetaDTO etiquetaActualizada) {
        return etiquetaRepository.findById(id).map(etiquetaExistente -> {
            if (etiquetaActualizada.getNombre() != null) {
                etiquetaExistente.setNombre(etiquetaActualizada.getNombre());
            }
            if (etiquetaActualizada.getEstadoSincronizacion() != null) {
                etiquetaExistente.setEstadoSincronizacion(etiquetaActualizada.getEstadoSincronizacion());
            }
            etiquetaExistente.setFechaModificacion(LocalDateTime.now());
            return ResponseEntity.ok(etiquetaRepository.save(etiquetaExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (etiquetaRepository.existsById(id)) {
            etiquetaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
