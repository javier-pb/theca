/**
 * Descripción: controlador de la entidad Cola (sincronización offline).
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 * 
 */

package com.theca.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.theca.backend.entity.Cola;
import com.theca.backend.repository.ColaRepository;

@RestController
@RequestMapping("/api/cola")
public class ColaController {

    private final ColaRepository colaRepository;

    public ColaController(ColaRepository colaRepository) {
        this.colaRepository = colaRepository;
    }

    @GetMapping
    public List<Cola> getAll() {
        return colaRepository.findAll();
    }

    @GetMapping("/pendientes")
    public List<Cola> getPendientes() {
        return colaRepository.findBySincronizadoFalse();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cola> getById(@PathVariable String id) {
        return colaRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cola create(@RequestBody Cola cola) {
        cola.setFechaModificacion(LocalDateTime.now());
        cola.setSincronizado(false);
        return colaRepository.save(cola);
    }

    @PutMapping("/sincronizar/{id}")
    public ResponseEntity<Cola> marcarSincronizado(@PathVariable String id) {
        return colaRepository.findById(id).map(c -> {
            c.setSincronizado(true);
            c.setFechaModificacion(LocalDateTime.now());
            return ResponseEntity.ok(colaRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }
}
