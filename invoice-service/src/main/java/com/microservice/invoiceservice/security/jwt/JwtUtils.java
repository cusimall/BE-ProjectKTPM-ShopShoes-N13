package com.microservice.invoiceservice.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${projectjavasneaker.app.jwtSecret}")
    private String jwtSecret;

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Thử lấy roles từ trường "roles" (cấu trúc cũ)
            List<String> roles = (List<String>) claims.get("roles");
            
            // Nếu không có trường "roles", thử lấy từ trường "role" (cấu trúc mới)
            if (roles == null) {
                List<Map<String, String>> rolesList = (List<Map<String, String>>) claims.get("role");
                if (rolesList != null && !rolesList.isEmpty()) {
                    roles = new ArrayList<>();
                    for (Map<String, String> roleMap : rolesList) {
                        String authority = roleMap.get("authority");
                        if (authority != null) {
                            roles.add(authority);
                        }
                    }
                } else {
                    return new ArrayList<>();
                }
            }
            
            return roles;
        } catch (Exception e) {
            logger.error("Cannot get roles from JWT token: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
    
    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
} 