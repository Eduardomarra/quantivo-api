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

	public String getNomeProduto() {
		return nomeProduto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}
}
