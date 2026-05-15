/**
 * Descripción: test unitario del controlador de la entidad Tipo.
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

import com.theca.backend.dto.tipo.CreateTipoDTO;
import com.theca.backend.dto.tipo.UpdateTipoDTO;
import com.theca.backend.entity.Tipo;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.TipoRepository;

@ExtendWith(MockitoExtension.class)
public class TipoControllerTest {

    @Mock
    private TipoRepository tipoRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TipoController tipoController;

    private Tipo tipo1;
    private Tipo tipo2;
    private static final String TEST_USER = "testuser";

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(TEST_USER);

        tipo1 = new Tipo();
        tipo1.setId("1");
        tipo1.setNombre("Libro");
        tipo1.setFechaModificacion(LocalDateTime.now());
        tipo1.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        tipo1.setUsuarioId(TEST_USER);
        tipo1.setEsPredeterminado(true);

        tipo2 = new Tipo();
        tipo2.setId("2");
        tipo2.setNombre("Artículo");
        tipo2.setFechaModificacion(LocalDateTime.now());
        tipo2.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        tipo2.setUsuarioId(TEST_USER);
        tipo2.setEsPredeterminado(false);
    }

    @Test
    void getAll_ShouldReturnListFilteredByUser() {
        when(tipoRepository.findByUsuarioId(TEST_USER)).thenReturn(Arrays.asList(tipo1, tipo2));
        
        List<Tipo> result = tipoController.getAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tipoRepository, times(1)).findByUsuarioId(TEST_USER);
    }

    @Test
    void getById_ShouldReturnTipo_WhenBelongsToUser() {
        when(tipoRepository.findById("1")).thenReturn(Optional.of(tipo1));
        
        ResponseEntity<Tipo> resp = tipoController.getById("1");
        
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Libro", resp.getBody().getNombre());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenTipoNotBelongsToUser() {
        Tipo tipoDeOtroUsuario = new Tipo();
        tipoDeOtroUsuario.setId("3");
        tipoDeOtroUsuario.setNombre("Otro");
        tipoDeOtroUsuario.setUsuarioId("otheruser");
        
        when(tipoRepository.findById("3")).thenReturn(Optional.of(tipoDeOtroUsuario));
        
        ResponseEntity<Tipo> resp = tipoController.getById("3");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void create_ShouldSaveTipo_WhenNombreIsUnique() {
        CreateTipoDTO dto = new CreateTipoDTO();
        dto.setNombre("Nuevo Tipo");
        
        when(tipoRepository.existsByNombreAndUsuarioId(dto.getNombre(), TEST_USER)).thenReturn(false);
        when(tipoRepository.save(any(Tipo.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = tipoController.create(dto);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Tipo saved = (Tipo) response.getBody();
        assertNotNull(saved.getFechaModificacion());
        assertEquals("Nuevo Tipo", saved.getNombre());
        assertEquals(TEST_USER, saved.getUsuarioId());
        assertEquals(false, saved.isEsPredeterminado());
        verify(tipoRepository, times(1)).save(any(Tipo.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenNombreAlreadyExists() {
        CreateTipoDTO dto = new CreateTipoDTO();
        dto.setNombre("Libro");
        
        when(tipoRepository.existsByNombreAndUsuarioId(dto.getNombre(), TEST_USER)).thenReturn(true);
        
        ResponseEntity<?> response = tipoController.create(dto);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ya existe un tipo con el nombre 'Libro'", response.getBody());
        verify(tipoRepository, never()).save(any(Tipo.class));
    }

    @Test
    void update_ShouldUpdateTipo_WhenBelongsToUserAndNombreIsUnique() {
        UpdateTipoDTO dto = new UpdateTipoDTO();
        dto.setNombre("Libro Actualizado");
        
        when(tipoRepository.findById("1")).thenReturn(Optional.of(tipo1));
        when(tipoRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), TEST_USER, "1")).thenReturn(false);
        when(tipoRepository.save(any(Tipo.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = tipoController.update("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Tipo updated = (Tipo) response.getBody();
        assertEquals("Libro Actualizado", updated.getNombre());
        verify(tipoRepository, times(1)).save(any(Tipo.class));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenNombreAlreadyExists() {
        UpdateTipoDTO dto = new UpdateTipoDTO();
        dto.setNombre("Artículo");
        
        when(tipoRepository.findById("1")).thenReturn(Optional.of(tipo1));
        when(tipoRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), TEST_USER, "1")).thenReturn(true);
        
        ResponseEntity<?> response = tipoController.update("1", dto);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ya existe un tipo con el nombre 'Artículo'", response.getBody());
        verify(tipoRepository, never()).save(any(Tipo.class));
    }

    @Test
    void delete_ShouldDeleteTipo_WhenNotPredeterminado() {
        when(tipoRepository.findById("2")).thenReturn(Optional.of(tipo2));
        
        ResponseEntity<?> resp = tipoController.delete("2");
        
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(tipoRepository, times(1)).deleteById("2");
    }

    @Test
    void delete_ShouldReturnBadRequest_WhenTipoIsPredeterminado() {
        when(tipoRepository.findById("1")).thenReturn(Optional.of(tipo1));
        
        ResponseEntity<?> resp = tipoController.delete("1");
        
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("No se puede eliminar un tipo predeterminado", resp.getBody());
        verify(tipoRepository, never()).deleteById(any());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenTipoNotBelongsToUser() {
        Tipo tipoDeOtroUsuario = new Tipo();
        tipoDeOtroUsuario.setId("3");
        tipoDeOtroUsuario.setNombre("Otro");
        tipoDeOtroUsuario.setUsuarioId("otheruser");
        
        when(tipoRepository.findById("3")).thenReturn(Optional.of(tipoDeOtroUsuario));
        
        ResponseEntity<?> resp = tipoController.delete("3");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        verify(tipoRepository, never()).deleteById(any());
    }
    
}