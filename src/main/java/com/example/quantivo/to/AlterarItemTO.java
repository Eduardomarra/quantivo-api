package com.example.quantivo.to;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AlterarItemTO {

	@NotBlank(message = "Informe o nome do produto")
	private String nomeProduto;

	@NotNull(message = "Informe a quantidade do produto")
	@Min(value = 1, message = "Quantidade não pode ser menor que 1")
	private Integer quantidade;

	@NotNull(message = "Informe o valor do produto")
	@Min(value = 0, message = "Valor permitido a partir de $0,00")
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
