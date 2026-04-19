# Theca
Sistema de gestión de colecciones digitales - Proyecto Fin de Ciclo de Desarrollo de Aplicaciones Multiplataforma

## Arquitectura

- **Backend**: Spring Boot + MongoDB (AWS EC2 con Docker)
- **Cliente de escritorio**: JavaFX + SQLite
- **Sincronización bidireccional** con resolución de conflictos por timestamp

## Características principales

- ✅ CRUD completo de recursos, categorías, etiquetas, autores y tipos
- ✅ Funciona sin conexión (base de datos local SQLite)
- ✅ Sincronización automática al recuperar la conexión
- ✅ Autenticación JWT
- ✅ Búsqueda avanzada

## Tecnologías

| Componente | Tecnología |
|------------|------------|
| Backend | Java 17, Spring Boot 3.x, MongoDB |
| Cliente | JavaFX 17, SQLite, Retrofit |
| Infraestructura | AWS EC2, Docker, GitHub |

## Enlaces

- [Memoria completa (PDF)](docs/Memoria.pdf)
- [Documentación técnica](docs/)

## Estado del proyecto

🚧 En desarrollo — Trabajo Fin de Ciclo (DAM)

## Autor

Javier Pérez Báez — IES Virgen de la Paloma  
Tutor: Isidoro Nevares Martín
