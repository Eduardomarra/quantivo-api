package com.example.quantivo.integration.controller;

import com.example.quantivo.controller.ListaMensalController;
import com.example.quantivo.service.ListaMensalService;
import com.example.quantivo.to.*;
import com.example.quantivo.entity.ItemLista;
import com.example.quantivo.entity.ListaMensal;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ListaMensalController")
class ListaMensalControllerTest {

	@Mock
	private ListaMensalService listaMensalService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private ListaMensalController listaMensalController;

	private UUID usuarioId;
	private UUID listaId;
	private UUID itemId;
	private ListaMensalTO listaMensalTO;
	private List<ListaMensalTO> listasTO;
	private ItemListaTO itemTO;
	private CriarListaMensalTO criarListaTO;
	private AdicionarItemTO adicionarItemTO;
	private AlterarItemTO alterarItemTO;
	private String emailLogado;

	@BeforeEach
	void setUp() {
		usuarioId = UUID.randomUUID();
		listaId = UUID.randomUUID();
		itemId = UUID.randomUUID();
		emailLogado = "usuario@teste.com";

		// ListaMensalTO
		listaMensalTO = new ListaMensalTO();
		listaMensalTO.setIdLista(listaId);
		listaMensalTO.setUsuarioId(usuarioId);
		listaMensalTO.setAno(LocalDateTime.now().getYear());
		listaMensalTO.setMes(LocalDateTime.now().getMonthValue());
		listaMensalTO.setDescricao("Compras do Mês");

		// Lista de listas
		listasTO = Collections.singletonList(listaMensalTO);

		// ItemTO
		itemTO = new ItemListaTO();
		itemTO.setId(itemId);
		itemTO.setNomeProduto("Produto Teste");
		itemTO.setQuantidade(2);
		itemTO.setValorUnitario(BigDecimal.valueOf(10.00));
		itemTO.setValorTotal(BigDecimal.valueOf(20.00));

		// Request TOs
		criarListaTO = new CriarListaMensalTO();
		criarListaTO.setUsuarioId(usuarioId);
		criarListaTO.setDescricao("Compras do Mês");

		adicionarItemTO = new AdicionarItemTO();
		adicionarItemTO.setNomeProduto("Novo Produto");
		adicionarItemTO.setQuantidade(3);
		adicionarItemTO.setValorUnitario(BigDecimal.valueOf(25.00));

		alterarItemTO = new AlterarItemTO();
		alterarItemTO.setNomeProduto("Produto Alterado");
		alterarItemTO.setQuantidade(5);
		alterarItemTO.setValorUnitario(BigDecimal.valueOf(15.00));
	}

	private void mockSecurityContext() {
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(emailLogado);
	}

	@Test
	@DisplayName("Deve criar lista mensal com sucesso")
	void deveCriarListaMensalComSucesso() {
		// Arrange
		mockSecurityContext();
		when(listaMensalService.criarListaMensal(emailLogado, criarListaTO)).thenReturn(listaMensalTO);

		// Act
		ResponseEntity<ListaMensalTO> response = listaMensalController.criar(criarListaTO);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getIdLista()).isEqualTo(listaId);
		assertThat(response.getBody().getUsuarioId()).isEqualTo(usuarioId);
		assertThat(response.getBody().getDescricao()).isEqualTo("Compras do Mês");

		verify(listaMensalService, times(1)).criarListaMensal(emailLogado, criarListaTO);
	}

	@Test
	@DisplayName("Deve retornar lista quando ID existe")
	void deveRetornarListaQuandoIdExiste() {
		// Arrange
		mockSecurityContext();
		when(listaMensalService.getPorId(emailLogado, listaId)).thenReturn(listaMensalTO);

		// Act
		ResponseEntity<ListaMensalTO> response = listaMensalController.getListaPorId(listaId);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getIdLista()).isEqualTo(listaId);

		verify(listaMensalService, times(1)).getPorId(emailLogado, listaId);
	}

	@Test
	@DisplayName("Deve lançar exceção quando lista não existe")
	void deveLancarExcecaoQuandoListaNaoExiste() {
		// Arrange
		mockSecurityContext();
		when(listaMensalService.getPorId(emailLogado, listaId))
				.thenThrow(new ResourceNotFoundException("Lista nao encontrada"));

		// Act & Assert
		assertThatThrownBy(() -> listaMensalController.getListaPorId(listaId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Lista nao encontrada");

		verify(listaMensalService, times(1)).getPorId(emailLogado, listaId);
	}

	@Test
	@DisplayName("Deve retornar listas do usuário")
	void deveRetornarListasDoUsuario() {
		// Arrange
		mockSecurityContext();
		when(listaMensalService.getPorUsuarioId(emailLogado, usuarioId)).thenReturn(listasTO);

		// Act
		ResponseEntity<List<ListaMensalTO>> response = listaMensalController.getListaPorUsuarioId(usuarioId);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody().get(0).getIdLista()).isEqualTo(listaId);

		verify(listaMensalService, times(1)).getPorUsuarioId(emailLogado, usuarioId);
	}

	@Test
	@DisplayName("Deve editar a descrição da lista com sucesso")
	void deveEditarDescricaoDaListaComSucesso() {
		// Arrange
		mockSecurityContext();
		String novaDescricao = "Nova Descrição";
		when(listaMensalService.editarDescricaoLista(emailLogado, novaDescricao, listaId)).thenReturn(listaMensalTO);

		// Act
		ResponseEntity<ListaMensalTO> response = listaMensalController.editarLista(listaId, novaDescricao);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();

		verify(listaMensalService, times(1)).editarDescricaoLista(emailLogado, novaDescricao, listaId);
	}

	@Test
	@DisplayName("Deve deletar lista com sucesso")
	void deveDeletarListaComSucesso() {
		// Arrange
		mockSecurityContext();
		doNothing().when(listaMensalService).deletarLista(emailLogado, listaId);

		// Act
		ResponseEntity<Void> response = listaMensalController.deletar(listaId);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();

		verify(listaMensalService, times(1)).deletarLista(emailLogado, listaId);
	}

	@Test
	@DisplayName("Deve adicionar item com sucesso")
	void deveAdicionarItemComSucesso() {
		// Arrange
		mockSecurityContext();
		when(listaMensalService.adicionarItem(emailLogado, listaId, adicionarItemTO)).thenReturn(itemTO);

		// Act
		ResponseEntity<ItemListaTO> response = listaMensalController.adicionarItem(listaId, adicionarItemTO);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getNomeProduto()).isEqualTo("Produto Teste");

		verify(listaMensalService, times(1)).adicionarItem(emailLogado, listaId, adicionarItemTO);
	}

	@Test
	@DisplayName("Deve lançar exceção ao adicionar item com dados inválidos")
	void deveLancarExcecaoAoAdicionarItemComDadosInvalidos() {
		// Arrange
		mockSecurityContext();
		adicionarItemTO.setQuantidade(0);
		when(listaMensalService.adicionarItem(emailLogado, listaId, adicionarItemTO))
				.thenThrow(new IllegalArgumentException("Quantidade deve ser maior que 0"));

		// Act & Assert
		assertThatThrownBy(() -> listaMensalController.adicionarItem(listaId, adicionarItemTO))
				.isInstanceOf(IllegalArgumentException.class);

		verify(listaMensalService, times(1)).adicionarItem(emailLogado, listaId, adicionarItemTO);
	}

	@Test
	@DisplayName("Deve alterar item com sucesso")
	void deveAlterarItemComSucesso() {
		// Arrange
		mockSecurityContext();
		when(listaMensalService.alterarItem(emailLogado, itemId, alterarItemTO)).thenReturn(itemTO);

		// Act
		ResponseEntity<ItemListaTO> response = listaMensalController.alterarItem(itemId, alterarItemTO);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();

		verify(listaMensalService, times(1)).alterarItem(emailLogado, itemId, alterarItemTO);
	}

	@Test
	@DisplayName("Deve lançar exceção ao alterar item não existente")
	void deveLancarExcecaoAoAlterarItemNaoExistente() {
		// Arrange
		mockSecurityContext();
		when(listaMensalService.alterarItem(emailLogado, itemId, alterarItemTO))
				.thenThrow(new ResourceNotFoundException("Item nao encontrado"));

		// Act & Assert
		assertThatThrownBy(() -> listaMensalController.alterarItem(itemId, alterarItemTO))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Item nao encontrado");

		verify(listaMensalService, times(1)).alterarItem(emailLogado, itemId, alterarItemTO);
	}

	@Test
	@DisplayName("Deve deletar item com sucesso")
	void deveDeletarItemComSucesso() {
		// Arrange
		mockSecurityContext();
		doNothing().when(listaMensalService).deletarItem(emailLogado, itemId);

		// Act
		ResponseEntity<Void> response = listaMensalController.deletarItem(itemId);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();

		verify(listaMensalService, times(1)).deletarItem(emailLogado, itemId);
	}

	@Test
	@DisplayName("Deve retornar resumo da lista com sucesso")
	void deveRetornarResumoListaComSucesso() {
		// Arrange - criar a ListaMensal para o resumo
		mockSecurityContext();
		ListaMensal listaMensal = new ListaMensal();
		listaMensal.setId(listaId);

		ItemLista itemLista = new ItemLista();
		itemLista.setValorTotal(BigDecimal.valueOf(20.00));
		listaMensal.setItens(Collections.singletonList(itemLista));

		ResumoListaTO resumoTO = new ResumoListaTO(listaMensal);

		when(listaMensalService.getResumoLista(emailLogado, listaId)).thenReturn(resumoTO);

		// Act
		ResponseEntity<ResumoListaTO> response = listaMensalController.getResumoLista(listaId);

		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTotalItens()).isEqualTo(1);
		assertThat(response.getBody().getValorTotal()).isEqualTo(BigDecimal.valueOf(20.00));

		verify(listaMensalService, times(1)).getResumoLista(emailLogado, listaId);
	}

	@Test
	@DisplayName("Deve lançar exceção ao buscar resumo de lista não existente")
	void deveLancarExcecaoAoBuscarResumoListaNaoExistente() {
		// Arrange
		mockSecurityContext();
		when(listaMensalService.getResumoLista(emailLogado, listaId))
				.thenThrow(new ResourceNotFoundException("Lista nao encontrada"));

		// Act & Assert
		assertThatThrownBy(() -> listaMensalController.getResumoLista(listaId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Lista nao encontrada");

		verify(listaMensalService, times(1)).getResumoLista(emailLogado, listaId);
	}
}
