package com.example.quantivo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "core", name = "ITEM_LISTA")
public class ItemLista {

	@Id
	@GeneratedValue
	private UUID id;

	@Column(name= "nome_produto", nullable = false)
	private String nomeProduto;

	@Column(nullable = false)
	private Integer quantidade;

	@Column(name= "valor_unitario", nullable = false)
	private BigDecimal valorUnitario;

	@Column(name= "valor_total", nullable = false)
	private BigDecimal valorTotal;

	@Column(name= "data_criacao", nullable = false)
	private LocalDateTime dataCriacao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lista_id", nullable = false)
	private ListaMensal listaMensal;

	public ItemLista() {}

	public ItemLista(UUID id, String nomeProduto, Integer quantidade, BigDecimal valorUnitario, BigDecimal valorTotal, LocalDateTime dataCriacao) {
		this.id = id;
		this.nomeProduto = nomeProduto;
		this.quantidade = quantidade;
		this.valorUnitario = valorUnitario;
		this.valorTotal = valorTotal;
		this.dataCriacao = dataCriacao;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getNomeProduto() {
		return nomeProduto;
	}

	public void setNomeProduto(String nomeProduto) {
		this.nomeProduto = nomeProduto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public ListaMensal getListaMensal() {
		return listaMensal;
	}

	public void setListaMensal(ListaMensal listaMensal) {
		this.listaMensal = listaMensal;
	}

}
