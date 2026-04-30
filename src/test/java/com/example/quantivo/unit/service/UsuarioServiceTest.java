package com.example.quantivo.unit.service;

import com.example.quantivo.exception.ResourceNotFoundException;
import com.example.quantivo.service.UsuarioService;
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
import java.util.Collections;
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
	private String emailLogado;

	@BeforeEach
	void setUp() {
		usuarioId = UUID.randomUUID();
		email = "usuario@teste.com";
		emailLogado = "usuario@teste.com";
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
	@DisplayName("Deve lançar exceção ao buscar todos os usuários")
	void deveLancarExcecaoAoBuscarTodosUsuarios() {
		Pageable pageable = PageRequest.of(0, 10);
		assertThatThrownBy(() -> usuarioService.buscarAllUsuarios(emailLogado, pageable))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Acesso negado. Não é permitido listar todos os usuários.");
	}

	@Test
	@DisplayName("Deve retornar usuário quando email existe e pertence ao usuário logado")
	void deveRetornarUsuarioQuandoEmailPertenceAoLogado() {
		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

		UsuarioTO resultado = usuarioService.buscarPorEmail(emailLogado, email);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getEmail()).isEqualTo(email);
		assertThat(resultado.getId()).isEqualTo(usuarioId);

		verify(usuarioRepository, times(1)).findByEmail(email);
	}

	@Test
	@DisplayName("Deve lançar exceção ao buscar email de outro usuário")
	void deveLancarExcecaoAoBuscarEmailDeOutroUsuario() {
		String outroEmail = "outro@teste.com";
		assertThatThrownBy(() -> usuarioService.buscarPorEmail(emailLogado, outroEmail))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Acesso negado. Você não tem permissão para visualizar este usuário.");
	}

	@Test
	@DisplayName("Deve retornar usuário quando ID existe e pertence ao usuário logado")
	void deveRetornarUsuarioQuandoIdPertenceAoLogado() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

		UsuarioTO resultado = usuarioService.buscarPorId(emailLogado, usuarioId);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getId()).isEqualTo(usuarioId);
		assertThat(resultado.getEmail()).isEqualTo(email);

		verify(usuarioRepository, times(1)).findById(usuarioId);
	}

	@Test
	@DisplayName("Deve lançar exceção ao buscar ID de outro usuário")
	void deveLancarExcecaoAoBuscarIdDeOutroUsuario() {
		UUID outroId = UUID.randomUUID();
		Usuario outroUsuario = new Usuario();
		outroUsuario.setId(outroId);
		outroUsuario.setEmail("outro@teste.com");

		when(usuarioRepository.findById(outroId)).thenReturn(Optional.of(outroUsuario));

		assertThatThrownBy(() -> usuarioService.buscarPorId(emailLogado, outroId))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Acesso negado. Você não tem permissão para visualizar este usuário.");
	}

	@Test
	@DisplayName("Deve ativar usuário com sucesso")
	void deveAtivarUsuarioComSucesso() {
		usuario.setAtivo(false);
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

		usuarioService.ativarUsuario(emailLogado, usuarioId);

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

		usuarioService.desativarUsuario(emailLogado, usuarioId);

		assertThat(usuario.getAtivo()).isFalse();
		verify(usuarioRepository, times(1)).findById(usuarioId);
		verify(usuarioRepository, times(1)).save(usuario);
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

		usuarioService.alterarSenha(emailLogado, email, senhaAtual, senhaNova);

		verify(usuarioRepository).findByEmail(email);
		verify(passwordEncoder).matches(eq(senhaAtual), anyString());
		verify(passwordEncoder).encode(eq(senhaNova));
		verify(usuarioRepository).save(any(Usuario.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando senha atual está incorreta")
	void deveLancarExcecaoQuandoSenhaAtualIncorreta() {
		String senhaAtual = "senhaErrada";
		String senhaNova = "novaSenha456";

		when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches(senhaAtual, usuario.getSenha())).thenReturn(false);

		assertThatThrownBy(() -> usuarioService.alterarSenha(emailLogado, email, senhaAtual, senhaNova))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Senha atual incorreta");
	}

	@Test
	@DisplayName("Deve excluir usuário (desativar) com sucesso")
	void deveExcluirUsuarioComSucesso() {
		usuario.setAtivo(true);
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

		usuarioService.excluirUsuario(emailLogado, usuarioId);

		assertThat(usuario.getAtivo()).isFalse();
		verify(usuarioRepository, times(1)).findById(usuarioId);
		verify(usuarioRepository, times(1)).save(usuario);
	}
}
