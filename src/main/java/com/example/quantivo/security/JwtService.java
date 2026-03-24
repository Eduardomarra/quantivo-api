package com.example.quantivo.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JwtService {

	private final String secret;
	private final long expirationHours;

	public JwtService(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-hours}") Long expirationHours) {

		// Validação da chave secreta
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalArgumentException(
					"JWT secret must be at least 32 bytes (256 bits). Current: " +
							(secret != null ? secret.getBytes(StandardCharsets.UTF_8).length : 0)
			);
		}

		this.secret = secret;
		this.expirationHours = expirationHours;
	}

	private long getExpirationMillis() {
		return Duration.ofHours(expirationHours).toMillis();
	}

	private Key getSignKey() {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(String username) {
		long now = System.currentTimeMillis();

		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + getExpirationMillis()))
				.signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public String extractUsername(String token) {
		return Jwts.parser()
				.setSigningKey(getSignKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	public boolean isTokenValid(String token) {
		try {
			Jwts.parser()
					.setSigningKey(getSignKey())
					.build()
					.parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException e) {
		} catch (UnsupportedJwtException e) {
		} catch (MalformedJwtException e) {
		} catch (SignatureException e) {
		} catch (IllegalArgumentException e) {
		}
		return false;
	}
}
