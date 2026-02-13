package com.example.quantivo.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "core", name = "USUARIO")
public class Usuario {

	@Id
	@GeneratedValue
	@Column(nullable = false)
	private UUID id;

	@Column(unique = true, nullable = true)
	private String email;

	@Column(nullable = true)
	private String senha;

	@Column(nullable = true)
	private Boolean ativo;

	@Column(name = "data_criacao", nullable = true)
	private LocalDateTime dataCriacao;

	public Usuario() {}

	public Usuario(UUID id, String email, String senha, Boolean ativo, LocalDateTime dataCriacao) {
		this.id = id;
		this.email = email;
		this.senha = senha;
		this.ativo = ativo;
		this.dataCriacao = dataCriacao;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

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

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

}
