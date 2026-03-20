package com.example.quantivo.to;

import java.math.BigDecimal;
import java.util.UUID;

public class ItensListaTO {

	private UUID id;
	private String nomeProduto;
	private Integer quantidade;
	private BigDecimal valorUnitario;
	private BigDecimal valorTotal;

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
