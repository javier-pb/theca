/**
 * Descripción: test unitario para la clase JwtUtils.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 20 abr 2026
 * 
 */

package com.theca.backend.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.theca.backend.security.services.UserDetailsImpl;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "d2FwSnR5R2hhbUZ0eUdoYW1GdHlHaGFtRnR5R2hhbUZ0eUdoYW1GdHlHaGFtRnR5R2hhbUZ0eUdoYW1GdHlHaGFt");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String mockUsername = "Javier";
        String token = jwtUtils.generateJwtToken(mockUsername);
        
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void validateJwtToken_ShouldReturnTrue_ForValidToken() {
        String token = jwtUtils.generateJwtToken("Javier");
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_ForInvalidToken() {
        String invalidToken = "Token inválido";
        assertFalse(jwtUtils.validateJwtToken(invalidToken));
    }

    @Test
    void getUserNameFromJwtToken_ShouldReturnUsername() {
        String username = "javier";
        String token = jwtUtils.generateJwtToken(username);
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals(username, extractedUsername);
    }
    
    @Test
    void generateJwtTokenWithUserId_ShouldIncludeUserIdInToken() {
        UserDetailsImpl userDetails = new UserDetailsImpl("1", "javier", "javier@email.com", "password");
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String userId = "123456789";
        
        String token = jwtUtils.generateJwtTokenWithUserId(authentication, userId);
        
        assertNotNull(token);
        String extractedUserId = jwtUtils.getUserIdFromJwtToken(token);
        assertEquals(userId, extractedUserId);
    }
    
    @Test
    void getUserIdFromJwtToken_ShouldReturnUserId() {
        UserDetailsImpl userDetails = new UserDetailsImpl("1", "javier", "javier@email.com", "password");
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String userId = "987654321";
        
        String token = jwtUtils.generateJwtTokenWithUserId(authentication, userId);
        String extractedUserId = jwtUtils.getUserIdFromJwtToken(token);
        
        assertEquals(userId, extractedUserId);
    }
    
    @Test
    void getUserIdFromJwtToken_ShouldReturnNull_WhenUserIdNotPresent() {
        String token = jwtUtils.generateJwtToken("javier");
        String userId = jwtUtils.getUserIdFromJwtToken(token);
        assertNull(userId);
    }
    
}