/**
 * Descripción: test unitario del controlador de la entidad Categoria.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 * 
 */

package com.theca.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.theca.backend.dto.categoria.CreateCategoriaDTO;
import com.theca.backend.dto.categoria.UpdateCategoriaDTO;
import com.theca.backend.entity.Categoria;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
public class CategoriaControllerTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CategoriaController categoriaController;

    private Categoria c1;
    private Categoria c2;
    private static final String TEST_USER = "testuser";

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(TEST_USER);

        c1 = new Categoria();
        c1.setId("1");
        c1.setNombre("C1");
        c1.setFechaModificacion(LocalDateTime.now());
        c1.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        c1.setUsuarioId(TEST_USER);

        c2 = new Categoria();
        c2.setId("2");
        c2.setNombre("C2");
        c2.setFechaModificacion(LocalDateTime.now());
        c2.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        c2.setUsuarioId(TEST_USER);
    }

    @Test
    void getAll_ShouldReturnListFilteredByUser() {
        when(categoriaRepository.findByUsuarioId(TEST_USER)).thenReturn(Arrays.asList(c1, c2));
        
        List<Categoria> result = categoriaController.getAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoriaRepository, times(1)).findByUsuarioId(TEST_USER);
    }

    @Test
    void getById_ShouldReturnCategoria_WhenBelongsToUser() {
        when(categoriaRepository.findById("1")).thenReturn(Optional.of(c1));
        
        ResponseEntity<Categoria> resp = categoriaController.getById("1");
        
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("C1", resp.getBody().getNombre());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenCategoriaNotBelongsToUser() {
        Categoria categoriaDeOtroUsuario = new Categoria();
        categoriaDeOtroUsuario.setId("3");
        categoriaDeOtroUsuario.setNombre("C3");
        categoriaDeOtroUsuario.setUsuarioId("otheruser");
        
        when(categoriaRepository.findById("3")).thenReturn(Optional.of(categoriaDeOtroUsuario));
        
        ResponseEntity<Categoria> resp = categoriaController.getById("3");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenIdDoesNotExist() {
        when(categoriaRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<Categoria> resp = categoriaController.getById("999");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void create_ShouldSaveCategoriaWithUser_WhenNombreIsUnique() {
        CreateCategoriaDTO dto = new CreateCategoriaDTO();
        dto.setNombre("Nueva Categoria");
        dto.setCategoriaPadreId(null);
        
        when(categoriaRepository.existsByNombreAndUsuarioId(dto.getNombre(), TEST_USER)).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = categoriaController.create(dto);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Categoria saved = (Categoria) response.getBody();
        assertNotNull(saved.getFechaModificacion());
        assertEquals("Nueva Categoria", saved.getNombre());
        assertEquals(TEST_USER, saved.getUsuarioId());
        assertEquals(EstadoSincronizacion.PENDIENTE, saved.getEstadoSincronizacion());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenNombreAlreadyExists() {
        CreateCategoriaDTO dto = new CreateCategoriaDTO();
        dto.setNombre("C1");
        dto.setCategoriaPadreId(null);
        
        when(categoriaRepository.existsByNombreAndUsuarioId(dto.getNombre(), TEST_USER)).thenReturn(true);
        
        ResponseEntity<?> response = categoriaController.create(dto);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ya existe una categoría con el nombre 'C1'", response.getBody());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void update_ShouldUpdateCategoria_WhenBelongsToUserAndNombreIsUnique() {
        UpdateCategoriaDTO dto = new UpdateCategoriaDTO();
        dto.setNombre("C1 Actualizada");
        
        when(categoriaRepository.findById("1")).thenReturn(Optional.of(c1));
        when(categoriaRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), TEST_USER, "1")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = categoriaController.update("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Categoria updated = (Categoria) response.getBody();
        assertEquals("C1 Actualizada", updated.getNombre());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenNombreAlreadyExists() {
        UpdateCategoriaDTO dto = new UpdateCategoriaDTO();
        dto.setNombre("C2");
        
        when(categoriaRepository.findById("1")).thenReturn(Optional.of(c1));
        when(categoriaRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), TEST_USER, "1")).thenReturn(true);
        
        ResponseEntity<?> response = categoriaController.update("1", dto);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ya existe una categoría con el nombre 'C2'", response.getBody());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void update_ShouldSkipNombreValidation_WhenNombreNotChanged() {
        UpdateCategoriaDTO dto = new UpdateCategoriaDTO();
        dto.setNombre("C1");
        
        when(categoriaRepository.findById("1")).thenReturn(Optional.of(c1));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = categoriaController.update("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoriaRepository, never()).existsByNombreAndUsuarioIdAndIdNot(any(), any(), any());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenCategoriaNotBelongsToUser() {
        UpdateCategoriaDTO dto = new UpdateCategoriaDTO();
        dto.setNombre("Actualizada");
        
        Categoria categoriaDeOtroUsuario = new Categoria();
        categoriaDeOtroUsuario.setId("3");
        categoriaDeOtroUsuario.setNombre("C3");
        categoriaDeOtroUsuario.setUsuarioId("otheruser");
        
        when(categoriaRepository.findById("3")).thenReturn(Optional.of(categoriaDeOtroUsuario));
        
        ResponseEntity<?> response = categoriaController.update("3", dto);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void delete_ShouldDeleteCategoria_WhenBelongsToUser() {
        when(categoriaRepository.findById("1")).thenReturn(Optional.of(c1));
        
        ResponseEntity<Void> resp = categoriaController.delete("1");
        
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(categoriaRepository, times(1)).deleteById("1");
    }

    @Test
    void delete_ShouldReturnNotFound_WhenCategoriaNotBelongsToUser() {
        Categoria categoriaDeOtroUsuario = new Categoria();
        categoriaDeOtroUsuario.setId("3");
        categoriaDeOtroUsuario.setNombre("C3");
        categoriaDeOtroUsuario.setUsuarioId("otheruser");
        
        when(categoriaRepository.findById("3")).thenReturn(Optional.of(categoriaDeOtroUsuario));
        
        ResponseEntity<Void> resp = categoriaController.delete("3");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        verify(categoriaRepository, never()).deleteById(any());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenIdDoesNotExist() {
        when(categoriaRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<Void> resp = categoriaController.delete("999");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        verify(categoriaRepository, never()).deleteById(any());
    }
    
}