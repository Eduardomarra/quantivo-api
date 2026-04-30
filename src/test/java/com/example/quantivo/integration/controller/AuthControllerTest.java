package com.example.quantivo.integration.controller;

import com.example.quantivo.controller.AuthController;
import com.example.quantivo.entity.PasswordResetToken;
import com.example.quantivo.entity.Usuario;
import com.example.quantivo.exception.BusinessException;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.security.JwtService;
import com.example.quantivo.service.EmailService;
import com.example.quantivo.service.PasswordResetTokenService;
import com.example.quantivo.to.ForgotPasswordRequest;
import com.example.quantivo.to.LoginRequestTO;
import com.example.quantivo.to.PasswordResetRequest;
import com.example.quantivo.to.UsuarioTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private PasswordResetTokenService tokenService;

	@Mock
	private EmailService emailService;

	@InjectMocks
	private AuthController authController;

	private LoginRequestTO loginRequest;
	private ForgotPasswordRequest forgotPasswordRequest;
	private PasswordResetRequest passwordResetRequest;
	private Usuario usuario;
	private PasswordResetToken passwordResetToken;
	private String email;
	private String senha;
	private String token;
	private UUID usuarioId;

	@BeforeEach
	void setUp() {
		email = "usuario@teste.com";
		senha = "senha123";
		token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQHRlc3RlLmNvbSJ9..."; // Exemplo de token JWT
		usuarioId = UUID.randomUUID();

		loginRequest = new LoginRequestTO();
		loginRequest.setEmail(email);
		loginRequest.setSenha(senha);

		forgotPasswordRequest = new ForgotPasswordRequest();
		forgotPasswordRequest.setEmail(email);

		passwordResetRequest = new PasswordResetRequest();
		passwordResetRequest.setToken("validResetToken");
		passwordResetRequest.setNewPassword("novaSenha123");
		passwordResetRequest.setConfirmPassword("novaSenha123");

		usuario = new Usuario();
		usuario.setId(usuarioId);
		usuario.setEmail(email);
		usuario.setSenha("encodedPasswordHash"); // Senha já encodificada
		usuario.setAtivo(true);
		usuario.setDataCriacao(LocalDateTime.now());

		passwordResetToken = new PasswordResetToken("validResetToken", usuario);
	}

	// Testes para o método login

	@Test
	@DisplayName("Deve realizar login com sucesso e retornar token + dados do usuário")
	void deveRealizarLoginComSucesso() {
		// Arrange
		Authentication authentication = mock(Authentication.class);
		when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
		when(jwtService.generateToken(email)).thenReturn(token);

		// Act
		ResponseEntity<UsuarioTO> responseEntity = authController.login(loginRequest);

		// Assert
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + token);
		assertThat(responseEntity.getBody()).isNotNull();
		assertThat(responseEntity.getBody().getId()).isEqualTo(usuarioId);
		assertThat(responseEntity.getBody().getEmail()).isEqualTo(email);
		assertThat(responseEntity.getBody().getAtivo()).isTrue();

		verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(usuarioRepository, times(1)).findByEmail(email);
		verify(jwtService, times(1)).generateToken(email);
	}

	@Test
	@DisplayName("Deve lançar exceção quando credenciais são inválidas no login")
	void deveLancarExcecaoQuandoCredenciaisInvalidasNoLogin() {
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
	@DisplayName("Deve lançar exceção quando usuário não é encontrado no banco durante o login")
	void deveLancarExcecaoQuandoUsuarioNaoEncontradoNoLogin() {
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
	@DisplayName("Deve lançar exceção quando email é nulo no login")
	void deveLancarExcecaoQuandoEmailNuloNoLogin() {
		// Arrange
		loginRequest.setEmail(null);

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Email não pode ser nulo ou vazio");

		verifyNoInteractions(authManager);
		verifyNoInteractions(usuarioRepository);
		verifyNoInteractions(jwtService);
	}

	@Test
	@DisplayName("Deve lançar exceção quando email é vazio no login")
	void deveLancarExcecaoQuandoEmailVazioNoLogin() {
		// Arrange
		loginRequest.setEmail("");

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Email não pode ser nulo ou vazio");

		verifyNoInteractions(authManager);
		verifyNoInteractions(usuarioRepository);
		verifyNoInteractions(jwtService);
	}

	@Test
	@DisplayName("Deve lançar exceção quando senha é nula no login")
	void deveLancarExcecaoQuandoSenhaNulaNoLogin() {
		// Arrange
		loginRequest.setSenha(null);

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Senha não pode ser nula ou vazia");

		verifyNoInteractions(authManager);
		verifyNoInteractions(usuarioRepository);
		verifyNoInteractions(jwtService);
	}

	@Test
	@DisplayName("Deve lançar exceção quando senha é vazia no login")
	void deveLancarExcecaoQuandoSenhaVaziaNoLogin() {
		// Arrange
		loginRequest.setSenha("");

		// Act & Assert
		assertThatThrownBy(() -> authController.login(loginRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Senha não pode ser nula ou vazia");

		verifyNoInteractions(authManager);
		verifyNoInteractions(usuarioRepository);
		verifyNoInteractions(jwtService);
	}

	// Testes para o método register

	@Test
	@DisplayName("Deve registrar usuário com sucesso")
	void deveRegistrarUsuarioComSucesso() {
		// Arrange
		LoginRequestTO registerRequest = new LoginRequestTO();
		registerRequest.setEmail("novo@teste.com");
		registerRequest.setSenha("senhaSegura123");

		Usuario novoUsuario = new Usuario();
		novoUsuario.setId(UUID.randomUUID());
		novoUsuario.setEmail(registerRequest.getEmail());
		novoUsuario.setSenha("encodedSenhaSegura");
		novoUsuario.setAtivo(true);
		novoUsuario.setDataCriacao(LocalDateTime.now());

		when(usuarioRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(registerRequest.getSenha())).thenReturn("encodedSenhaSegura");
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(novoUsuario);
		when(jwtService.generateToken(novoUsuario.getEmail())).thenReturn("novoToken");

		// Act
		ResponseEntity<UsuarioTO> responseEntity = authController.register(registerRequest);

		// Assert
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer novoToken");
		assertThat(responseEntity.getBody()).isNotNull();
		assertThat(responseEntity.getBody().getEmail()).isEqualTo(novoUsuario.getEmail());

		verify(usuarioRepository, times(1)).findByEmail(registerRequest.getEmail());
		verify(passwordEncoder, times(1)).encode(registerRequest.getSenha());
		verify(usuarioRepository, times(1)).save(any(Usuario.class));
		verify(jwtService, times(1)).generateToken(novoUsuario.getEmail());
	}

	@Test
	@DisplayName("Deve lançar exceção ao registrar com email já cadastrado")
	void deveLancarExcecaoAoRegistrarComEmailJaCadastrado() {
		// Arrange
		LoginRequestTO registerRequest = new LoginRequestTO();
		registerRequest.setEmail(email);
		registerRequest.setSenha("senhaSegura123");

		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

		// Act & Assert
		assertThatThrownBy(() -> authController.register(registerRequest))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Email já cadastrado");

		verify(usuarioRepository, times(1)).findByEmail(email);
		verify(passwordEncoder, never()).encode(anyString());
		verify(usuarioRepository, never()).save(any(Usuario.class));
		verify(jwtService, never()).generateToken(anyString());
	}

	@Test
	@DisplayName("Deve lançar exceção ao registrar com senha muito curta")
	void deveLancarExcecaoAoRegistrarComSenhaMuitoCurta() {
		// Arrange
		LoginRequestTO registerRequest = new LoginRequestTO();
		registerRequest.setEmail("novo@teste.com");
		registerRequest.setSenha("123"); // Senha muito curta

		// Act & Assert
		assertThatThrownBy(() -> authController.register(registerRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Senha deve ter no mínimo 6 caracteres");

		verify(usuarioRepository, never()).findByEmail(anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(usuarioRepository, never()).save(any(Usuario.class));
		verify(jwtService, never()).generateToken(anyString());
	}

	// Testes para o método forgotPassword

	@Test
	@DisplayName("Deve retornar OK e não enviar email se o usuário não for encontrado")
	void deveRetornarOkENaoEnviarEmailSeUsuarioNaoEncontrado() {
		// Arrange
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

		// Act
		ResponseEntity<Void> responseEntity = authController.forgotPassword(forgotPasswordRequest);

		// Assert
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		verify(usuarioRepository, times(1)).findByEmail(email);
		verify(tokenService, never()).createToken(any(Usuario.class));
		verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
	}

	@Test
	@DisplayName("Deve enviar email de redefinição de senha com sucesso")
	void deveEnviarEmailDeRedefinicaoComSucesso() {
		// Arrange
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
		when(tokenService.createToken(usuario)).thenReturn(passwordResetToken);
		doNothing().when(emailService).sendPasswordResetEmail(email, passwordResetToken.getToken());

		// Act
		ResponseEntity<Void> responseEntity = authController.forgotPassword(forgotPasswordRequest);

		// Assert
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		verify(usuarioRepository, times(1)).findByEmail(email);
		verify(tokenService, times(1)).createToken(usuario);
		verify(emailService, times(1)).sendPasswordResetEmail(email, passwordResetToken.getToken());
	}

	// Testes para o método savePassword

	@Test
	@DisplayName("Deve salvar nova senha com sucesso e invalidar token")
	void deveSalvarNovaSenhaComSucessoEInvalidarToken() {
		// Arrange
		when(tokenService.validatePasswordResetToken(passwordResetRequest.getToken())).thenReturn(passwordResetToken);
		when(passwordEncoder.encode(passwordResetRequest.getNewPassword())).thenReturn("encodedNovaSenha");
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
		doNothing().when(tokenService).deleteToken(passwordResetRequest.getToken());

		// Act
		ResponseEntity<Void> responseEntity = authController.savePassword(passwordResetRequest);

		// Assert
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(usuario.getSenha()).isEqualTo("encodedNovaSenha"); // Verifica se a senha do objeto foi atualizada

		verify(tokenService, times(1)).validatePasswordResetToken(passwordResetRequest.getToken());
		verify(passwordEncoder, times(1)).encode(passwordResetRequest.getNewPassword());
		verify(usuarioRepository, times(1)).save(usuario);
		verify(tokenService, times(1)).deleteToken(passwordResetRequest.getToken());
	}

	@Test
	@DisplayName("Deve lançar exceção quando senhas não conferem ao salvar nova senha")
	void deveLancarExcecaoQuandoSenhasNaoConferem() {
		// Arrange
		passwordResetRequest.setConfirmPassword("senhaDiferente");

		// Act & Assert
		assertThatThrownBy(() -> authController.savePassword(passwordResetRequest))
				.isInstanceOf(BusinessException.class)
				.hasMessage("As senhas não conferem.");

		verify(tokenService, never()).validatePasswordResetToken(anyString());
		verify(passwordEncoder, never()).encode(anyString());
		verify(usuarioRepository, never()).save(any(Usuario.class));
		verify(tokenService, never()).deleteToken(anyString());
	}

	@Test
	@DisplayName("Deve retornar BAD_REQUEST quando token inválido ou expirado ao salvar nova senha")
	void deveRetornarBadRequestQuandoTokenInvalidoOuExpirado() {
		// Arrange
		when(tokenService.validatePasswordResetToken(passwordResetRequest.getToken())).thenReturn(null);

		// Act
		ResponseEntity<Void> responseEntity = authController.savePassword(passwordResetRequest);

		// Assert
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		verify(tokenService, times(1)).validatePasswordResetToken(passwordResetRequest.getToken());
		verify(passwordEncoder, never()).encode(anyString());
		verify(usuarioRepository, never()).save(any(Usuario.class));
		verify(tokenService, never()).deleteToken(anyString());
	}

	// Testes para o método validateToken

	@Test
	@DisplayName("Deve validar token com sucesso")
	void deveValidarTokenComSucesso() {
		// Arrange
		when(tokenService.validatePasswordResetToken("validToken")).thenReturn(passwordResetToken);

		// Act
		ResponseEntity<Void> responseEntity = authController.validateToken("validToken");

		// Assert
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		verify(tokenService, times(1)).validatePasswordResetToken("validToken");
	}

	@Test
	@DisplayName("Deve retornar BAD_REQUEST quando token inválido ou expirado na validação")
	void deveRetornarBadRequestQuandoTokenInvalidoOuExpiradoNaValidacao() {
		// Arrange
		when(tokenService.validatePasswordResetToken("invalidToken")).thenReturn(null);

		// Act
		ResponseEntity<Void> responseEntity = authController.validateToken("invalidToken");

		// Assert
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		verify(tokenService, times(1)).validatePasswordResetToken("invalidToken");
	}
}
