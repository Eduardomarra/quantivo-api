package com.example.quantivo.integration.controller;

import com.example.quantivo.controller.AuthController;
import com.example.quantivo.entity.Usuario;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.security.JwtService;
import com.example.quantivo.to.LoginRequestTO;
import com.example.quantivo.to.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AuthController")
class AuthControllerTest {

	@Mock
	private AuthenticationManager authManager;

	@Mock
	private JwtService jwtService;

	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private AuthController authController;

	private LoginRequestTO loginRequest;
	private Usuario usuario;
	private String email;
	private String senha;
	private String token;
	private UUID usuarioId;

	@BeforeEach
	void setUp() {
		email = "usuario@teste.com";
		senha = "senha123";
		token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQHRlc3RlLmNvbSJ9...";
		usuarioId = UUID.randomUUID();

		loginRequest = new LoginRequestTO();
		loginRequest.setEmail(email);
		loginRequest.setSenha(senha);

		usuario = new Usuario();
		usuario.setId(usuarioId);
		usuario.setEmail(email);
		usuario.setSenha("$2a$10$encodedPasswordHash");
		usuario.setAtivo(true);
		usuario.setDataCriacao(LocalDateTime.now());
	}

	@Test
	@DisplayName("Deve realizar login com sucesso e retornar token + dados do usuário")
	void deveRealizarLoginComSucesso() {
		Authentication authentication = mock(Authentication.class);
		when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
		when(jwtService.generateToken(email)).thenReturn(token);

		LoginResponse response = authController.login(loginRequest);

		assertThat(response).isNotNull();
		assertThat(response.token()).isEqualTo(token);
		assertThat(response.usuario()).isNotNull();
		assertThat(response.usuario().getId()).isEqualTo(usuarioId);
		assertThat(response.usuario().getEmail()).isEqualTo(email);
		assertThat(response.usuario().getAtivo()).isTrue();

		verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(usuarioRepository, times(1)).findByEmail(email);
		verify(jwtService, times(1)).generateToken(email);
	}

	@Test
	@DisplayName("Deve lançar exceção quando credenciais são inválidas")
	void deveLancarExcecaoQuandoCredenciaisInvalidas() {
		// Arrange
		when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Credenciais inválidas"));

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage("Credenciais inválidas");

		verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(usuarioRepository, never()).findByEmail(anyString());
		verify(jwtService, never()).generateToken(anyString());
	}
	@Test
	@DisplayName("Deve lançar exceção quando usuário não é encontrado no banco")
	void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
		// Arrange
		Authentication authentication = mock(Authentication.class);
		when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("Usuário não encontrado");

		verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(usuarioRepository, times(1)).findByEmail(email);
		verify(jwtService, never()).generateToken(anyString());
	}

	@Test
	@DisplayName("Deve lançar exceção quando email é nulo")
	void deveLancarExcecaoQuandoEmailNulo() {
		// Arrange
		loginRequest.setEmail(null);

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(Exception.class);

		// Verifica que nenhum dos mocks foi chamado
		verifyNoInteractions(authManager);
		verifyNoInteractions(usuarioRepository);
		verifyNoInteractions(jwtService);
	}

	@Test
	@DisplayName("Deve lançar exceção quando email é vazio")
	void deveLancarExcecaoQuandoEmailVazio() {
		// Arrange
		loginRequest.setEmail("");

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(Exception.class);

		verifyNoInteractions(authManager);
		verifyNoInteractions(usuarioRepository);
		verifyNoInteractions(jwtService);
	}

	@Test
	@DisplayName("Deve lançar exceção quando senha é nula")
	void deveLancarExcecaoQuandoSenhaNula() {
		// Arrange
		loginRequest.setSenha(null);

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(Exception.class);

		verifyNoInteractions(authManager);
		verifyNoInteractions(usuarioRepository);
		verifyNoInteractions(jwtService);
	}

	@Test
	@DisplayName("Deve lançar exceção quando senha é vazia")
	void deveLancarExcecaoQuandoSenhaVazia() {
		// Arrange
		loginRequest.setSenha("");

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(Exception.class);

		verifyNoInteractions(authManager);
		verifyNoInteractions(usuarioRepository);
		verifyNoInteractions(jwtService);
	}

	@Test
	@DisplayName("Deve retornar LoginResponse com token e dados do usuário")
	void deveRetornarLoginResponseCompleto() {
		// Arrange
		Authentication authentication = mock(Authentication.class);
		when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
		when(jwtService.generateToken(email)).thenReturn(token);

		// Act
		LoginResponse response = authController.login(loginRequest);

		// Assert
		assertThat(response).isInstanceOf(LoginResponse.class);
		assertThat(response.token()).isNotBlank();
		assertThat(response.usuario()).isNotNull();
		assertThat(response.usuario().getId()).isEqualTo(usuarioId);
		assertThat(response.usuario().getEmail()).isEqualTo(email);
	}

}
