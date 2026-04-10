package com.example.quantivo.to;

import com.example.quantivo.entity.Usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioCreateTO {

	@Schema(example = "usuario@email.com.br")
	@Email(message = "Formato de e-mail inválido")
	@NotBlank(message = "E-mail é orbigatório")
	private String email;

	@Schema(example = "123456Aa@-*")
	@NotBlank(message = "Senha é obrigatória")
	@Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).*$",
			message = "Senha deve conter letras e números")
	private String senha;

	public UsuarioCreateTO() {}

	public UsuarioCreateTO(String email, String senha) {}

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
