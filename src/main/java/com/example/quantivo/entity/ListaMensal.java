package com.example.quantivo.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.DialectOverride;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(schema = "core", name = "LISTA_MENSAL")
public class ListaMensal {

	@Id
	@GeneratedValue
	@Column(nullable = false)
	private UUID id;

	@Column(nullable = false)
	private Integer mes;

	@Column(nullable = false)
	private Integer ano;

	@Column(name = "data_criacao", nullable = false)
	private LocalDateTime dataCriacao;

	@ManyToOne(optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@OneToMany(mappedBy = "listaMensal", fetch = FetchType.LAZY)
	private List<ItemLista> itens;

	public ListaMensal() {}

	public ListaMensal(UUID id, Integer mes, Integer ano, LocalDateTime dataCriacao) {
		this.id = id;
		this.mes = mes;
		this.ano = ano;
		this.dataCriacao = dataCriacao;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

}
