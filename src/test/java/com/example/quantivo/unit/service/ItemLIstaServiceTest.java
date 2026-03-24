package com.example.quantivo.unit.service;

import com.example.quantivo.entity.ItemLista;
import com.example.quantivo.entity.ListaMensal;
import com.example.quantivo.exception.ResourceNotFoundException;
import com.example.quantivo.repository.ItemListaReporitory;
import com.example.quantivo.repository.ListaMensalRepository;
import com.example.quantivo.services.ItemListaService;
import com.example.quantivo.to.ItemListaTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ItemListaService")
class ItemListaServiceTest {

	@Mock
	private ItemListaReporitory itemListaReporitory;

	@Mock
	private ListaMensalRepository listaMensalRepository;

	@InjectMocks
	private ItemListaService itemListaService;

	private UUID listaId;
	private ListaMensal listaMensal;
	private ItemLista item1;
	private ItemLista item2;

	@BeforeEach
	void setUp() {
		listaId = UUID.randomUUID();

		listaMensal = new ListaMensal();
		listaMensal.setId(listaId);
		listaMensal.setDataCriacao(LocalDateTime.now());

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
	}

	@Test
	@DisplayName("Deve retornar lista de itens quando lista existe e tem itens")
	void deveRetornarItensQuandoListaExiste() {
		List<ItemLista> itensEsperados = Arrays.asList(item1, item2);

		when(listaMensalRepository.findById(listaId))
				.thenReturn(Optional.of(listaMensal));
		when(itemListaReporitory.findByListaMensalId(listaId))
				.thenReturn(itensEsperados);

		List<ItemListaTO> resultado = itemListaService.getItens(listaId);

		assertThat(resultado).hasSize(2);
		assertThat(resultado.get(0).getNomeProduto()).isEqualTo("Produto 1");
		assertThat(resultado.get(1).getNomeProduto()).isEqualTo("Produto 2");

		verify(listaMensalRepository, times(1)).findById(listaId);
		verify(itemListaReporitory, times(1)).findByListaMensalId(listaId);
	}

	@Test
	@DisplayName("Deve retornar lista vazia quando lista existe mas não tem itens")
	void deveRetornarListaVaziaQuandoListaSemItens() {
		when(listaMensalRepository.findById(listaId))
				.thenReturn(Optional.of(listaMensal));
		when(itemListaReporitory.findByListaMensalId(listaId))
				.thenReturn(Collections.emptyList());

		List<ItemListaTO> resultado = itemListaService.getItens(listaId);

		assertThat(resultado).isEmpty();
	}

	@Test
	@DisplayName("Deve lançar ResourceNotFoundException quando lista não existe")
	void deveLancarExcecaoQuandoListaNaoExiste() {
		when(listaMensalRepository.findById(listaId))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> itemListaService.getItens(listaId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Lista mensal não encontrada.");

		verify(itemListaReporitory, never()).findByListaMensalId(any());
	}

	@Test
	@DisplayName("Deve mapear corretamente ItemLista para ItemListaTO")
	void deveMapearCorretamenteItemListaParaTO() {
		List<ItemLista> itens = Arrays.asList(item1);

		when(listaMensalRepository.findById(listaId))
				.thenReturn(Optional.of(listaMensal));
		when(itemListaReporitory.findByListaMensalId(listaId))
				.thenReturn(itens);

		List<ItemListaTO> resultado = itemListaService.getItens(listaId);
		ItemListaTO to = resultado.get(0);

		assertThat(to.getId()).isEqualTo(item1.getId());
		assertThat(to.getNomeProduto()).isEqualTo(item1.getNomeProduto());
		assertThat(to.getQuantidade()).isEqualTo(item1.getQuantidade());
		assertThat(to.getValorTotal()).isEqualTo(item1.getValorTotal());
	}
}
