package com.example.quantivo.to;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Objeto de requisição de login")
public class LoginRequestTO {

	@Schema(example = "usuario@email.com.br")
	private String email;

	@Schema(example = "123456Aa@-*")
	private String senha;

	public String getEmail() {

		return email;
	}

	public String getSenha() {

		return senha;
	}

}
