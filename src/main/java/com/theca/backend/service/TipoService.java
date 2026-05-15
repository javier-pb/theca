/**
 * Descripción: Servicio para la gestión de tipos de recursos.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 15 may 2026
 */

package com.theca.backend.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.theca.backend.entity.Tipo;
import com.theca.backend.enums.EstadoSincronizacion;
import com.theca.backend.repository.TipoRepository;

@Service
public class TipoService {

    @Autowired
    private TipoRepository tipoRepository;

    // Tipos predeterminados con sus imágenes en base64 o ruta:
    private static final List<TipoPredeterminado> TIPOS_PREDETERMINADOS = Arrays.asList(
        new TipoPredeterminado("PDF", "PDF.png"),
        new TipoPredeterminado("Hoja de cálculo", "Hoja de cálculo.png"),
        new TipoPredeterminado("Documento", "Documento.png"),
        new TipoPredeterminado("Enlace", "Enlace.png"),
        new TipoPredeterminado("ePub", "ePub.png")
    );

    // Método para crear los tipos predeterminados para un nuevo usuario:
    public void crearTiposPredeterminados(String usuarioId) {
        for (TipoPredeterminado tipoPred : TIPOS_PREDETERMINADOS) {
            Tipo tipo = new Tipo();
            tipo.setNombre(tipoPred.nombre);
            tipo.setImagen(null); // La imagen se cargará desde el frontend o assets
            tipo.setUsuarioId(usuarioId);
            tipo.setFechaModificacion(LocalDateTime.now());
            tipo.setEstadoSincronizacion(EstadoSincronizacion.PENDIENTE);
            tipo.setEsPredeterminado(true);
            
            tipoRepository.save(tipo);
        }
    }

    // Clase interna para los datos de tipos predeterminados:
    private static class TipoPredeterminado {
        String nombre;
        String nombreImagen;
        
        TipoPredeterminado(String nombre, String nombreImagen) {
            this.nombre = nombre;
            this.nombreImagen = nombreImagen;
        }
    }
    
}