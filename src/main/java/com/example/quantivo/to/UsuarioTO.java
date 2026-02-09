package com.example.quantivo.to;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.quantivo.entity.Usuario;


public class UsuarioTO {

	private UUID id;
	private String email;
	private Boolean ativo;
	private LocalDateTime dataCriacao;

	public UsuarioTO() {}

	public UsuarioTO (Usuario usuario) {
		this.id = usuario.getId();
		this.email = usuario.getEmail();
		this.ativo = usuario.getAtivo();
		this.dataCriacao = usuario.getDataCriacao();
	}

	public UsuarioTO(UUID id, String email, String senha, Boolean ativo, LocalDateTime dataCriacao) {
		this.id = id;
		this.email = email;
		this.ativo = ativo;
		this.dataCriacao = dataCriacao;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}
}
