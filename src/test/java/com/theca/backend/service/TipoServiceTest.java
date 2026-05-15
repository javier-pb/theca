/**
 * Descripción: test unitario para el servicio TipoService.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 15 may 2026
 */

package com.theca.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theca.backend.entity.Tipo;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.TipoRepository;

@ExtendWith(MockitoExtension.class)
class TipoServiceTest {

    @Mock
    private TipoRepository tipoRepository;

    @InjectMocks
    private TipoService tipoService;

    private static final String USUARIO_ID = "usuario123";

    @Test
    void crearTiposPredeterminados_ShouldCreateFiveTypes() {
        tipoService.crearTiposPredeterminados(USUARIO_ID);

        verify(tipoRepository, times(5)).save(any(Tipo.class));
    }

    @Test
    void crearTiposPredeterminados_ShouldCreateCorrectTypesWithNames() {
        ArgumentCaptor<Tipo> tipoCaptor = ArgumentCaptor.forClass(Tipo.class);

        tipoService.crearTiposPredeterminados(USUARIO_ID);

        verify(tipoRepository, times(5)).save(tipoCaptor.capture());
        
        java.util.List<Tipo> tiposGuardados = tipoCaptor.getAllValues();
        
        assertEquals(5, tiposGuardados.size());
        
        java.util.List<String> nombres = tiposGuardados.stream()
                .map(Tipo::getNombre)
                .toList();
        
        assertTrue(nombres.contains("PDF"));
        assertTrue(nombres.contains("Hoja de cálculo"));
        assertTrue(nombres.contains("Documento"));
        assertTrue(nombres.contains("Enlace"));
        assertTrue(nombres.contains("ePub"));
    }

    @Test
    void crearTiposPredeterminados_ShouldSetUsuarioIdCorrectly() {
        ArgumentCaptor<Tipo> tipoCaptor = ArgumentCaptor.forClass(Tipo.class);

        tipoService.crearTiposPredeterminados(USUARIO_ID);

        verify(tipoRepository, times(5)).save(tipoCaptor.capture());
        
        for (Tipo tipo : tipoCaptor.getAllValues()) {
            assertEquals(USUARIO_ID, tipo.getUsuarioId());
        }
    }

    @Test
    void crearTiposPredeterminados_ShouldSetEsPredeterminadoTrue() {
        ArgumentCaptor<Tipo> tipoCaptor = ArgumentCaptor.forClass(Tipo.class);

        tipoService.crearTiposPredeterminados(USUARIO_ID);

        verify(tipoRepository, times(5)).save(tipoCaptor.capture());
        
        for (Tipo tipo : tipoCaptor.getAllValues()) {
            assertTrue(tipo.isEsPredeterminado());
        }
    }

    @Test
    void crearTiposPredeterminados_ShouldSetEstadoSincronizacionPendiente() {
        ArgumentCaptor<Tipo> tipoCaptor = ArgumentCaptor.forClass(Tipo.class);

        tipoService.crearTiposPredeterminados(USUARIO_ID);

        verify(tipoRepository, times(5)).save(tipoCaptor.capture());
        
        for (Tipo tipo : tipoCaptor.getAllValues()) {
            assertEquals(EstadoSincronizacion.PENDIENTE, tipo.getEstadoSincronizacion());
        }
    }

    @Test
    void crearTiposPredeterminados_ShouldSetFechaModificacion() {
        ArgumentCaptor<Tipo> tipoCaptor = ArgumentCaptor.forClass(Tipo.class);

        tipoService.crearTiposPredeterminados(USUARIO_ID);

        verify(tipoRepository, times(5)).save(tipoCaptor.capture());
        
        for (Tipo tipo : tipoCaptor.getAllValues()) {
            assertNotNull(tipo.getFechaModificacion());
        }
    }

    @Test
    void crearTiposPredeterminados_ShouldNotSetImagen() {
        ArgumentCaptor<Tipo> tipoCaptor = ArgumentCaptor.forClass(Tipo.class);

        tipoService.crearTiposPredeterminados(USUARIO_ID);

        verify(tipoRepository, times(5)).save(tipoCaptor.capture());
        
        for (Tipo tipo : tipoCaptor.getAllValues()) {
            assertEquals(null, tipo.getImagen());
        }
    }
    
}