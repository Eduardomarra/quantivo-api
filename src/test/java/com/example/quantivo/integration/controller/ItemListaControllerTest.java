package com.example.quantivo.integration.controller;

import com.example.quantivo.controller.ItemListaController;
import com.example.quantivo.entity.ItemLista;
import com.example.quantivo.entity.ListaMensal;
import com.example.quantivo.service.ItemListaService;
import com.example.quantivo.to.ItemListaTO;
import com.example.quantivo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ItemListaController")
class ItemListaControllerTest {

	@Mock
	private ItemListaService itemListaService;

	@InjectMocks
	private ItemListaController itemListaController;

	private UUID listaId;
	private List<ItemListaTO> itens;
	private ItemLista item1;
	private ItemLista item2;
	private ListaMensal listaMensal;

	@BeforeEach
	void setUp() {
		listaId = UUID.randomUUID();

		listaMensal = new ListaMensal();
		listaMensal.setId(listaId);
		listaMensal.setDataCriacao(LocalDateTime.now());

		// Criando entidades ItemLista
		item1 = new ItemLista();
		item1.setId(UUID.randomUUID());
		item1.setNomeProduto("Produto 1");
		item1.setQuantidade(2);
		item1.setValorUnitario(BigDecimal.valueOf(10.00));
		item1.setValorTotal(BigDecimal.valueOf(20.00));
		item1.setDataCriacao(LocalDateTime.now());
		item1.setListaMensal(listaMensal);

		item2 = new ItemLista();
		item2.setId(UUID.randomUUID());
		item2.setNomeProduto("Produto 2");
		item2.setQuantidade(3);
		item2.setValorUnitario(BigDecimal.valueOf(15.00));
		item2.setValorTotal(BigDecimal.valueOf(45.00));
		item2.setDataCriacao(LocalDateTime.now());
		item2.setListaMensal(listaMensal);

		// Criando TOs a partir das entidades
		itens = Arrays.asList(
				new ItemListaTO(item1),
				new ItemListaTO(item2)
		);
	}

	@Test
	@DisplayName("Deve retornar lista de itens quando lista existe")
	void deveRetornarListaDeItensQuandoListaExiste() {
		when(itemListaService.getItens(listaId)).thenReturn(itens);

		ResponseEntity<List<ItemListaTO>> response = itemListaController.getItem(listaId);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody().get(0).getNomeProduto()).isEqualTo("Produto 1");
		assertThat(response.getBody().get(1).getNomeProduto()).isEqualTo("Produto 2");

		verify(itemListaService, times(1)).getItens(listaId);
	}

	@Test
	@DisplayName("Deve retornar lista vazia quando lista não tem itens")
	void deveRetornarListaVaziaQuandoListaSemItens() {
		when(itemListaService.getItens(listaId)).thenReturn(Collections.emptyList());

		ResponseEntity<List<ItemListaTO>> response = itemListaController.getItem(listaId);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).isEmpty();

		verify(itemListaService, times(1)).getItens(listaId);
	}

	@Test
	@DisplayName("Deve lançar exceção quando lista não existe")
	void deveLancarExcecaoQuandoListaNaoExiste() {
		when(itemListaService.getItens(listaId))
				.thenThrow(new ResourceNotFoundException("Lista mensal não encontrada."));

		assertThatThrownBy(() -> itemListaController.getItem(listaId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Lista mensal não encontrada.");

		verify(itemListaService, times(1)).getItens(listaId);
	}

	@Test
	@DisplayName("Deve lançar exceção quando ID da lista é nulo")
	void deveLancarExcecaoQuandoIdListaNulo() {
		assertThatThrownBy(() -> itemListaController.getItem(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("ID da lista não pode ser nulo");

		verify(itemListaService, never()).getItens(any());
	}

	@Test
	@DisplayName("Deve retornar ResponseEntity com status 200 OK")
	void deveRetornarStatus200Ok() {
		when(itemListaService.getItens(listaId)).thenReturn(itens);

		ResponseEntity<List<ItemListaTO>> response = itemListaController.getItem(listaId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(itens);
	}

	@Test
	@DisplayName("Deve retornar itens com dados corretos")
	void deveRetornarItensComDadosCorretos() {
		when(itemListaService.getItens(listaId)).thenReturn(itens);

		ResponseEntity<List<ItemListaTO>> response = itemListaController.getItem(listaId);
		List<ItemListaTO> itensRetornados = response.getBody();

		assertThat(itensRetornados).hasSize(2);

		ItemListaTO primeiroItem = itensRetornados.get(0);
		assertThat(primeiroItem.getId()).isEqualTo(item1.getId());
		assertThat(primeiroItem.getNomeProduto()).isEqualTo("Produto 1");
		assertThat(primeiroItem.getQuantidade()).isEqualTo(2);
		assertThat(primeiroItem.getValorUnitario()).isEqualTo(BigDecimal.valueOf(10.00));
		assertThat(primeiroItem.getValorTotal()).isEqualTo(BigDecimal.valueOf(20.00));

		ItemListaTO segundoItem = itensRetornados.get(1);
		assertThat(segundoItem.getNomeProduto()).isEqualTo("Produto 2");
		assertThat(segundoItem.getQuantidade()).isEqualTo(3);
	}
}
