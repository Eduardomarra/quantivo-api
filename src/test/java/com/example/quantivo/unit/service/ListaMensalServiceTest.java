package com.example.quantivo.unit.service;

import com.example.quantivo.to.*;
import com.example.quantivo.entity.*;
import com.example.quantivo.exception.BusinessException;
import com.example.quantivo.exception.ResourceNotFoundException;
import com.example.quantivo.repository.ItemListaReporitory;
import com.example.quantivo.repository.ListaMensalRepository;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.services.ListaMensalService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ListaMensalService")
class ListaMensalServiceTest {

	@Mock
	private ListaMensalRepository listaMensalRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private ItemListaReporitory itemListaReporitory;

	@InjectMocks
	private ListaMensalService listaMensalService;

	private UUID usuarioId;
	private UUID listaId;
	private UUID itemId;
	private Usuario usuario;
	private ListaMensal listaMensal;
	private ItemLista itemLista;
	private LocalDateTime agora;

	@BeforeEach
	void setUp() {
		agora = LocalDateTime.now();
		usuarioId = UUID.randomUUID();
		listaId = UUID.randomUUID();
		itemId = UUID.randomUUID();

		usuario = new Usuario();
		usuario.setId(usuarioId);
		usuario.setEmail("usuario@teste.com");
		usuario.setSenha("senha123");
		usuario.setAtivo(true);
		usuario.setDataCriacao(agora);

		listaMensal = new ListaMensal();
		listaMensal.setId(listaId);
		listaMensal.setUsuario(usuario);
		listaMensal.setAno(agora.getYear());
		listaMensal.setMes(agora.getMonthValue());
		listaMensal.setDataCriacao(agora);
		listaMensal.setItens(new ArrayList<>());

		itemLista = new ItemLista();
		itemLista.setId(itemId);
		itemLista.setListaMensal(listaMensal);
		itemLista.setNomeProduto("Produto Teste");
		itemLista.setQuantidade(2);
		itemLista.setValorUnitario(BigDecimal.valueOf(10.00));
		itemLista.setValorTotal(BigDecimal.valueOf(20.00));
		itemLista.setDataCriacao(agora);

		listaMensal.getItens().add(itemLista);
	}

	@Test
	@DisplayName("Deve criar nova lista quando usuário não tem lista no mês")
	void deveCriarNovaListaQuandoUsuarioNaoTemListaNoMes() {
		// Arrange
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(listaMensalRepository.findByUsuario_IdAndMesAndAno(usuarioId,
				agora.getMonthValue(), agora.getYear()))
				.thenReturn(Optional.empty());
		when(listaMensalRepository.save(any(ListaMensal.class))).thenReturn(listaMensal);

		// Act
		ListaMensalTO resultado = listaMensalService.criarListaMensal(usuarioId);

		// Assert
		assertThat(resultado).isNotNull();
		assertThat(resultado.getIdLista()).isEqualTo(listaId);
		assertThat(resultado.getAno()).isEqualTo(agora.getYear());
		assertThat(resultado.getMes()).isEqualTo(agora.getMonthValue());

		verify(usuarioRepository, times(1)).findById(usuarioId);
		verify(listaMensalRepository, times(1)).findByUsuario_IdAndMesAndAno(usuarioId,
				agora.getMonthValue(), agora.getYear());
		verify(listaMensalRepository, times(1)).save(any(ListaMensal.class));
	}

	@Test
	@DisplayName("Deve retornar lista existente quando usuário já tem lista no mês")
	void deveRetornarListaExistenteQuandoUsuarioJaTemLista() {
		// Arrange
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(listaMensalRepository.findByUsuario_IdAndMesAndAno(usuarioId,
				agora.getMonthValue(), agora.getYear()))
				.thenReturn(Optional.of(listaMensal));

		// Act
		ListaMensalTO resultado = listaMensalService.criarListaMensal(usuarioId);

		// Assert
		assertThat(resultado).isNotNull();
		assertThat(resultado.getIdLista()).isEqualTo(listaId);

		verify(usuarioRepository, times(1)).findById(usuarioId);
		verify(listaMensalRepository, times(1)).findByUsuario_IdAndMesAndAno(usuarioId,
				agora.getMonthValue(), agora.getYear());
		verify(listaMensalRepository, never()).save(any(ListaMensal.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando usuário não existe ao criar lista")
	void deveLancarExcecaoQuandoUsuarioNaoExiste() {
		// Arrange
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> listaMensalService.criarListaMensal(usuarioId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Usuario nao encontrado");

		verify(listaMensalRepository, never()).findByUsuario_IdAndMesAndAno(any(), any(), any());
		verify(listaMensalRepository, never()).save(any());
	}

	@Test
	@DisplayName("Deve retornar lista quando ID existe")
	void deveRetornarListaQuandoIdExiste() {
		// Arrange
		when(listaMensalRepository.findById(listaId)).thenReturn(Optional.of(listaMensal));

		// Act
		ListaMensalTO resultado = listaMensalService.getPorId(listaId);

		// Assert
		assertThat(resultado).isNotNull();
		assertThat(resultado.getIdLista()).isEqualTo(listaId);
		assertThat(resultado.getUsuarioId()).isEqualTo(usuarioId);

		verify(listaMensalRepository, times(1)).findById(listaId);
	}

	@Test
	@DisplayName("Deve lançar exceção quando ID da lista não existe")
	void deveLancarExcecaoQuandoIdListaNaoExiste() {
		// Arrange
		when(listaMensalRepository.findById(listaId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> listaMensalService.getPorId(listaId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Lista nao encontrada");
	}

	@Test
	@DisplayName("Deve retornar lista quando usuário, mês e ano existem")
	void deveRetornarListaQuandoUsuarioMesAnoExistem() {
		// Arrange
		Integer mes = agora.getMonthValue();
		Integer ano = agora.getYear();

		when(listaMensalRepository.findByUsuario_IdAndMesAndAno(usuarioId, mes, ano))
				.thenReturn(Optional.of(listaMensal));

		// Act
		ListaMensalTO resultado = listaMensalService.getPorUsuarioIdMesAno(usuarioId, mes, ano);

		// Assert
		assertThat(resultado).isNotNull();
		assertThat(resultado.getIdLista()).isEqualTo(listaId);

		verify(listaMensalRepository, times(1))
				.findByUsuario_IdAndMesAndAno(usuarioId, mes, ano);
	}

	@Test
	@DisplayName("Deve lançar exceção quando lista não encontrada por usuário, mês e ano")
	void deveLancarExcecaoQuandoListaNaoEncontradaPorUsuarioMesAno() {
		Integer mes = 12;
		Integer ano = 2025;

		when(listaMensalRepository.findByUsuario_IdAndMesAndAno(usuarioId, mes, ano))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> listaMensalService.getPorUsuarioIdMesAno(usuarioId, mes, ano))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Lista não encontrada.");
	}

	@Test
	@DisplayName("Deve retornar lista de listas do usuário")
	void deveRetornarListasDoUsuario() {
		List<ListaMensal> listas = Arrays.asList(listaMensal);
		when(listaMensalRepository.findByUsuario_Id(usuarioId))
				.thenReturn(listas);  // Mantém Optional.of()

		List<ListaMensalTO> resultado = listaMensalService.getPorUsuarioId(usuarioId);

		assertThat(resultado).hasSize(1);
		assertThat(resultado.get(0).getIdLista()).isEqualTo(listaId);

		verify(listaMensalRepository, times(1)).findByUsuario_Id(usuarioId);
	}

	@Test
	@DisplayName("Deve retornar lista vazia quando usuário não tem listas")
	void deveRetornarListaVaziaQuandoUsuarioSemListas() {
		when(listaMensalRepository.findByUsuario_Id(usuarioId))
				.thenReturn(List.of());

		List<ListaMensalTO> resultado = listaMensalService.getPorUsuarioId(usuarioId);

		assertThat(resultado).isEmpty();
	}

	@Test
	@DisplayName("Deve adicionar item com sucesso")
	void deveAdicionarItemComSucesso() {
		// Arrange
		AdicionarItemTO to = new AdicionarItemTO();
		to.setNomeProduto("Novo Produto");
		to.setQuantidade(3);
		to.setValorUnitario(BigDecimal.valueOf(25.00));

		ItemLista novoItem = new ItemLista();
		novoItem.setId(UUID.randomUUID());
		novoItem.setNomeProduto(to.getNomeProduto());
		novoItem.setQuantidade(to.getQuantidade());
		novoItem.setValorUnitario(to.getValorUnitario());
		novoItem.setValorTotal(to.getValorUnitario().multiply(BigDecimal.valueOf(to.getQuantidade())));

		when(listaMensalRepository.findById(listaId)).thenReturn(Optional.of(listaMensal));
		when(itemListaReporitory.save(any(ItemLista.class))).thenReturn(novoItem);

		// Act
		ItemListaTO resultado = listaMensalService.adicionarItem(listaId, to);

		// Assert
		assertThat(resultado).isNotNull();
		assertThat(resultado.getNomeProduto()).isEqualTo("Novo Produto");
		assertThat(resultado.getQuantidade()).isEqualTo(3);
		assertThat(resultado.getValorTotal()).isEqualTo(BigDecimal.valueOf(75.00));

		verify(itemListaReporitory, times(1)).save(any(ItemLista.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando quantidade é menor ou igual a zero")
	void deveLancarExcecaoQuandoQuantidadeInvalida() {
		// Arrange
		AdicionarItemTO to = new AdicionarItemTO();
		to.setQuantidade(0);
		to.setValorUnitario(BigDecimal.TEN);

		when(listaMensalRepository.findById(listaId)).thenReturn(Optional.of(listaMensal));

		// Act & Assert
		assertThatThrownBy(() -> listaMensalService.adicionarItem(listaId, to))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Quantidade deve ser maior que 0.");

		verify(itemListaReporitory, never()).save(any());
	}

	@Test
	@DisplayName("Deve lançar exceção quando valor unitário é menor ou igual a zero")
	void deveLancarExcecaoQuandoValorUnitarioInvalido() {
		// Arrange
		AdicionarItemTO to = new AdicionarItemTO();
		to.setQuantidade(5);
		to.setValorUnitario(BigDecimal.ZERO);

		when(listaMensalRepository.findById(listaId)).thenReturn(Optional.of(listaMensal));

		// Act & Assert
		assertThatThrownBy(() -> listaMensalService.adicionarItem(listaId, to))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Valor unitário deve ser maior que 0.");
	}

	@Test
	@DisplayName("Deve alterar item com sucesso")
	void deveAlterarItemComSucesso() {
		// Arrange
		AlterarItemTO to = new AlterarItemTO();
		to.setNomeProduto("Produto Alterado");
		to.setQuantidade(5);
		to.setValorUnitario(BigDecimal.valueOf(15.00));

		when(itemListaReporitory.findById(itemId)).thenReturn(Optional.of(itemLista));
		when(itemListaReporitory.save(any(ItemLista.class))).thenReturn(itemLista);

		// Act
		ItemListaTO resultado = listaMensalService.alterarItem(itemId, to);

		// Assert
		assertThat(resultado).isNotNull();
		assertThat(resultado.getNomeProduto()).isEqualTo("Produto Alterado");
		assertThat(resultado.getQuantidade()).isEqualTo(5);
		assertThat(resultado.getValorUnitario()).isEqualTo(BigDecimal.valueOf(15.00));
		assertThat(resultado.getValorTotal()).isEqualTo(BigDecimal.valueOf(75.00));

		verify(itemListaReporitory, times(1)).save(any(ItemLista.class));
	}

	@Test
	@DisplayName("Deve lançar exceção quando item não existe ao alterar")
	void deveLancarExcecaoQuandoItemNaoExisteAoAlterar() {
		AlterarItemTO to = new AlterarItemTO();
		when(itemListaReporitory.findById(itemId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> listaMensalService.alterarItem(itemId, to))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Item nao encontrado");
	}

	@Test
	@DisplayName("Deve deletar item com sucesso")
	void deveDeletarItemComSucesso() {
		listaMensalService.deletarItem(itemId);

		verify(itemListaReporitory, times(1)).deleteById(itemId);
	}

	@Test
	@DisplayName("Deve deletar lista com sucesso")
	void deveDeletarListaComSucesso() {
		listaMensalService.deletarLista(listaId);

		verify(listaMensalRepository, times(1)).deleteById(listaId);
	}

	@Test
	@DisplayName("Deve retornar resumo da lista com itens")
	void deveRetornarResumoListaComItens() {
		when(listaMensalRepository.findById(listaId)).thenReturn(Optional.of(listaMensal));

		ResumoListaTO resultado = listaMensalService.getResumoLista(listaId);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getTotalItens()).isEqualTo(1);
		assertThat(resultado.getValorTotal()).isEqualTo(BigDecimal.valueOf(20.00));
	}

	@Test
	@DisplayName("Deve retornar resumo da lista vazia quando não tem itens")
	void deveRetornarResumoListaVazia() {
		listaMensal.setItens(new ArrayList<>());
		when(listaMensalRepository.findById(listaId)).thenReturn(Optional.of(listaMensal));

		ResumoListaTO resultado = listaMensalService.getResumoLista(listaId);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getTotalItens()).isZero();
		assertThat(resultado.getValorTotal()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Deve lançar exceção quando lista não existe para resumo")
	void deveLancarExcecaoQuandoListaNaoExisteParaResumo() {
		when(listaMensalRepository.findById(listaId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> listaMensalService.getResumoLista(listaId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Lista nao encontrada");
	}
}
