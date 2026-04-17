package com.example.quantivo.to;

import java.util.UUID;

public class CriarListaMensalTO {

	private UUID usuarioId;
	private String descricao;

	public UUID getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(UUID usuarioId) {
		this.usuarioId = usuarioId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
