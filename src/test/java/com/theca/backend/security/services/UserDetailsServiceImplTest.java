/**
 * Descripción: test unitario para la clase UserDetailsServiceImpl.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 20 abr 2026
 * 
 */

package com.theca.backend.security.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.theca.backend.entity.Usuario;
import com.theca.backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    // Test para verificar que se carga el usuario por nombre:
    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExistsByNombre() {
        Usuario usuario = new Usuario();
        usuario.setId("1");
        usuario.setNombre("Javier");
        usuario.setCorreo("javier@email.com");
        usuario.setContrasena("hash123");
        
        when(usuarioRepository.findByNombre("Javier")).thenReturn(Optional.of(usuario));

        UserDetails result = userDetailsService.loadUserByUsername("Javier");

        assertNotNull(result);
        assertEquals("Javier", result.getUsername());
        assertEquals("hash123", result.getPassword());
        verify(usuarioRepository, times(1)).findByNombre("Javier");
        verify(usuarioRepository, never()).findByCorreo(anyString());
    }

    // Test para verificar que se busca por correo si no se encuentra por nombre:
    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExistsByCorreo() {
        Usuario usuario = new Usuario();
        usuario.setId("1");
        usuario.setNombre("Javier");
        usuario.setCorreo("javier@email.com");
        usuario.setContrasena("hash123");
        
        when(usuarioRepository.findByNombre("javier@email.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreo("javier@email.com")).thenReturn(Optional.of(usuario));

        UserDetails result = userDetailsService.loadUserByUsername("javier@email.com");

        assertNotNull(result);
        assertEquals("Javier", result.getUsername());
        assertEquals("hash123", result.getPassword());
        verify(usuarioRepository, times(1)).findByNombre("javier@email.com");
        verify(usuarioRepository, times(1)).findByCorreo("javier@email.com");
    }

    // Test para verificar que se lanza la excepción cuando el usuario no existe ni por nombre ni por correo:
    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        when(usuarioRepository.findByNombre("inexistente")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreo("inexistente")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("inexistente");
        });
        
        verify(usuarioRepository, times(1)).findByNombre("inexistente");
        verify(usuarioRepository, times(1)).findByCorreo("inexistente");
    }
    
}
