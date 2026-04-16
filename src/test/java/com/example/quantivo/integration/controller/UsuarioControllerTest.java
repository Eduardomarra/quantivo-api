package com.example.quantivo.integration.controller;


import com.example.quantivo.controller.UsuarioController;
import com.example.quantivo.to.AlterarSenhaTO;
import com.example.quantivo.to.UsuarioCreateTO;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioController")
class UsuarioControllerTest {

	@Mock
	private UsuarioService usuarioService;

	@InjectMocks
	private UsuarioController usuarioController;

	private UUID usuarioId;
	private String email;
	private UsuarioTO usuarioTO;
	private UsuarioCreateTO usuarioCreateTO;
	private AlterarSenhaTO alterarSenhaTO;
	private Pageable pageable;
	private Page<UsuarioTO> page;

	@BeforeEach
	void setUp() {
		usuarioId = UUID.randomUUID();
		email = "usuario@teste.com";

		// UsuarioTO
		usuarioTO = new UsuarioTO();
		usuarioTO.setId(usuarioId);
		usuarioTO.setEmail(email);
		usuarioTO.setAtivo(true);
		usuarioTO.setDataCriacao(LocalDateTime.now());

		// UsuarioCreateTO
		usuarioCreateTO = new UsuarioCreateTO();
		usuarioCreateTO.setEmail("novo@teste.com");
		usuarioCreateTO.setSenha("senha123");

		// AlterarSenhaTO
		alterarSenhaTO = new AlterarSenhaTO();
		alterarSenhaTO.setSenhaAtual("senha123");
		alterarSenhaTO.setSenhaNova("novaSenha456");

		// Pageable e Page
		pageable = PageRequest.of(0, 10);
		List<UsuarioTO> usuarios = Arrays.asList(usuarioTO);
		page = new PageImpl<>(usuarios, pageable, 1);
	}

	@Test
	@DisplayName("Deve retornar página de usuários com sucesso")
	void deveRetornarPaginaDeUsuariosComSucesso() {
		// Arrange
		when(usuarioService.buscarAllUsuarios(pageable)).thenReturn(page);

		// Act
		ResponseEntity<Page<UsuarioTO>> response = usuarioController.buscarAllUsuarios(pageable);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getContent()).hasSize(1);
		assertThat(response.getBody().getContent().get(0).getEmail()).isEqualTo(email);

		verify(usuarioService, times(1)).buscarAllUsuarios(pageable);
	}

	@Test
	@DisplayName("Deve retornar usuário quando email existe")
	void deveRetornarUsuarioQuandoEmailExiste() {
		// Arrange
		when(usuarioService.buscarPorEmail(email)).thenReturn(usuarioTO);

		// Act
		UsuarioTO response = usuarioController.buscarPorEmail(email);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(usuarioId);
		assertThat(response.getEmail()).isEqualTo(email);

		verify(usuarioService, times(1)).buscarPorEmail(email);
	}

	@Test
	@DisplayName("Deve lançar exceção quando email não existe")
	void deveLancarExcecaoQuandoEmailNaoExiste() {
		// Arrange
		String emailNaoExistente = "naoexiste@teste.com";
		when(usuarioService.buscarPorEmail(emailNaoExistente))
				.thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.buscarPorEmail(emailNaoExistente))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Usuário não encontrado");

		verify(usuarioService, times(1)).buscarPorEmail(emailNaoExistente);
	}

	@Test
	@DisplayName("Deve retornar usuário quando ID existe")
	void deveRetornarUsuarioQuandoIdExiste() {
		// Arrange
		when(usuarioService.buscarPorId(usuarioId)).thenReturn(usuarioTO);

		// Act
		UsuarioTO response = usuarioController.buscarPorId(usuarioId);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(usuarioId);
		assertThat(response.getEmail()).isEqualTo(email);

		verify(usuarioService, times(1)).buscarPorId(usuarioId);
	}

	@Test
	@DisplayName("Deve lançar exceção quando ID não existe")
	void deveLancarExcecaoQuandoIdNaoExiste() {
		// Arrange
		UUID idInvalido = UUID.randomUUID();
		when(usuarioService.buscarPorId(idInvalido))
				.thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.buscarPorId(idInvalido))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Usuário não encontrado");

		verify(usuarioService, times(1)).buscarPorId(idInvalido);
	}

	@Test
	@DisplayName("Deve criar usuário com sucesso")
	void deveCriarUsuarioComSucesso() {
		// Arrange
		when(usuarioService.criarUsuario(usuarioCreateTO)).thenReturn(usuarioTO);

		// Act
		UsuarioTO response = usuarioController.criarUsuario(usuarioCreateTO);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(usuarioId);
		assertThat(response.getEmail()).isEqualTo(email);

		verify(usuarioService, times(1)).criarUsuario(usuarioCreateTO);
	}

	@Test
	@DisplayName("Deve lançar exceção ao criar usuário com email já existente")
	void deveLancarExcecaoAoCriarUsuarioComEmailExistente() {
		// Arrange
		when(usuarioService.criarUsuario(usuarioCreateTO))
				.thenThrow(new BusinessException("Email ja cadastrado"));

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.criarUsuario(usuarioCreateTO))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Email ja cadastrado");

		verify(usuarioService, times(1)).criarUsuario(usuarioCreateTO);
	}

	@Test
	@DisplayName("Deve alterar senha com sucesso")
	void deveAlterarSenhaComSucesso() {
		// Arrange
		doNothing().when(usuarioService).alterarSenha(email, alterarSenhaTO.getSenhaAtual(), alterarSenhaTO.getSenhaNova());

		// Act
		usuarioController.alterarSenha(email, alterarSenhaTO);

		// Assert
		verify(usuarioService, times(1)).alterarSenha(email, alterarSenhaTO.getSenhaAtual(), alterarSenhaTO.getSenhaNova());
	}

	@Test
	@DisplayName("Deve lançar exceção ao alterar senha com credenciais inválidas")
	void deveLancarExcecaoAoAlterarSenhaComCredenciaisInvalidas() {
		// Arrange
		doThrow(new BusinessException("Senha atual incorreta"))
				.when(usuarioService).alterarSenha(email, alterarSenhaTO.getSenhaAtual(), alterarSenhaTO.getSenhaNova());

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.alterarSenha(email, alterarSenhaTO))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Senha atual incorreta");

		verify(usuarioService, times(1)).alterarSenha(email, alterarSenhaTO.getSenhaAtual(), alterarSenhaTO.getSenhaNova());
	}

	@Test
	@DisplayName("Deve excluir usuário com sucesso")
	void deveExcluirUsuarioComSucesso() {
		// Arrange
		doNothing().when(usuarioService).excluirUsuario(usuarioId);

		// Act
		usuarioController.excluirUsuario(usuarioId);

		// Assert
		verify(usuarioService, times(1)).excluirUsuario(usuarioId);
	}

	@Test
	@DisplayName("Deve lançar exceção ao excluir usuário inexistente")
	void deveLancarExcecaoAoExcluirUsuarioInexistente() {
		// Arrange
		UUID idInvalido = UUID.randomUUID();
		doThrow(new ResourceNotFoundException("Usuário não encontrado"))
				.when(usuarioService).excluirUsuario(idInvalido);

		// Act & Assert
		assertThatThrownBy(() -> usuarioController.excluirUsuario(idInvalido))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Usuário não encontrado");

		verify(usuarioService, times(1)).excluirUsuario(idInvalido);
	}

	@Test
	@DisplayName("Deve ativar usuário com sucesso")
	void deveAtivarUsuarioComSucesso() {
		// Arrange
		doNothing().when(usuarioService).ativarUsuario(usuarioId);

		// Act
		usuarioController.ativarUsuario(usuarioId);

		// Assert
		verify(usuarioService, times(1)).ativarUsuario(usuarioId);
	}

	@Test
	@DisplayName("Deve desativar usuário com sucesso")
	void deveDesativarUsuarioComSucesso() {
		// Arrange
		doNothing().when(usuarioService).desativarUsuario(usuarioId);

		// Act
		usuarioController.desativarUsuario(usuarioId);

		// Assert
		verify(usuarioService, times(1)).desativarUsuario(usuarioId);
	}
}
