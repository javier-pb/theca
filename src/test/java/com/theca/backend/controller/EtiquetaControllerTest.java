/**
 * Descripción: test unitario del controlador de la entidad Etiqueta.
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

import com.theca.backend.dto.etiqueta.CreateEtiquetaDTO;
import com.theca.backend.dto.etiqueta.UpdateEtiquetaDTO;
import com.theca.backend.entity.Etiqueta;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.EtiquetaRepository;

@ExtendWith(MockitoExtension.class)
public class EtiquetaControllerTest {

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private EtiquetaController etiquetaController;

    private Etiqueta e1;
    private Etiqueta e2;
    private static final String TEST_USER = "testuser";

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(TEST_USER);

        e1 = new Etiqueta();
        e1.setId("1");
        e1.setNombre("Java");
        e1.setFechaModificacion(LocalDateTime.now());
        e1.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        e1.setUsuarioId(TEST_USER);

        e2 = new Etiqueta();
        e2.setId("2");
        e2.setNombre("Spring Boot");
        e2.setFechaModificacion(LocalDateTime.now());
        e2.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        e2.setUsuarioId(TEST_USER);
    }

    @Test
    void getAll_ShouldReturnListFilteredByUser() {
        when(etiquetaRepository.findByUsuarioId(TEST_USER)).thenReturn(Arrays.asList(e1, e2));
        
        List<Etiqueta> result = etiquetaController.getAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(etiquetaRepository, times(1)).findByUsuarioId(TEST_USER);
    }

    @Test
    void getById_ShouldReturnEtiqueta_WhenBelongsToUser() {
        when(etiquetaRepository.findById("1")).thenReturn(Optional.of(e1));
        
        ResponseEntity<Etiqueta> resp = etiquetaController.getById("1");
        
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Java", resp.getBody().getNombre());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenEtiquetaNotBelongsToUser() {
        Etiqueta etiquetaDeOtroUsuario = new Etiqueta();
        etiquetaDeOtroUsuario.setId("3");
        etiquetaDeOtroUsuario.setNombre("Python");
        etiquetaDeOtroUsuario.setUsuarioId("otheruser");
        
        when(etiquetaRepository.findById("3")).thenReturn(Optional.of(etiquetaDeOtroUsuario));
        
        ResponseEntity<Etiqueta> resp = etiquetaController.getById("3");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenIdDoesNotExist() {
        when(etiquetaRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<Etiqueta> resp = etiquetaController.getById("999");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void create_ShouldSaveEtiqueta_WhenNombreIsUnique() {
        CreateEtiquetaDTO dto = new CreateEtiquetaDTO();
        dto.setNombre("Nueva Etiqueta");
        
        when(etiquetaRepository.existsByNombreAndUsuarioId(dto.getNombre(), TEST_USER)).thenReturn(false);
        when(etiquetaRepository.save(any(Etiqueta.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = etiquetaController.create(dto);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Etiqueta saved = (Etiqueta) response.getBody();
        assertNotNull(saved.getFechaModificacion());
        assertEquals("Nueva Etiqueta", saved.getNombre());
        assertEquals(TEST_USER, saved.getUsuarioId());
        assertEquals(EstadoSincronizacion.PENDIENTE, saved.getEstadoSincronizacion());
        verify(etiquetaRepository, times(1)).save(any(Etiqueta.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenNombreAlreadyExists() {
        CreateEtiquetaDTO dto = new CreateEtiquetaDTO();
        dto.setNombre("Java");
        
        when(etiquetaRepository.existsByNombreAndUsuarioId(dto.getNombre(), TEST_USER)).thenReturn(true);
        
        ResponseEntity<?> response = etiquetaController.create(dto);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ya existe una etiqueta con el nombre 'Java'", response.getBody());
        verify(etiquetaRepository, never()).save(any(Etiqueta.class));
    }

    @Test
    void update_ShouldUpdateEtiqueta_WhenBelongsToUserAndNombreIsUnique() {
        UpdateEtiquetaDTO dto = new UpdateEtiquetaDTO();
        dto.setNombre("JavaScript");
        
        when(etiquetaRepository.findById("1")).thenReturn(Optional.of(e1));
        when(etiquetaRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), TEST_USER, "1")).thenReturn(false);
        when(etiquetaRepository.save(any(Etiqueta.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = etiquetaController.update("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Etiqueta updated = (Etiqueta) response.getBody();
        assertEquals("JavaScript", updated.getNombre());
        verify(etiquetaRepository, times(1)).save(any(Etiqueta.class));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenNombreAlreadyExists() {
        UpdateEtiquetaDTO dto = new UpdateEtiquetaDTO();
        dto.setNombre("Spring Boot");
        
        when(etiquetaRepository.findById("1")).thenReturn(Optional.of(e1));
        when(etiquetaRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), TEST_USER, "1")).thenReturn(true);
        
        ResponseEntity<?> response = etiquetaController.update("1", dto);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ya existe una etiqueta con el nombre 'Spring Boot'", response.getBody());
        verify(etiquetaRepository, never()).save(any(Etiqueta.class));
    }

    @Test
    void update_ShouldSkipNombreValidation_WhenNombreNotChanged() {
        UpdateEtiquetaDTO dto = new UpdateEtiquetaDTO();
        dto.setNombre("Java");
        
        when(etiquetaRepository.findById("1")).thenReturn(Optional.of(e1));
        when(etiquetaRepository.save(any(Etiqueta.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = etiquetaController.update("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(etiquetaRepository, never()).existsByNombreAndUsuarioIdAndIdNot(any(), any(), any());
        verify(etiquetaRepository, times(1)).save(any(Etiqueta.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenEtiquetaNotBelongsToUser() {
        UpdateEtiquetaDTO dto = new UpdateEtiquetaDTO();
        dto.setNombre("Actualizada");
        
        Etiqueta etiquetaDeOtroUsuario = new Etiqueta();
        etiquetaDeOtroUsuario.setId("3");
        etiquetaDeOtroUsuario.setNombre("Python");
        etiquetaDeOtroUsuario.setUsuarioId("otheruser");
        
        when(etiquetaRepository.findById("3")).thenReturn(Optional.of(etiquetaDeOtroUsuario));
        
        ResponseEntity<?> response = etiquetaController.update("3", dto);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(etiquetaRepository, never()).save(any(Etiqueta.class));
    }

    @Test
    void delete_ShouldDeleteEtiqueta_WhenBelongsToUser() {
        when(etiquetaRepository.findById("1")).thenReturn(Optional.of(e1));
        
        ResponseEntity<Void> resp = etiquetaController.delete("1");
        
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(etiquetaRepository, times(1)).deleteById("1");
    }

    @Test
    void delete_ShouldReturnNotFound_WhenEtiquetaNotBelongsToUser() {
        Etiqueta etiquetaDeOtroUsuario = new Etiqueta();
        etiquetaDeOtroUsuario.setId("3");
        etiquetaDeOtroUsuario.setNombre("Python");
        etiquetaDeOtroUsuario.setUsuarioId("otheruser");
        
        when(etiquetaRepository.findById("3")).thenReturn(Optional.of(etiquetaDeOtroUsuario));
        
        ResponseEntity<Void> resp = etiquetaController.delete("3");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        verify(etiquetaRepository, never()).deleteById(any());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenIdDoesNotExist() {
        when(etiquetaRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<Void> resp = etiquetaController.delete("999");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        verify(etiquetaRepository, never()).deleteById(any());
    }

    @Test
    void getRecursosAsociados_ShouldReturnEmptyList_WhenEtiquetaExists() {
        when(etiquetaRepository.findById("1")).thenReturn(Optional.of(e1));
        
        ResponseEntity<?> response = etiquetaController.getRecursosAsociados("1");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getRecursosAsociados_ShouldReturnNotFound_WhenEtiquetaNotExists() {
        when(etiquetaRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<?> response = etiquetaController.getRecursosAsociados("999");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}