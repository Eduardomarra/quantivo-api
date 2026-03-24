package com.example.quantivo.unit.service;

import com.example.quantivo.exception.ResourceNotFoundException;
import com.example.quantivo.services.UsuarioService;
import com.example.quantivo.entity.Usuario;
import com.example.quantivo.exception.BusinessException;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.to.UsuarioCreateTO;
import com.example.quantivo.to.UsuarioTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioService")
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UsuarioService usuarioService;

	private UUID usuarioId;
	private String email;
	private String senha;
	private String senhaEncoded;
	private Usuario usuario;
	private UsuarioCreateTO usuarioCreateTO;

	@BeforeEach
	void setUp() {
		usuarioId = UUID.randomUUID();
		email = "usuario@teste.com";
		senha = "senha123";
		senhaEncoded = "$2a$10$encodedPasswordHash";

		usuario = new Usuario();
		usuario.setId(usuarioId);
		usuario.setEmail(email);
		usuario.setSenha(senhaEncoded);
		usuario.setAtivo(true);
		usuario.setDataCriacao(LocalDateTime.now());

		usuarioCreateTO = new UsuarioCreateTO();
		usuarioCreateTO.setEmail(email);
		usuarioCreateTO.setSenha(senha);
	}

	@Test
	@DisplayName("Deve criar usuário com sucesso quando email não existe")
	void deveCriarUsuarioComSucesso() {
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(senha)).thenReturn(senhaEncoded);
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

		UsuarioTO resultado = usuarioService.criarUsuario(usuarioCreateTO);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getEmail()).isEqualTo(email);
		assertThat(resultado.getAtivo()).isTrue();

		verify(usuarioRepository, times(1)).findByEmail(email);
		verify(passwordEncoder, times(1)).encode(senha);
		verify(usuarioRepository, times(1)).save(any(Usuario.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando email já está cadastrado")
	void deveLancarExcecaoQuandoEmailJaCadastrado() {
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

		assertThatThrownBy(() -> usuarioService.criarUsuario(usuarioCreateTO))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Email ja cadastrado");

		verify(usuarioRepository, times(1)).findByEmail(email);
		verify(passwordEncoder, never()).encode(anyString());
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	@DisplayName("Deve retornar página de usuários")
	void deveRetornarPaginaDeUsuarios() {
		Pageable pageable = PageRequest.of(0, 10);
		List<Usuario> usuarios = List.of(usuario);
		Page<Usuario> page = new PageImpl<>(usuarios, pageable, 1);

		when(usuarioRepository.findAll(pageable)).thenReturn(page);

		Page<UsuarioTO> resultado = usuarioService.buscarAllUsuarios(pageable);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getContent()).hasSize(1);
		assertThat(resultado.getContent().get(0).getEmail()).isEqualTo(email);

		verify(usuarioRepository, times(1)).findAll(pageable);
	}

	@Test
	@DisplayName("Deve retornar página vazia quando não há usuários")
	void deveRetornarPaginaVazia() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Usuario> page = Page.empty(pageable);

		when(usuarioRepository.findAll(pageable)).thenReturn(page);

		Page<UsuarioTO> resultado = usuarioService.buscarAllUsuarios(pageable);

		assertThat(resultado).isEmpty();
		assertThat(resultado.getContent()).isEmpty();
	}

	@Test
	@DisplayName("Deve retornar usuário quando email existe")
	void deveRetornarUsuarioQuandoEmailExiste() {
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

		UsuarioTO resultado = usuarioService.buscarPorEmail(email);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getEmail()).isEqualTo(email);
		assertThat(resultado.getId()).isEqualTo(usuarioId);

		verify(usuarioRepository, times(1)).findByEmail(email);
	}

	@Test
	@DisplayName("Deve lançar exceção quando email não existe")
	void deveLancarExcecaoQuandoEmailNaoExiste() {
		String emailNaoExistente = "naoexiste@teste.com";
		when(usuarioRepository.findByEmail(emailNaoExistente))
				.thenThrow(new RuntimeException("Usuário não encontrado"));

		assertThatThrownBy(() -> usuarioService.buscarPorEmail(emailNaoExistente))
				.isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("Deve retornar usuário quando ID existe")
	void deveRetornarUsuarioQuandoIdExiste() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

		UsuarioTO resultado = usuarioService.buscarPorId(usuarioId);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getId()).isEqualTo(usuarioId);
		assertThat(resultado.getEmail()).isEqualTo(email);

		verify(usuarioRepository, times(1)).findById(usuarioId);
	}

	@Test
	@DisplayName("Deve lançar exceção quando ID não existe")
	void deveLancarExcecaoQuandoIdNaoExiste() {
		UUID idInvalido = UUID.randomUUID();
		when(usuarioRepository.findById(idInvalido))
				.thenThrow(new RuntimeException("Usuário não encontrado"));

		assertThatThrownBy(() -> usuarioService.buscarPorId(idInvalido))
				.isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("Deve ativar usuário com sucesso")
	void deveAtivarUsuarioComSucesso() {
		// Arrange
		usuario.setAtivo(false);
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

		// Act
		usuarioService.ativarUsuario(usuarioId);

		// Assert
		assertThat(usuario.getAtivo()).isTrue();
		verify(usuarioRepository, times(1)).findById(usuarioId);
		verify(usuarioRepository, times(1)).save(usuario);
	}

	@Test
	@DisplayName("Deve desativar usuário com sucesso")
	void deveDesativarUsuarioComSucesso() {
		usuario.setAtivo(true);
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

		usuarioService.desativarUsuario(usuarioId);

		assertThat(usuario.getAtivo()).isFalse();
		verify(usuarioRepository, times(1)).findById(usuarioId);
		verify(usuarioRepository, times(1)).save(usuario);
	}

	@Test
	@DisplayName("Deve lançar exceção ao ativar usuário inexistente")
	void deveLancarExcecaoAoAtivarUsuarioInexistente() {
		UUID idInvalido = UUID.randomUUID();
		when(usuarioRepository.findById(idInvalido))
				.thenThrow(new RuntimeException("Usuário não encontrado"));

		assertThatThrownBy(() -> usuarioService.ativarUsuario(idInvalido))
				.isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("Deve alterar senha com sucesso")
	void deveAlterarSenhaComSucesso() {
		String senhaAtual = "senha123";
		String senhaNova = "novaSenha456";
		String senhaNovaEncoded = "$2a$10$novaSenhaHash";

		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches(eq(senhaAtual), anyString())).thenReturn(true);
		when(passwordEncoder.encode(eq(senhaNova))).thenReturn(senhaNovaEncoded);
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

		usuarioService.alterarSenha(email, senhaAtual, senhaNova);

		verify(usuarioRepository).findByEmail(email);
		verify(passwordEncoder).matches(eq(senhaAtual), anyString());
		verify(passwordEncoder).encode(eq(senhaNova));
		verify(usuarioRepository).save(any(Usuario.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando senha atual está incorreta")
	void deveLancarExcecaoQuandoSenhaAtualIncorreta() {
		// Arrange
		String senhaAtual = "senhaErrada";
		String senhaNova = "novaSenha456";

		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches(senhaAtual, usuario.getSenha())).thenReturn(false);

		// Act & Assert
		assertThatThrownBy(() -> usuarioService.alterarSenha(email, senhaAtual, senhaNova))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Senha atual incorreta");

		verify(usuarioRepository, times(1)).findByEmail(email);
		verify(passwordEncoder, times(1)).matches(senhaAtual, usuario.getSenha());
		verify(passwordEncoder, never()).encode(anyString());
		verify(usuarioRepository, never()).save(any());
	}

	@Test
	@DisplayName("Deve lançar exceção quando usuário não encontrado")
	void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
		// Arrange
		String emailNaoExistente = "naoexiste@teste.com";
		when(usuarioRepository.findByEmail(emailNaoExistente))
				.thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> usuarioService.alterarSenha(emailNaoExistente, "senha", "nova"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Usuário não encontrado");

		verify(passwordEncoder, never()).matches(anyString(), anyString());
		verify(usuarioRepository, never()).save(any());
	}

	@Test
	@DisplayName("Deve excluir usuário (desativar) com sucesso")
	void deveExcluirUsuarioComSucesso() {
		usuario.setAtivo(true);
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

		usuarioService.excluirUsuario(usuarioId);

		assertThat(usuario.getAtivo()).isFalse();
		verify(usuarioRepository, times(1)).findById(usuarioId);
		verify(usuarioRepository, times(1)).save(usuario);
	}

	@Test
	@DisplayName("Deve lançar exceção ao excluir usuário inexistente")
	void deveLancarExcecaoAoExcluirUsuarioInexistente() {
		UUID idInvalido = UUID.randomUUID();
		when(usuarioRepository.findById(idInvalido))
				.thenThrow(new RuntimeException("Usuário não encontrado"));

		assertThatThrownBy(() -> usuarioService.excluirUsuario(idInvalido))
				.isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("Deve manter dados do usuário ao criar")
	void deveManterDadosCorretosAoCriar() {
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(senha)).thenReturn(senhaEncoded);
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
			Usuario user = invocation.getArgument(0);
			user.setId(usuarioId);
			return user;
		});

		UsuarioTO resultado = usuarioService.criarUsuario(usuarioCreateTO);

		assertThat(resultado.getEmail()).isEqualTo(email);
		assertThat(resultado.getAtivo()).isTrue();
		assertThat(resultado.getDataCriacao()).isNotNull();
	}

	@Test
	@DisplayName("Deve tratar email com case insensitive")
	void deveTratarEmailComCaseInsensitive() {
		String emailMaiusculo = "USUARIO@TESTE.COM";
		when(usuarioRepository.findByEmail(emailMaiusculo)).thenReturn(Optional.of(usuario));

		UsuarioTO resultado = usuarioService.buscarPorEmail(emailMaiusculo);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getEmail()).isEqualTo(email);
	}
}
