/**
 * Descripción: test unitario para la clase AuthController.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 20 abr 2026
 * 
 */

package com.theca.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.theca.backend.dto.login.LoginRequestDTO;
import com.theca.backend.dto.login.LoginResponseDTO;
import com.theca.backend.dto.usuario.CreateUsuarioDTO;
import com.theca.backend.entity.Usuario;
import com.theca.backend.repository.UsuarioRepository;
import com.theca.backend.security.jwt.JwtUtils;
import com.theca.backend.security.services.UserDetailsImpl;
import com.theca.backend.service.TipoService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private TipoService tipoService;

    @InjectMocks
    private AuthController authController;

    @Test
    void authenticateUser_ShouldReturnToken_WhenCredentialsAreValid() {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("Javier");
        loginRequest.setPassword("123456");
        
        UserDetailsImpl userDetails = new UserDetailsImpl("1", "Javier", "javier@email.com", "hash123");
        Usuario usuario = new Usuario();
        usuario.setId("userObjectId123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepository.findByNombre("Javier")).thenReturn(Optional.of(usuario));
        when(jwtUtils.generateJwtTokenWithUserId(authentication, "userObjectId123")).thenReturn("Token JWT falso");

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof LoginResponseDTO);
        LoginResponseDTO loginResponse = (LoginResponseDTO) response.getBody();
        assertEquals("Token JWT falso", loginResponse.getToken());
    }

    @Test
    void registerUser_ShouldReturnOk_WhenUserIsNew() {
        CreateUsuarioDTO createDTO = new CreateUsuarioDTO();
        createDTO.setNombre("Nuevo");
        createDTO.setCorreo("nuevo@email.com");
        createDTO.setContrasena("password");

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setId("123");

        when(usuarioRepository.existsByNombre("Nuevo")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("nuevo@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        ResponseEntity<?> response = authController.registerUser(createDTO);

        assertEquals(201, response.getStatusCode().value());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(tipoService, times(1)).crearTiposPredeterminados("123");
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenUsernameExists() {
        CreateUsuarioDTO createDTO = new CreateUsuarioDTO();
        createDTO.setNombre("Existente");
        createDTO.setCorreo("nuevo@email.com");

        when(usuarioRepository.existsByNombre("Existente")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(createDTO);

        assertEquals(400, response.getStatusCode().value());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(tipoService, never()).crearTiposPredeterminados(any());
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenEmailExists() {
        CreateUsuarioDTO createDTO = new CreateUsuarioDTO();
        createDTO.setNombre("Nuevo");
        createDTO.setCorreo("existente@email.com");

        when(usuarioRepository.existsByNombre("Nuevo")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("existente@email.com")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(createDTO);

        assertEquals(400, response.getStatusCode().value());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(tipoService, never()).crearTiposPredeterminados(any());
    }
    
}