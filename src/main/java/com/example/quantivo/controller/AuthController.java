package com.example.quantivo.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quantivo.entity.Usuario;
import com.example.quantivo.exception.BusinessException;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.security.JwtService;
import com.example.quantivo.services.UsuarioService;
import com.example.quantivo.to.LoginRequestTO;
import com.example.quantivo.to.UsuarioTO;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name="Autenticação", description = "Endpoints de autenticação")
public class AuthController {

	private final UsuarioRepository usuarioRepository;
	private final AuthenticationManager authManager;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;

	public AuthController(
			UsuarioRepository usuarioRepository,
			AuthenticationManager authManager,
			JwtService jwtService,
			PasswordEncoder passwordEncoder
	) {
		this.usuarioRepository = usuarioRepository;
		this.authManager = authManager;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
	}

	@Operation(summary="Realiza login e retorna JWT")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Login realizado com sucesso."),
		@ApiResponse(responseCode = "401", description = "Credenciais inválidas."),
		@ApiResponse(responseCode = "500", description = "Erro interno."),
	})
	@SecurityRequirement(name = "BearerAuth")
	@PostMapping("/login")
	public ResponseEntity<UsuarioTO> login(@Valid @RequestBody LoginRequestTO request) {

		// Validação dos campos
		if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
			throw new IllegalArgumentException("Email não pode ser nulo ou vazio");
		}

		if (request.getSenha() == null || request.getSenha().trim().isEmpty()) {
			throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
		}

		authManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getSenha()
				)
		);

		Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

		String token = jwtService.generateToken(request.getEmail());

		return ResponseEntity.ok()
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.header("Access-Control-Expose-Headers", HttpHeaders.AUTHORIZATION) // Para CORS
				.body(new UsuarioTO(usuario));
	}

	@Operation(summary="Realiza cadastro e retorna JWT")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "cadastro realizado com sucesso."),
			@ApiResponse(responseCode = "500", description = "Erro interno."),
	})
	@SecurityRequirement(name = "BearerAuth")
	@PostMapping("/register")
	public ResponseEntity<UsuarioTO> register(@Valid @RequestBody LoginRequestTO request) {

		validateRequest(request);

		if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new BusinessException("Email já cadastrado");
		}

		Usuario usuario = criarUsuario(request);

		String token = jwtService.generateToken(request.getEmail());

		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.header("Access-Control-Expose-Headers", HttpHeaders.AUTHORIZATION)
				.body(new UsuarioTO(usuario));
	}

	private void validateRequest(LoginRequestTO request) {
		if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
			throw new IllegalArgumentException("Email não pode ser nulo ou vazio");
		}
		if (request.getSenha() == null || request.getSenha().trim().isEmpty()) {
			throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
		}
		if (request.getSenha().length() < 6) {
			throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres");
		}
	}

	private Usuario criarUsuario(LoginRequestTO request) {
		Usuario usuario = new Usuario();
		usuario.setEmail(request.getEmail());
		usuario.setSenha(passwordEncoder.encode(request.getSenha()));
		usuario.setAtivo(true);
		usuario.setDataCriacao(LocalDateTime.now());
		return usuarioRepository.save(usuario);
	}
}
