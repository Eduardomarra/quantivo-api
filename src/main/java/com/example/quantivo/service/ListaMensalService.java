package com.example.quantivo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.quantivo.entity.ItemLista;
import com.example.quantivo.entity.ListaMensal;
import com.example.quantivo.entity.Usuario;
import com.example.quantivo.exception.BusinessException;
import com.example.quantivo.exception.ResourceNotFoundException;
import com.example.quantivo.repository.ItemListaReporitory;
import com.example.quantivo.repository.ListaMensalRepository;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.to.AdicionarItemTO;
import com.example.quantivo.to.AlterarItemTO;
import com.example.quantivo.to.CriarListaMensalTO;
import com.example.quantivo.to.ItemListaTO;
import com.example.quantivo.to.ListaMensalTO;
import com.example.quantivo.to.ResumoListaTO;

import jakarta.transaction.Transactional;

@Service
public class ListaMensalService {

	@Autowired private ListaMensalRepository listaMensalRepository;
	@Autowired private UsuarioRepository usuarioRepository;
	@Autowired private ItemListaReporitory itemListaReporitory;

	@Transactional
	public ListaMensalTO criarListaMensal(CriarListaMensalTO to) {
		Usuario usuario = usuarioRepository.findById(to.getUsuarioId())
				.orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

		ListaMensal lista = new ListaMensal();
		lista.setUsuario(usuario);
		lista.setAno(LocalDateTime.now().getYear());
		lista.setMes(LocalDateTime.now().getMonthValue());
		lista.setDescricao(to.getDescricao());
		lista.setDataCriacao(LocalDateTime.now());

		return new ListaMensalTO(listaMensalRepository.save(lista));
	}

	@Transactional
	public ListaMensalTO getPorId(UUID id) {
		return new ListaMensalTO(listaMensalRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Lista nao encontrada")));
	}

	@Transactional
	public ListaMensalTO getPorUsuarioIdMesAno(UUID usuarioId, Integer mes, Integer ano) {
		ListaMensal lista = listaMensalRepository.findByUsuario_IdAndMesAndAno(usuarioId, mes, ano).orElseThrow(() -> new ResourceNotFoundException("Lista não encontrada."));
		return new ListaMensalTO(lista);
	}

	@Transactional
	public List<ListaMensalTO> getPorUsuarioId(UUID usuarioId) {
		List<ListaMensal> lista = listaMensalRepository.findByUsuario_Id(usuarioId);
		return lista.stream().map(ListaMensalTO::new).toList();
	}

	@Transactional
	public void deletarLista(UUID id) {
		listaMensalRepository.deleteById(id);
	}

	@Transactional
	public ItemListaTO adicionarItem(UUID listaId, AdicionarItemTO to) {
		ListaMensal lista = listaMensalRepository.findById(listaId).orElseThrow(() -> new ResourceNotFoundException("Lista não encontrada."));

		if(to.getQuantidade() <= 0) {
			throw new BusinessException("Quantidade deve ser maior que 0.");
		}

		if(to.getValorUnitario().floatValue() < 0) {
			throw new BusinessException("Valor unitário não pode ser negativo.");
		}

		ItemLista itemLista = new ItemLista();
		itemLista.setListaMensal(lista);
		itemLista.setNomeProduto(to.getNomeProduto());
		itemLista.setQuantidade(to.getQuantidade());
		itemLista.setValorUnitario(to.getValorUnitario());
		itemLista.setValorTotal(to.getValorUnitario().multiply(new BigDecimal(to.getQuantidade())));
		itemLista.setDataCriacao(LocalDateTime.now());

		ItemLista saveItem = itemListaReporitory.save(itemLista);

		return new ItemListaTO(saveItem);
	}

	@Transactional
	public ItemListaTO alterarItem(UUID itemId, AlterarItemTO to) {
		ItemLista item = itemListaReporitory.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Item nao encontrado"));

		item.setNomeProduto(to.getNomeProduto());
		item.setQuantidade(to.getQuantidade());
		item.setValorUnitario(to.getValorUnitario());
		item.setValorTotal(to.getValorUnitario().multiply(new BigDecimal(to.getQuantidade())));

		ItemLista itemLista = itemListaReporitory.save(item);

		return new ItemListaTO(itemLista);
	}

	@Transactional
	public void deletarItem(UUID itemId) {
		itemListaReporitory.deleteById(itemId);
	}


	@Transactional
	public ResumoListaTO getResumoLista(UUID listaId) {
		ListaMensal lista = listaMensalRepository.findById(listaId).orElseThrow(() -> new ResourceNotFoundException("Lista nao encontrada"));

		lista.getItens().stream().map(ItemLista::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new ResumoListaTO(lista);
	}
}
