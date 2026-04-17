package com.example.quantivo.to;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.quantivo.entity.ListaMensal;

public class ListaMensalTO {

	private Integer mes;
	private Integer ano;
	private String descricao;
	private LocalDateTime dataCriacao;
	private UUID usuarioId;
	private UUID idLista;
	private List<AdicionarItemTO> itens;

	public ListaMensalTO() {}

	public ListaMensalTO(ListaMensal lista){
		this.mes = lista.getMes();
		this.ano = lista.getAno();
		this.descricao = lista.getDescricao();
		this.dataCriacao = lista.getDataCriacao();
		this.usuarioId = lista.getUsuario().getId();
		this.idLista = lista.getId();
	}

	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public UUID getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(UUID usuarioId) {
		this.usuarioId = usuarioId;
	}

	public UUID getIdLista() {
		return idLista;
	}

	public void setIdLista(UUID idLista) {
		this.idLista = idLista;
	}

}
