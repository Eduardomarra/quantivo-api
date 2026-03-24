package com.example.quantivo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quantivo.entity.Usuario;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.security.JwtService;
import com.example.quantivo.to.LoginRequestTO;
import com.example.quantivo.to.LoginResponse;
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

	public AuthController(
			UsuarioRepository usuarioRepository,
			AuthenticationManager authManager,
			JwtService jwtService
	) {
		this.usuarioRepository = usuarioRepository;
		this.authManager = authManager;
		this.jwtService = jwtService;
	}

	@Operation(summary="Realiza login e retorna JWT")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Login realizado com sucesso."),
		@ApiResponse(responseCode = "401", description = "Credenciais inválidas."),
		@ApiResponse(responseCode = "500", description = "Erro interno."),
	})
	@SecurityRequirement(name = "BearerAuth")
	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequestTO request) {

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

		return new LoginResponse(token, new UsuarioTO(usuario));
	}
}
