/**
 * Descripción: controlador de la entidad Usuario.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 18 abr 2026
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

import com.theca.backend.entity.Usuario;
import com.theca.backend.dto.usuario.CreateUsuarioDTO;
import com.theca.backend.dto.usuario.UpdateUsuarioDTO;
import com.theca.backend.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    
    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    @GetMapping
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getById(@PathVariable String id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario create(@RequestBody CreateUsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        usuario.setContrasena(dto.getContrasena());
        usuario.setFechaCreacion(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable String id, @RequestBody UpdateUsuarioDTO usuarioActualizado) {
        return usuarioRepository.findById(id)
                .map(usuarioExistente -> {
                    if (usuarioActualizado.getNombre() != null) {
                        usuarioExistente.setNombre(usuarioActualizado.getNombre());
                    }
                    if (usuarioActualizado.getCorreo() != null) {
                        usuarioExistente.setCorreo(usuarioActualizado.getCorreo());
                    }
                    if (usuarioActualizado.getContrasena() != null) {
                        usuarioExistente.setContrasena(usuarioActualizado.getContrasena());
                    }
                    return ResponseEntity.ok(usuarioRepository.save(usuarioExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}