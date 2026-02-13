package com.example.quantivo.to;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.quantivo.entity.ListaMensal;

public class ListaMensalTO {

	private Integer mes;
	private Integer ano;
	private LocalDateTime dataCriacao;
	private UUID usuarioId;
	private List<AdicionarItemTO> itens;

	public ListaMensalTO() {}

	public ListaMensalTO(ListaMensal lista){
		this.mes = lista.getMes();
		this.ano = lista.getAno();
		this.dataCriacao = lista.getDataCriacao();
		this.usuarioId = lista.getUsuario().getId();
	}

	public Integer getMes() {
		return mes;
	}

	public Integer getAno() {
		return ano;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public UUID getUsuarioId() {
		return usuarioId;
	}
}
