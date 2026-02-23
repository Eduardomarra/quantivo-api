package com.example.quantivo.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JwtService {

	private final String secret;
	private final long expirationHours;

	public JwtService(Dotenv dotenv) {
		this.secret = dotenv.get("JWT_SECRET");
		this.expirationHours =
				Long.parseLong(dotenv.get("JWT_EXPIRATION_HOURS"));
	}

	private long getExpirationMillis() {
		return Duration.ofHours(expirationHours).toMillis();
	}

	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(String username) {

		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(new Date())
				.setExpiration(
						new Date(System.currentTimeMillis() + getExpirationMillis())
				)
				.signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public String extractUsername(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSignKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	public boolean isTokenValid(String token) {
		try {
			Jwts.parserBuilder()
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
