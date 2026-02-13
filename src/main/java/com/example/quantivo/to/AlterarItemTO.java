package com.example.quantivo.to;

import java.math.BigDecimal;

public class AlterarItemTO {

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
