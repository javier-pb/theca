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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.theca.backend.dto.autor.UpdateAutorDTO;
import com.theca.backend.entity.Autor;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.AutorRepository;

@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorRepository autorRepository;

    public AutorController(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    @GetMapping
    public List<Autor> getAll() {
        return autorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autor> getById(@PathVariable String id) {
        return autorRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Autor create(@RequestBody Autor autor) {
        autor.setFechaModificacion(LocalDateTime.now());
        autor.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        return autorRepository.save(autor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Autor> update(@PathVariable String id, @RequestBody UpdateAutorDTO autorActualizado) {
        return autorRepository.findById(id).map(autorExistente -> {
            if (autorActualizado.getNombre() != null) {
                autorExistente.setNombre(autorActualizado.getNombre());
            }
            if (autorActualizado.getEstadoSincronizacion() != null) {
                autorExistente.setEstadoSincronizacion(autorActualizado.getEstadoSincronizacion());
            }
            autorExistente.setFechaModificacion(LocalDateTime.now());
            return ResponseEntity.ok(autorRepository.save(autorExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (autorRepository.existsById(id)) {
            autorRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
