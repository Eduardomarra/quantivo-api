package com.example.quantivo.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AlterarSenhaTO {

	@NotBlank(message = "Informe a senha atual")
	private String senhaAtual;

	@NotBlank(message = "Informe a nova senha")
	@Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).*$",
			message = "Senha deve conter letras e números")
	private String senhaNova;

	public String getSenhaAtual() {
		return senhaAtual;
	}

	public void setSenhaAtual(String senhaAtual) {
		this.senhaAtual = senhaAtual;
	}

	public String getSenhaNova() {
		return senhaNova;
	}

	public void setSenhaNova(String senhaNova) {
		this.senhaNova = senhaNova;
	}
}
