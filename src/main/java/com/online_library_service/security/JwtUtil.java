package com.online_library_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private Long expiration;

	private Key secretKey;

	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(String email, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);

		return Jwts.builder().setClaims(claims).setSubject(email).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(secretKey, SignatureAlgorithm.HS256).compact();
	}

	public String generateResetToken(String email) {
		return Jwts.builder().setSubject(email).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 min expiry
				.signWith(secretKey, SignatureAlgorithm.HS256).compact();
	}

	public Claims extractClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
	}

	public String extractEmail(String token) {
		return extractClaims(token).getSubject();
	}

	public String extractRole(String token) {
		return (String) extractClaims(token).get("role");
	}

	public boolean validateToken(String token) {
		try {
			Claims claims = extractClaims(token);
			return !claims.getExpiration().before(new Date());
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public String extractUsername(String token) {
		return extractEmail(token);
	}

	public boolean isTokenValid(String token, String email) {
		Claims claims = extractClaims(token);
		return claims.getSubject().equals(email) && !claims.getExpiration().before(new Date());
	}
}
