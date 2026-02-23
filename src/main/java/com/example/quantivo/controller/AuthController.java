package com.example.quantivo.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quantivo.security.JwtService;
import com.example.quantivo.to.LoginRequestTO;
import com.example.quantivo.to.LoginResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authManager;
	private final JwtService jwtService;

	public AuthController(
			AuthenticationManager authManager,
			JwtService jwtService
	) {
		this.authManager = authManager;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequestTO request) {

		authManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getSenha()
				)
		);

		String token = jwtService.generateToken(request.getEmail());

		return new LoginResponse(token);
	}
}
