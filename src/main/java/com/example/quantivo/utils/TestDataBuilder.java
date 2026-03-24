package com.example.quantivo.utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.quantivo.entity.ItemLista;
import com.example.quantivo.entity.ListaMensal;
import com.example.quantivo.entity.Usuario;
import com.example.quantivo.to.AdicionarItemTO;

public class TestDataBuilder {

	public static Usuario criarUsuario() {
		Usuario usuario = new Usuario();
		usuario.setId(UUID.randomUUID());
		usuario.setEmail("teste@email.com");
		usuario.setSenha("senha123");
		usuario.setAtivo(true);
		usuario.setDataCriacao(LocalDateTime.now());
		return usuario;
	}

	public static ListaMensal criarListaMensal() {
		ListaMensal lista = new ListaMensal();
		lista.setId(UUID.randomUUID());
		lista.setDataCriacao(LocalDateTime.now());
		return lista;
	}

	public static ItemLista criarItemLista() {
		ItemLista item = new ItemLista();
		item.setId(UUID.randomUUID());
		item.setNomeProduto("Produto Teste");
		item.setQuantidade(2);
		item.setValorUnitario(BigDecimal.valueOf(10.0));
		item.setValorTotal(BigDecimal.valueOf(20.0));
		item.setDataCriacao(LocalDateTime.now());
		return item;
	}

	public static AdicionarItemTO criarAdicionarItemTO() {
		AdicionarItemTO to = new AdicionarItemTO();
		to.setNomeProduto("Produto Teste");
		to.setQuantidade(2);
		to.setValorUnitario(BigDecimal.valueOf(10.0));
		return to;
	}
}
