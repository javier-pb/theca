/**
 * Descripción: Servicio para la búsqueda avanzada (consulta dinámica) de recursos en la base de datos MongoDB.
 * 
 * @author Javier Pérez Báez
 * @version 1.1
 * @date 22 abr 2026
 */

package com.theca.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.theca.backend.dto.recurso.RecursoSearchDTO;
import com.theca.backend.entity.Recurso;

@Service
public class RecursoSearchService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Recurso> search(RecursoSearchDTO searchDTO) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (searchDTO.getUsuarioId() != null && !searchDTO.getUsuarioId().isEmpty()) {
            criteriaList.add(Criteria.where("usuario.id").is(searchDTO.getUsuarioId()));
        }

        // BÚSQUEDAS PARA LA BARRA DE BÚSQUEDA:
        if (searchDTO.getTitulo() != null && !searchDTO.getTitulo().isEmpty()) {
            criteriaList.add(Criteria.where("titulo").regex(searchDTO.getTitulo(), "i"));
        }

        if (searchDTO.getAutor() != null && !searchDTO.getAutor().isEmpty()) {
            criteriaList.add(Criteria.where("autores.nombre").regex(searchDTO.getAutor(), "i"));
        }
        
        if (searchDTO.getCategoria() != null && !searchDTO.getCategoria().isEmpty()) {
            criteriaList.add(Criteria.where("categorias.nombre").regex(searchDTO.getCategoria(), "i"));
        }
        
        if (searchDTO.getEtiqueta() != null && !searchDTO.getEtiqueta().isEmpty()) {
            criteriaList.add(Criteria.where("etiquetas.nombre").regex(searchDTO.getEtiqueta(), "i"));
        }
        
        
        // BÚSQUEDAS PARA LA BÚSQUEDA AVANZADA:
        if (searchDTO.getAutores() != null && !searchDTO.getAutores().isEmpty()) {
            criteriaList.add(Criteria.where("autores.id").in(searchDTO.getAutores()));
        }

        if (searchDTO.getTipo() != null && !searchDTO.getTipo().isEmpty()) {
            criteriaList.add(Criteria.where("tipos.id").is(searchDTO.getTipo()));
        }

        if (searchDTO.getVersion() != null) {
            criteriaList.add(Criteria.where("version").is(searchDTO.getVersion()));
        }

        if (searchDTO.getDescripcion() != null && !searchDTO.getDescripcion().isEmpty()) {
            criteriaList.add(Criteria.where("descripcion").regex(searchDTO.getDescripcion(), "i"));
        }
        
        if (searchDTO.getEstadoSincronizacion() != null) {
            criteriaList.add(Criteria.where("estadoSincronizacion").is(searchDTO.getEstadoSincronizacion()));
        }

        if (searchDTO.getCategorias() != null && !searchDTO.getCategorias().isEmpty()) {
            criteriaList.add(Criteria.where("categorias.id").in(searchDTO.getCategorias()));
        }

        if (searchDTO.getEtiquetas() != null && !searchDTO.getEtiquetas().isEmpty()) {
            criteriaList.add(Criteria.where("etiquetas.id").in(searchDTO.getEtiquetas()));
        }
        
        if (searchDTO.getFechaCreacion() != null) {
            LocalDate fecha = searchDTO.getFechaCreacion().toLocalDate();
            LocalDateTime startOfDay = fecha.atStartOfDay();
            LocalDateTime endOfDay = fecha.atTime(LocalTime.MAX);
            criteriaList.add(Criteria.where("fechaCreacion").gte(startOfDay).lte(endOfDay));
        }
        
        if (searchDTO.getFechaModificacion() != null) {
            LocalDate fecha = searchDTO.getFechaModificacion().toLocalDate();
            LocalDateTime startOfDay = fecha.atStartOfDay();
            LocalDateTime endOfDay = fecha.atTime(LocalTime.MAX);
            criteriaList.add(Criteria.where("fechaModificacion").gte(startOfDay).lte(endOfDay));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        query.collation(Collation.of("es").strength(1));

        return mongoTemplate.find(query, Recurso.class);
    }
    
}