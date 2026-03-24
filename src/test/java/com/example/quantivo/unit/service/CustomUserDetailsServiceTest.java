package com.example.quantivo.unit.service;

import com.example.quantivo.entity.Usuario;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.services.CustomUserDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CustomUserDetailsService")
class CustomUserDetailsServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private CustomUserDetailsService customUserDetailsService;

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		usuario = new Usuario();
		usuario.setId(UUID.randomUUID());
		usuario.setEmail("usuario@teste.com");
		usuario.setSenha("senha123");
		usuario.setAtivo(true);
		usuario.setDataCriacao(LocalDateTime.now());
	}

	@Test
	@DisplayName("Deve carregar usuário com sucesso quando email existe")
	void deveCarregarUsuarioComSucesso() {
		when(usuarioRepository.findByEmail(usuario.getEmail()))
				.thenReturn(Optional.of(usuario));

		UserDetails userDetails = customUserDetailsService.loadUserByUsername(usuario.getEmail());

		assertThat(userDetails).isNotNull();
		assertThat(userDetails.getUsername()).isEqualTo(usuario.getEmail());
		assertThat(userDetails.getPassword()).isEqualTo(usuario.getSenha());
		assertThat(userDetails.getAuthorities()).hasSize(1);
		assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
				.isEqualTo("ROLE_USER");

		verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
	}

	@Test
	@DisplayName("Deve lançar UsernameNotFoundException quando email não existe")
	void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
		String emailNaoExistente = "naoexiste@teste.com";
		when(usuarioRepository.findByEmail(emailNaoExistente))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(emailNaoExistente))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("Usuário não encontrado");

		verify(usuarioRepository, times(1)).findByEmail(emailNaoExistente);
	}

	@Test
	@DisplayName("Deve lançar exceção quando email é nulo")
	void deveLancarExcecaoQuandoEmailNulo() {
		assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("Usuário não encontrado");

		// O repository não deve ser chamado quando email é nulo
		verify(usuarioRepository, never()).findByEmail(anyString());
	}

	@Test
	@DisplayName("Deve tentar buscar no banco quando email é vazio e lançar exceção")
	void deveLancarExcecaoQuandoEmailVazio() {
		String emailVazio = "";

		// Mock para retornar empty quando buscar com string vazia
		when(usuarioRepository.findByEmail(emailVazio))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(emailVazio))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("Usuário não encontrado");

		// Verifica que o repository foi chamado com o email vazio
		verify(usuarioRepository, times(1)).findByEmail(emailVazio);
	}

	@Test
	@DisplayName("Deve carregar usuário mesmo se inativo (Spring Security gerencia isso)")
	void deveCarregarUsuarioInativo() {
		usuario.setAtivo(false);
		when(usuarioRepository.findByEmail(usuario.getEmail()))
				.thenReturn(Optional.of(usuario));

		UserDetails userDetails = customUserDetailsService.loadUserByUsername(usuario.getEmail());

		assertThat(userDetails).isNotNull();
		assertThat(userDetails.getUsername()).isEqualTo(usuario.getEmail());
		verify(usuarioRepository, times(1)).findByEmail(usuario.getEmail());
	}

	@Test
	@DisplayName("Deve configurar corretamente as roles do usuário")
	void deveConfigurarRolesCorretamente() {
		when(usuarioRepository.findByEmail(usuario.getEmail()))
				.thenReturn(Optional.of(usuario));

		UserDetails userDetails = customUserDetailsService.loadUserByUsername(usuario.getEmail());

		assertThat(userDetails.getAuthorities())
				.hasSize(1)
				.extracting("authority")
				.containsExactly("ROLE_USER");
	}
}
