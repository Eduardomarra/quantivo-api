package com.example.quantivo.to;

import java.math.BigDecimal;

import com.example.quantivo.entity.ItemLista;
import com.example.quantivo.entity.ListaMensal;

public class ResumoListaTO {

	private BigDecimal valorTotal;
	private Integer totalItens;

	public ResumoListaTO(ListaMensal lista) {
		this.valorTotal = lista.getItens().stream().map(ItemLista::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		this.totalItens = lista.getItens().size();
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public Integer getTotalItens() {
		return totalItens;
	}

}
