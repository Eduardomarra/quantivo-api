package com.example.quantivo.to;

import java.math.BigDecimal;


public class AdicionarItemTO {

	private String nomeProduto;
	private Integer quantidade;
	private BigDecimal valorUnitario;

	public String getNomeProduto() {
		return nomeProduto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}
}
