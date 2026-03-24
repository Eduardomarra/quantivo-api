package com.example.quantivo.unit.utils;

import com.example.quantivo.security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do JwtService")
public class JwtServiceTest {

	private JwtService jwtService;

	private final String SECRET_KEY = "aB3dE5fG7hJ9kL1mN2oP4qR6sT8uV0wX"; // 32 bytes
	private final Long EXPIRATION_HOURS = 24L;
	private final String USERNAME = "usuario@teste.com";

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(SECRET_KEY, EXPIRATION_HOURS);
	}

	@Test
	@DisplayName("Deve gerar token JWT válido")
	void deveGerarTokenValido() {
		String token = jwtService.generateToken(USERNAME);

		assertThat(token).isNotNull();
		assertThat(token).isNotEmpty();
		assertThat(token.split("\\.")).hasSize(3); // Verifica formato JWT
	}

	@Test
	@DisplayName("Deve extrair username corretamente do token")
	void deveExtrairUsernameCorretamente() {
		String token = jwtService.generateToken(USERNAME);
		String extractedUsername = jwtService.extractUsername(token);
		assertThat(extractedUsername).isEqualTo(USERNAME);
	}

	@Test
	@DisplayName("Deve gerar tokens com timestamps diferentes")
	void deveGerarTokensComTimestampsDiferentes() throws InterruptedException {
		String token1 = jwtService.generateToken(USERNAME);

		// Aguarda 2 segundos para garantir que o timestamp de segundos mude
		Thread.sleep(2000);

		String token2 = jwtService.generateToken(USERNAME);

		// Extrai as claims para comparar timestamps
		var claims1 = Jwts.parser()
				.setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
				.build()
				.parseClaimsJws(token1)
				.getBody();

		var claims2 = Jwts.parser()
				.setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
				.build()
				.parseClaimsJws(token2)
				.getBody();

		// Verifica se os timestamps são diferentes (em segundos)
		assertThat(claims2.getIssuedAt().getTime() / 1000)
				.isGreaterThan(claims1.getIssuedAt().getTime() / 1000);
	}

	@Test
	@DisplayName("Deve gerar tokens válidos para o mesmo usuário")
	void deveGerarTokensValidos() {
		String token1 = jwtService.generateToken(USERNAME);
		String token2 = jwtService.generateToken(USERNAME);

		assertThat(token1).isNotNull();
		assertThat(token2).isNotNull();
		assertThat(jwtService.isTokenValid(token1)).isTrue();
		assertThat(jwtService.isTokenValid(token2)).isTrue();
		assertThat(jwtService.extractUsername(token1)).isEqualTo(USERNAME);
		assertThat(jwtService.extractUsername(token2)).isEqualTo(USERNAME);
	}

	@Test
	@DisplayName("Deve gerar tokens válidos e extrair username corretamente")
	void deveGerarTokensValidosEExtrairUsername() {
		// Act
		String token1 = jwtService.generateToken(USERNAME);
		String token2 = jwtService.generateToken(USERNAME);

		// Assert - ambos devem ser válidos
		assertThat(jwtService.isTokenValid(token1)).isTrue();
		assertThat(jwtService.isTokenValid(token2)).isTrue();

		// Ambos devem extrair o mesmo username
		assertThat(jwtService.extractUsername(token1)).isEqualTo(USERNAME);
		assertThat(jwtService.extractUsername(token2)).isEqualTo(USERNAME);
	}

	@Test
	@DisplayName("Deve validar token válido corretamente")
	void deveValidarTokenValido() {
		String token = jwtService.generateToken(USERNAME);
		boolean isValid = jwtService.isTokenValid(token);
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Deve rejeitar token com formato inválido")
	void deveRejeitarTokenFormatoInvalido() {
		String tokenInvalido = "token.invalido.format";
		boolean isValid = jwtService.isTokenValid(tokenInvalido);
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("Deve rejeitar token vazio")
	void deveRejeitarTokenVazio() {
		boolean isValid = jwtService.isTokenValid("");
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("Deve rejeitar token nulo")
	void deveRejeitarTokenNulo() {
		boolean isValid = jwtService.isTokenValid(null);
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("Deve reconhecer token expirado")
	void deveReconhecerTokenExpirado() throws InterruptedException {
		JwtService jwtServiceCurto = new JwtService(SECRET_KEY, 0L); // Expira imediatamente
		String token = jwtServiceCurto.generateToken(USERNAME);

		// Aguarda um pouco para garantir expiração
		Thread.sleep(100);

		boolean isValid = jwtServiceCurto.isTokenValid(token);

		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("Deve lançar exceção ao extrair username de token expirado")
	void deveLancarExcecaoAoExtrairUsernameDeTokenExpirado() throws InterruptedException {
		JwtService jwtServiceCurto = new JwtService(SECRET_KEY, 0L);
		String token = jwtServiceCurto.generateToken(USERNAME);

		// Aguarda um pouco para garantir expiração
		Thread.sleep(100);

		assertThatThrownBy(() -> jwtServiceCurto.extractUsername(token))
				.isInstanceOf(ExpiredJwtException.class);
	}

	@Test
	@DisplayName("Deve aceitar chave secreta de 256 bits (32 caracteres)")
	void deveAceitarChaveSecreta256Bits() {
		String chave256Bits = "aB3dE5fG7hJ9kL1mN2oP4qR6sT8uV0wX"; // 32 caracteres

		JwtService service = new JwtService(chave256Bits, EXPIRATION_HOURS);
		String token = service.generateToken(USERNAME);

		assertThat(token).isNotNull();
	}

	@Test
	@DisplayName("Deve lançar exceção com chave secreta muito curta")
	void deveLancarExcecaoComChaveSecretaMuitoCurta() {
		String chaveCurta = "chave-curta";

		assertThatThrownBy(() -> new JwtService(chaveCurta, EXPIRATION_HOURS))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("Deve aceitar chave secreta de 512 bits (64 caracteres) para HS512")
	void deveAceitarChaveSecreta512Bits() {
		String chave512Bits = "aB3dE5fG7hJ9kL1mN2oP4qR6sT8uV0wXaB3dE5fG7hJ9kL1mN2oP4qR6sT8uV0wX"; // 64 caracteres

		JwtService service = new JwtService(chave512Bits, EXPIRATION_HOURS);
		String token = service.generateToken(USERNAME);

		assertThat(token).isNotNull();
	}

	@Test
	@DisplayName("Deve conter subject correto no token")
	void deveConterSubjectCorreto() {
		String token = jwtService.generateToken(USERNAME);
		String subject = jwtService.extractUsername(token);

		assertThat(subject).isEqualTo(USERNAME);
	}

	@Test
	@DisplayName("Deve conter issuedAt no token")
	void deveConterIssuedAt() {
		String token = jwtService.generateToken(USERNAME);

		// Verifica se o token pode ser parseado e tem issuedAt
		var claims = Jwts.parser()
				.setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
				.build()
				.parseClaimsJws(token)
				.getBody();

		assertThat(claims.getIssuedAt()).isNotNull();
		assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
	}

	@Test
	@DisplayName("Deve conter expiration correta no token")
	void deveConterExpirationCorreta() {
		long beforeCreation = System.currentTimeMillis();
		String token = jwtService.generateToken(USERNAME);
		long afterCreation = System.currentTimeMillis();

		var claims = Jwts.parser()
				.setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
				.build()
				.parseClaimsJws(token)
				.getBody();

		Date expiration = claims.getExpiration();
		assertThat(expiration).isNotNull();

		// Verifica se a expiração está no tempo correto (após a criação)
		long expectedExpiration = beforeCreation + Duration.ofHours(EXPIRATION_HOURS).toMillis();
		assertThat(expiration.getTime()).isGreaterThanOrEqualTo(expectedExpiration - 1000);
		assertThat(expiration.getTime()).isLessThanOrEqualTo(afterCreation + Duration.ofHours(EXPIRATION_HOURS).toMillis());
	}

	@Test
	@DisplayName("Deve extrair username mesmo com token válido")
	void deveExtrairUsernameComTokenValido() {
		String token = jwtService.generateToken(USERNAME);

		String username = jwtService.extractUsername(token);

		assertThat(username).isEqualTo(USERNAME);
	}

	@Test
	@DisplayName("Deve lançar exceção ao extrair username de token malformado")
	void deveLancarExcecaoAoExtrairUsernameDeTokenMalformado() {
		String tokenMalformado = "eyJhbGciOiJIUzI1NiJ9.invalido";

		assertThatThrownBy(() -> jwtService.extractUsername(tokenMalformado))
				.isInstanceOf(Exception.class);
	}

	@Test
	@DisplayName("Deve respeitar horas de expiração configuradas")
	void deveRespeitarHorasExpiracao() {
		Long horasExpiracao = 2L;
		JwtService jwtServiceCustom = new JwtService(SECRET_KEY, horasExpiracao);

		String token = jwtServiceCustom.generateToken(USERNAME);

		var claims = Jwts.parser()
				.setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
				.build()
				.parseClaimsJws(token)
				.getBody();

		long expectedMillis = Duration.ofHours(horasExpiracao).toMillis();
		long actualMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

		assertThat(actualMillis).isBetween(expectedMillis - 1000, expectedMillis + 1000);
	}

	@Test
	@DisplayName("Deve aceitar expiração zero (token expira imediatamente)")
	void deveAceitarExpiracaoZero() {
		JwtService jwtServiceZero = new JwtService(SECRET_KEY, 0L);

		String token = jwtServiceZero.generateToken(USERNAME);

		//O token ainda é gerado, mas expira imediatamente
		assertThat(token).isNotNull();
		assertThat(jwtServiceZero.isTokenValid(token)).isFalse();
	}

	@Test
	@DisplayName("Deve lançar exceção com chave secreta nula")
	void deveLancarExcecaoComChaveSecretaNula() {
		assertThatThrownBy(() -> new JwtService(null, EXPIRATION_HOURS))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("Deve lançar exceção com chave secreta vazia")
	void deveLancarExcecaoComChaveSecretaVazia() {
		assertThatThrownBy(() -> new JwtService("", EXPIRATION_HOURS))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
