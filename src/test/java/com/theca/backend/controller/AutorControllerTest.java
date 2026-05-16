/**
 * Descripción: test unitario del controlador de la entidad Autor.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.theca.backend.dto.autor.AsociarRecursosDTO;
import com.theca.backend.dto.autor.CreateAutorDTO;
import com.theca.backend.dto.autor.UpdateAutorDTO;
import com.theca.backend.entity.Autor;
import com.theca.backend.entity.Recurso;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.AutorRepository;
import com.theca.backend.repository.RecursoRepository;

@SpringBootTest
public class AutorControllerTest {

    @Autowired
    private AutorController autorController;

    @MockBean
    private AutorRepository autorRepository;

    @MockBean
    private RecursoRepository recursoRepository;

    private Autor autor1;
    private Autor autor2;
    private Recurso recurso1;
    private Recurso recurso2;
    private static final String TEST_USER = "testuser";

    @BeforeEach
    void setUp() {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(TEST_USER);
        
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("userId", TEST_USER);
        when(authentication.getDetails()).thenReturn(detailsMap);
        
        autor1 = new Autor();
        autor1.setId("1");
        autor1.setNombre("Gabriel García Márquez");
        autor1.setFechaModificacion(LocalDateTime.now());
        autor1.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        autor1.setUsuarioId(TEST_USER);

        autor2 = new Autor();
        autor2.setId("2");
        autor2.setNombre("Miguel de Cervantes");
        autor2.setFechaModificacion(LocalDateTime.now());
        autor2.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        autor2.setUsuarioId(TEST_USER);

        recurso1 = new Recurso();
        recurso1.setId("r1");
        recurso1.setTitulo("Recurso 1");
        recurso1.setAutores(new ArrayList<>());

        recurso2 = new Recurso();
        recurso2.setId("r2");
        recurso2.setTitulo("Recurso 2");
        recurso2.setAutores(new ArrayList<>());
    }

    @Test
    void getAll_ShouldReturnListFilteredByUser() {
        when(autorRepository.findByUsuarioId(TEST_USER)).thenReturn(Arrays.asList(autor1, autor2));
        
        List<Autor> result = autorController.getAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(autorRepository, times(1)).findByUsuarioId(TEST_USER);
    }

    @Test
    void getById_ShouldReturnAutor_WhenBelongsToUser() {
        when(autorRepository.findById("1")).thenReturn(Optional.of(autor1));
        
        ResponseEntity<Autor> resp = autorController.getById("1");
        
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Gabriel García Márquez", resp.getBody().getNombre());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenAutorNotBelongsToUser() {
        Autor autorDeOtroUsuario = new Autor();
        autorDeOtroUsuario.setId("3");
        autorDeOtroUsuario.setNombre("Otro Autor");
        autorDeOtroUsuario.setUsuarioId("otheruser");
        
        when(autorRepository.findById("3")).thenReturn(Optional.of(autorDeOtroUsuario));
        
        ResponseEntity<Autor> resp = autorController.getById("3");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void getById_ShouldReturnNotFound_WhenIdDoesNotExist() {
        when(autorRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<Autor> resp = autorController.getById("999");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void create_ShouldSaveAutor_WhenNombreIsUnique() {
        CreateAutorDTO dto = new CreateAutorDTO();
        dto.setNombre("Isabel Allende");
        
        when(autorRepository.existsByNombreAndUsuarioId(dto.getNombre(), TEST_USER)).thenReturn(false);
        when(autorRepository.save(any(Autor.class))).thenAnswer(i -> {
            Autor a = i.getArgument(0);
            a.setId("3");
            return a;
        });
        
        ResponseEntity<?> response = autorController.create(dto);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Autor saved = (Autor) response.getBody();
        assertNotNull(saved);
        assertEquals("Isabel Allende", saved.getNombre());
        assertEquals(TEST_USER, saved.getUsuarioId());
        assertEquals(EstadoSincronizacion.PENDIENTE, saved.getEstadoSincronizacion());
        verify(autorRepository, times(1)).save(any(Autor.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenNombreAlreadyExists() {
        CreateAutorDTO dto = new CreateAutorDTO();
        dto.setNombre("Gabriel García Márquez");
        
        when(autorRepository.existsByNombreAndUsuarioId(dto.getNombre(), TEST_USER)).thenReturn(true);
        
        ResponseEntity<?> response = autorController.create(dto);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ya existe un autor con el nombre 'Gabriel García Márquez'", response.getBody());
        verify(autorRepository, never()).save(any(Autor.class));
    }

    @Test
    void update_ShouldUpdateAutor_WhenBelongsToUserAndNombreIsUnique() {
        UpdateAutorDTO dto = new UpdateAutorDTO();
        dto.setNombre("Gabriel García Márquez (Actualizado)");
        
        when(autorRepository.findById("1")).thenReturn(Optional.of(autor1));
        when(autorRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), TEST_USER, "1")).thenReturn(false);
        when(autorRepository.save(any(Autor.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = autorController.update("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Autor updated = (Autor) response.getBody();
        assertEquals("Gabriel García Márquez (Actualizado)", updated.getNombre());
        verify(autorRepository, times(1)).save(any(Autor.class));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenNombreAlreadyExists() {
        UpdateAutorDTO dto = new UpdateAutorDTO();
        dto.setNombre("Miguel de Cervantes");
        
        when(autorRepository.findById("1")).thenReturn(Optional.of(autor1));
        when(autorRepository.existsByNombreAndUsuarioIdAndIdNot(dto.getNombre(), TEST_USER, "1")).thenReturn(true);
        
        ResponseEntity<?> response = autorController.update("1", dto);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ya existe un autor con el nombre 'Miguel de Cervantes'", response.getBody());
        verify(autorRepository, never()).save(any(Autor.class));
    }

    @Test
    void update_ShouldSkipNombreValidation_WhenNombreNotChanged() {
        UpdateAutorDTO dto = new UpdateAutorDTO();
        dto.setNombre("Gabriel García Márquez");
        
        when(autorRepository.findById("1")).thenReturn(Optional.of(autor1));
        when(autorRepository.save(any(Autor.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = autorController.update("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(autorRepository, never()).existsByNombreAndUsuarioIdAndIdNot(any(), any(), any());
        verify(autorRepository, times(1)).save(any(Autor.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenAutorNotBelongsToUser() {
        UpdateAutorDTO dto = new UpdateAutorDTO();
        dto.setNombre("Actualizado");
        
        Autor autorDeOtroUsuario = new Autor();
        autorDeOtroUsuario.setId("3");
        autorDeOtroUsuario.setNombre("Otro Autor");
        autorDeOtroUsuario.setUsuarioId("otheruser");
        
        when(autorRepository.findById("3")).thenReturn(Optional.of(autorDeOtroUsuario));
        
        ResponseEntity<?> response = autorController.update("3", dto);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(autorRepository, never()).save(any(Autor.class));
    }

    @Test
    void delete_ShouldDeleteAutor_WhenBelongsToUserAndRemoveFromRecursos() {
        List<Recurso> recursosConAutor = Arrays.asList(recurso1, recurso2);
        
        when(autorRepository.findById("1")).thenReturn(Optional.of(autor1));
        when(recursoRepository.findByAutoresId("1")).thenReturn(recursosConAutor);
        
        ResponseEntity<?> resp = autorController.delete("1");
        
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(recursoRepository, times(2)).save(any(Recurso.class));
        verify(autorRepository, times(1)).deleteById("1");
    }

    @Test
    void delete_ShouldReturnNotFound_WhenAutorNotBelongsToUser() {
        Autor autorDeOtroUsuario = new Autor();
        autorDeOtroUsuario.setId("3");
        autorDeOtroUsuario.setNombre("Otro Autor");
        autorDeOtroUsuario.setUsuarioId("otheruser");
        
        when(autorRepository.findById("3")).thenReturn(Optional.of(autorDeOtroUsuario));
        
        ResponseEntity<?> resp = autorController.delete("3");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        verify(autorRepository, never()).deleteById(any());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenIdDoesNotExist() {
        when(autorRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<?> resp = autorController.delete("999");
        
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        verify(autorRepository, never()).deleteById(any());
    }

    @Test
    void getRecursosAsociados_ShouldReturnRecursos_WhenAutorExists() {
        List<Recurso> recursos = Arrays.asList(recurso1, recurso2);
        when(autorRepository.findById("1")).thenReturn(Optional.of(autor1));
        when(recursoRepository.findByAutoresId("1")).thenReturn(recursos);
        
        ResponseEntity<?> response = autorController.getRecursosAsociados("1");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(recursoRepository, times(1)).findByAutoresId("1");
    }

    @Test
    void getRecursosAsociados_ShouldReturnNotFound_WhenAutorNotExists() {
        when(autorRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<?> response = autorController.getRecursosAsociados("999");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void asociarRecursos_ShouldAddAutorToRecursos_WhenAutorExists() {
        AsociarRecursosDTO dto = new AsociarRecursosDTO();
        dto.setRecursosIds(Arrays.asList("r1", "r2"));
        
        when(autorRepository.findById("1")).thenReturn(Optional.of(autor1));
        when(recursoRepository.findById("r1")).thenReturn(Optional.of(recurso1));
        when(recursoRepository.findById("r2")).thenReturn(Optional.of(recurso2));
        when(recursoRepository.save(any(Recurso.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = autorController.asociarRecursos("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(recursoRepository, times(2)).save(any(Recurso.class));
    }

    @Test
    void asociarRecursos_ShouldSkipIfAutorAlreadyInRecurso() {
        AsociarRecursosDTO dto = new AsociarRecursosDTO();
        dto.setRecursosIds(Arrays.asList("r1"));
        
        recurso1.getAutores().add(autor1);
        
        when(autorRepository.findById("1")).thenReturn(Optional.of(autor1));
        when(recursoRepository.findById("r1")).thenReturn(Optional.of(recurso1));
        
        ResponseEntity<?> response = autorController.asociarRecursos("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(recursoRepository, never()).save(any(Recurso.class));
    }

    @Test
    void asociarRecursos_ShouldReturnNotFound_WhenAutorNotExists() {
        AsociarRecursosDTO dto = new AsociarRecursosDTO();
        dto.setRecursosIds(Arrays.asList("r1", "r2"));
        
        when(autorRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<?> response = autorController.asociarRecursos("999", dto);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(recursoRepository, never()).save(any(Recurso.class));
    }

    @Test
    void desasociarRecursos_ShouldRemoveAutorFromRecursos_WhenAutorExists() {
        AsociarRecursosDTO dto = new AsociarRecursosDTO();
        dto.setRecursosIds(Arrays.asList("r1", "r2"));
        
        recurso1.getAutores().add(autor1);
        recurso2.getAutores().add(autor1);
        
        when(autorRepository.findById("1")).thenReturn(Optional.of(autor1));
        when(recursoRepository.findById("r1")).thenReturn(Optional.of(recurso1));
        when(recursoRepository.findById("r2")).thenReturn(Optional.of(recurso2));
        when(recursoRepository.save(any(Recurso.class))).thenAnswer(i -> i.getArgument(0));
        
        ResponseEntity<?> response = autorController.desasociarRecursos("1", dto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(recursoRepository, times(2)).save(any(Recurso.class));
    }

    @Test
    void desasociarRecursos_ShouldReturnNotFound_WhenAutorNotExists() {
        AsociarRecursosDTO dto = new AsociarRecursosDTO();
        dto.setRecursosIds(Arrays.asList("r1", "r2"));
        
        when(autorRepository.findById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<?> response = autorController.desasociarRecursos("999", dto);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(recursoRepository, never()).save(any(Recurso.class));
    }
    
}