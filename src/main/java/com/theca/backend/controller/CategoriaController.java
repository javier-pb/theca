/**
 * Descripción: controlador de la entidad Categoria.
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

import com.theca.backend.dto.categoria.UpdateCategoriaDTO;
import com.theca.backend.entity.Categoria;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.CategoriaRepository;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<Categoria> getAll() {
        return categoriaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> getById(@PathVariable String id) {
        return categoriaRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria create(@RequestBody Categoria categoria) {
        categoria.setFechaModificacion(LocalDateTime.now());
        categoria.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        return categoriaRepository.save(categoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> update(@PathVariable String id, @RequestBody UpdateCategoriaDTO categoriaActualizada) {
        return categoriaRepository.findById(id).map(categoriaExistente -> {
            if (categoriaActualizada.getNombre() != null) {
                categoriaExistente.setNombre(categoriaActualizada.getNombre());
            }
            if (categoriaActualizada.getCategoriaPadreId() != null) {
                categoriaExistente.setCategoriaPadreId(categoriaActualizada.getCategoriaPadreId());
            }
            if (categoriaActualizada.getEstadoSincronizacion() != null) {
                categoriaExistente.setEstadoSincronizacion(categoriaActualizada.getEstadoSincronizacion());
            }
            categoriaExistente.setFechaModificacion(LocalDateTime.now());
            return ResponseEntity.ok(categoriaRepository.save(categoriaExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
