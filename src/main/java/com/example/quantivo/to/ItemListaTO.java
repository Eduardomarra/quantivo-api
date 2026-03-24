package com.example.quantivo.to;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.quantivo.entity.ItemLista;

public class ItemListaTO {

	private UUID id;
	private String nomeProduto;
	private Integer quantidade;
	private BigDecimal valorUnitario;
	private BigDecimal valorTotal;

	public ItemListaTO() {}

	public ItemListaTO(ItemLista item){
		id = item.getId();
		nomeProduto = item.getNomeProduto();
		quantidade = item.getQuantidade();
		valorUnitario = item.getValorUnitario();
		valorTotal = item.getValorTotal();
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

}
