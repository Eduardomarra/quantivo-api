package com.example.quantivo.to;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Objeto de requisição de login")
public class LoginRequestTO {

	@NotBlank(message = "Email é obrigatório")
	@Email(message = "Email deve ser válido")
	@Schema(example = "usuario@email.com.br")
	private String email;

	//@NotBlank(message = "Senha é obrigatória")
	@Schema(example = "123456Aa@-*")
	private String senha;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

}
