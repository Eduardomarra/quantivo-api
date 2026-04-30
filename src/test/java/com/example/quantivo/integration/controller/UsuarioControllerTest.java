package com.example.quantivo.integration.controller;

import com.example.quantivo.controller.UsuarioController;
import com.example.quantivo.to.AlterarSenhaTO;
import com.example.quantivo.to.UsuarioTO;
import com.example.quantivo.exception.BusinessException;
import com.example.quantivo.exception.ResourceNotFoundException;
import com.example.quantivo.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioController")
class UsuarioControllerTest {

	@Mock
	private UsuarioService usuarioService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private UsuarioController usuarioController;

	private UUID usuarioId;
	private String email;
	private UsuarioTO usuarioTO;
	private AlterarSenhaTO alterarSenhaTO;
	private Pageable pageable;
	private String emailLogado;

	@BeforeEach
	void setUp() {
		usuarioId = UUID.randomUUID();
		email = "usuario@teste.com";
		emailLogado = "usuario@teste.com";

		// UsuarioTO
		usuarioTO = new UsuarioTO();
		usuarioTO.setId(usuarioId);
		usuarioTO.setEmail(email);
		usuarioTO.setAtivo(true);
		usuarioTO.setDataCriacao(LocalDateTime.now());

		// AlterarSenhaTO
		alterarSenhaTO = new AlterarSenhaTO();
		alterarSenhaTO.setSenhaAtual("senha123");
		alterarSenhaTO.setSenhaNova("novaSenha456");

		// Pageable
		pageable = PageRequest.of(0, 10);
	}

	private void mockSecurityContext() {
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(emailLogado);
	}

	@Test
	@DisplayName("Deve lançar exceção ao buscar todos os usuários")
	void deveLancarExcecaoAoBuscarTodosUsuarios() {
		// Arrange
		mockSecurityContext();
		when(usuarioService.buscarAllUsuarios(emailLogado, pageable))
				.thenThrow(new BusinessException("Acesso negado. Não é permitido listar todos os usuários."));

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.buscarAllUsuarios(pageable))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Acesso negado. Não é permitido listar todos os usuários.");

		verify(usuarioService, times(1)).buscarAllUsuarios(emailLogado, pageable);
	}

	@Test
	@DisplayName("Deve retornar usuário quando email existe e pertence ao usuário logado")
	void deveRetornarUsuarioQuandoEmailPertenceAoLogado() {
		// Arrange
		mockSecurityContext();
		when(usuarioService.buscarPorEmail(emailLogado, email)).thenReturn(usuarioTO);

		// Act
		ResponseEntity<UsuarioTO> response = usuarioController.buscarPorEmail(email);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(usuarioId);

		verify(usuarioService, times(1)).buscarPorEmail(emailLogado, email);
	}

	@Test
	@DisplayName("Deve lançar exceção ao buscar email de outro usuário")
	void deveLancarExcecaoAoBuscarEmailDeOutroUsuario() {
		// Arrange
		mockSecurityContext();
		String outroEmail = "outro@teste.com";
		when(usuarioService.buscarPorEmail(emailLogado, outroEmail))
				.thenThrow(new BusinessException("Acesso negado. Você não tem permissão para visualizar este usuário."));

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.buscarPorEmail(outroEmail))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Acesso negado. Você não tem permissão para visualizar este usuário.");

		verify(usuarioService, times(1)).buscarPorEmail(emailLogado, outroEmail);
	}

	@Test
	@DisplayName("Deve retornar usuário quando ID existe e pertence ao usuário logado")
	void deveRetornarUsuarioQuandoIdPertenceAoLogado() {
		// Arrange
		mockSecurityContext();
		when(usuarioService.buscarPorId(emailLogado, usuarioId)).thenReturn(usuarioTO);

		// Act
		ResponseEntity<UsuarioTO> response = usuarioController.buscarPorId(usuarioId);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(usuarioId);

		verify(usuarioService, times(1)).buscarPorId(emailLogado, usuarioId);
	}

	@Test
	@DisplayName("Deve lançar exceção ao buscar ID de outro usuário")
	void deveLancarExcecaoAoBuscarIdDeOutroUsuario() {
		// Arrange
		mockSecurityContext();
		UUID outroId = UUID.randomUUID();
		when(usuarioService.buscarPorId(emailLogado, outroId))
				.thenThrow(new BusinessException("Acesso negado. Você não tem permissão para visualizar este usuário."));

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.buscarPorId(outroId))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Acesso negado. Você não tem permissão para visualizar este usuário.");

		verify(usuarioService, times(1)).buscarPorId(emailLogado, outroId);
	}

	@Test
	@DisplayName("Deve alterar senha com sucesso")
	void deveAlterarSenhaComSucesso() {
		// Arrange
		mockSecurityContext();
		doNothing().when(usuarioService).alterarSenha(emailLogado, email, alterarSenhaTO.getSenhaAtual(), alterarSenhaTO.getSenhaNova());

		// Act
		ResponseEntity<Void> response = usuarioController.alterarSenha(email, alterarSenhaTO);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		verify(usuarioService, times(1)).alterarSenha(emailLogado, email, alterarSenhaTO.getSenhaAtual(), alterarSenhaTO.getSenhaNova());
	}

	@Test
	@DisplayName("Deve lançar exceção ao tentar alterar senha de outro usuário")
	void deveLancarExcecaoAoAlterarSenhaDeOutroUsuario() {
		// Arrange
		mockSecurityContext();
		String outroEmail = "outro@teste.com";
		doThrow(new BusinessException("Você não tem permissão para alterar a senha deste usuário."))
				.when(usuarioService).alterarSenha(emailLogado, outroEmail, alterarSenhaTO.getSenhaAtual(), alterarSenhaTO.getSenhaNova());

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.alterarSenha(outroEmail, alterarSenhaTO))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Você não tem permissão para alterar a senha deste usuário.");

		verify(usuarioService, times(1)).alterarSenha(emailLogado, outroEmail, alterarSenhaTO.getSenhaAtual(), alterarSenhaTO.getSenhaNova());
	}

	@Test
	@DisplayName("Deve excluir usuário com sucesso")
	void deveExcluirUsuarioComSucesso() {
		// Arrange
		mockSecurityContext();
		doNothing().when(usuarioService).excluirUsuario(emailLogado, usuarioId);

		// Act
		ResponseEntity<Void> response = usuarioController.excluirUsuario(usuarioId);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		verify(usuarioService, times(1)).excluirUsuario(emailLogado, usuarioId);
	}

	@Test
	@DisplayName("Deve lançar exceção ao tentar excluir outro usuário")
	void deveLancarExcecaoAoExcluirOutroUsuario() {
		// Arrange
		mockSecurityContext();
		UUID outroId = UUID.randomUUID();
		doThrow(new BusinessException("Você não tem permissão para excluir este usuário."))
				.when(usuarioService).excluirUsuario(emailLogado, outroId);

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.excluirUsuario(outroId))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Você não tem permissão para excluir este usuário.");

		verify(usuarioService, times(1)).excluirUsuario(emailLogado, outroId);
	}

	@Test
	@DisplayName("Deve ativar usuário com sucesso")
	void deveAtivarUsuarioComSucesso() {
		// Arrange
		mockSecurityContext();
		doNothing().when(usuarioService).ativarUsuario(emailLogado, usuarioId);

		// Act
		ResponseEntity<Void> response = usuarioController.ativarUsuario(usuarioId);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		verify(usuarioService, times(1)).ativarUsuario(emailLogado, usuarioId);
	}

	@Test
	@DisplayName("Deve desativar usuário com sucesso")
	void deveDesativarUsuarioComSucesso() {
		// Arrange
		mockSecurityContext();
		doNothing().when(usuarioService).desativarUsuario(emailLogado, usuarioId);

		// Act
		ResponseEntity<Void> response = usuarioController.desativarUsuario(usuarioId);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		verify(usuarioService, times(1)).desativarUsuario(emailLogado, usuarioId);
	}
}
