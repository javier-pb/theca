/**
 * Descripción: test unitario para RecursoSearchService.
 * 
 * @author Javier Pérez Báez
 * @version 1.1
 * @date 22 abr 2026
 */

package com.theca.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.theca.backend.dto.recurso.RecursoSearchDTO;
import com.theca.backend.entity.Autor;
import com.theca.backend.entity.Categoria;
import com.theca.backend.entity.Etiqueta;
import com.theca.backend.entity.Recurso;
import com.theca.backend.entity.Tipo;
import com.theca.backend.entity.Usuario;
import com.theca.backend.enums.EstadoSincronizacion;

@ExtendWith(MockitoExtension.class)
class RecursoSearchServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private RecursoSearchService recursoSearchService;

    private Recurso recurso1;
    private Recurso recurso2;
    private List<Recurso> expectedRecursos;
    private Usuario usuarioPrueba;
    
    private Tipo tipo1;
    private Autor autor1;
    private Categoria categoria1;
    private Etiqueta etiqueta1;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId("user1");
        
        tipo1 = new Tipo();
        tipo1.setId("tipo1");
        tipo1.setNombre("PDF");
        
        autor1 = new Autor();
        autor1.setId("autor1");
        autor1.setNombre("García Márquez");
        
        categoria1 = new Categoria();
        categoria1.setId("cat1");
        categoria1.setNombre("Literatura");
        
        etiqueta1 = new Etiqueta();
        etiqueta1.setId("etq1");
        etiqueta1.setNombre("Novela");

        recurso1 = new Recurso();
        recurso1.setId("1");
        recurso1.setTitulo("Cien años de soledad");
        recurso1.setDescripcion("Novela de García Márquez");
        recurso1.setVersion(1.0);
        recurso1.setUsuario(usuarioPrueba);
        recurso1.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        recurso1.setFechaCreacion(LocalDateTime.of(2024, 1, 15, 10, 30));
        recurso1.setTipos(Arrays.asList(tipo1));
        recurso1.setAutores(Arrays.asList(autor1));
        recurso1.setCategorias(Arrays.asList(categoria1));
        recurso1.setEtiquetas(Arrays.asList(etiqueta1));

        recurso2 = new Recurso();
        recurso2.setId("2");
        recurso2.setTitulo("El Quijote");
        recurso2.setDescripcion("Novela de Cervantes");
        recurso2.setVersion(2.0);
        recurso2.setUsuario(usuarioPrueba);
        recurso2.setEstadoSincronizacion(EstadoSincronizacion.SINCRONIZADO);
        recurso2.setFechaCreacion(LocalDateTime.of(2024, 2, 20, 15, 45));

        expectedRecursos = Arrays.asList(recurso1, recurso2);
    }

    @Test
    void search_ShouldReturnAllRecursos_WhenNoFilters() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(expectedRecursos);
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByUsuarioId() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(expectedRecursos);
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldReturnEmptyWhenUsuarioIdDoesNotMatch() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setUsuarioId("user2");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList());
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByTitulo() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setTitulo("Cien");
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cien años de soledad", result.get(0).getTitulo());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByAutorParcial() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setAutor("García");
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByCategoriaParcial() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setCategoria("Literatura");
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(expectedRecursos);
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByEtiquetaParcial() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setEtiqueta("Novela");
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(expectedRecursos);
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByDescripcion() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setDescripcion("García Márquez");
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByTipo() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setTipo("tipo1");
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByVersion() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setVersion(2.0);
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso2));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2.0, result.get(0).getVersion());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByEstadoSincronizacion() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setEstadoSincronizacion(EstadoSincronizacion.SINCRONIZADO);
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso2));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByMultipleAutores() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setAutores(Arrays.asList("autor1"));
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByMultipleCategorias() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setCategorias(Arrays.asList("cat1"));
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByMultipleEtiquetas() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setEtiquetas(Arrays.asList("etq1"));
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByFechaCreacion_EntireDay() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setFechaCreacion(LocalDateTime.of(2024, 1, 15, 0, 0));
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByFechaModificacion_EntireDay() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setFechaModificacion(LocalDateTime.of(2024, 2, 20, 0, 0));
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso2));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldCombineMultipleFilters() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setTitulo("Cien");
        searchDTO.setVersion(1.0);
        searchDTO.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldFilterByTipoWithMultipleTipos() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setTipo("tipo1");
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList(recurso1));
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldReturnEmptyWhenNoMatch() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setTitulo("NoExiste");
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(Arrays.asList());
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }

    @Test
    void search_ShouldHandleNullValuesGracefully() {
        RecursoSearchDTO searchDTO = new RecursoSearchDTO();
        searchDTO.setTitulo(null);
        searchDTO.setAutor(null);
        searchDTO.setTipo(null);
        searchDTO.setVersion(null);
        searchDTO.setDescripcion(null);
        searchDTO.setUsuarioId("user1");
        
        when(mongoTemplate.find(any(Query.class), eq(Recurso.class))).thenReturn(expectedRecursos);
        
        List<Recurso> result = recursoSearchService.search(searchDTO);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Recurso.class));
    }
    
}