package com.example.quantivo.services;

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
import com.example.quantivo.to.ListaMensalTO;
import com.example.quantivo.to.ResumoListaTO;

import jakarta.transaction.Transactional;

@Service
public class ListaMensalService {

	@Autowired private ListaMensalRepository listaMensalRepository;
	@Autowired private UsuarioRepository usuarioRepository;
	@Autowired private ItemListaReporitory itemListaReporitory;

	@Transactional
	public ListaMensalTO criarListaMensal(UUID usuarioId) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

		Optional<ListaMensal> listaMensal = listaMensalRepository.findByUsuario_IdAndMesAndAno(usuarioId, LocalDateTime.now().getMonthValue(), LocalDateTime.now().getYear());

		if(listaMensal.isPresent()) {
			return new ListaMensalTO(listaMensal.get());
		}

		ListaMensal lista = new ListaMensal();
		lista.setUsuario(usuario);
		lista.setAno(LocalDateTime.now().getYear());
		lista.setMes(LocalDateTime.now().getMonthValue());
		lista.setDataCriacao(LocalDateTime.now());

		return new ListaMensalTO(listaMensalRepository.save(lista));
	}

	public ListaMensalTO getPorId(UUID id) {
		return new ListaMensalTO(listaMensalRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Lista nao encontrada")));
	}

	public ListaMensalTO getPorUsuarioIdMesAno(UUID usuarioId, Integer mes, Integer ano) {
		ListaMensal lista = listaMensalRepository.findByUsuario_IdAndMesAndAno(usuarioId, mes, ano).orElseThrow(() -> new ResourceNotFoundException("Lista não encontrada."));
		return new ListaMensalTO(lista);
	}

	public List<ListaMensalTO> getPorUsuarioId(UUID usuarioId) {
		Optional<ListaMensal> lista = listaMensalRepository.findByUsuario_Id(usuarioId);
		return lista.stream().map(ListaMensalTO::new).toList();
	}

	public void deletarLista(UUID id) {
		listaMensalRepository.deleteById(id);
	}

	public ListaMensalTO adicionarItem(UUID listaId, AdicionarItemTO to) {
		ListaMensal lista = listaMensalRepository.findById(listaId).orElseThrow(() -> new ResourceNotFoundException("Lista não encontrada."));

		if(to.getQuantidade() <= 0) {
			throw new BusinessException("Quantidade deve ser maior que 0.");
		}

		if(to.getValorUnitario().floatValue() <= 0) {
			throw new BusinessException("Valor unitário deve ser maior que 0.");
		}

		ItemLista itemLista = new ItemLista();
		itemLista.setListaMensal(lista);
		itemLista.setNomeProduto(to.getNomeProduto());
		itemLista.setQuantidade(to.getQuantidade());
		itemLista.setValorUnitario(to.getValorUnitario());
		itemLista.setValorTotal(to.getValorUnitario().multiply(new BigDecimal(to.getQuantidade())));
		itemLista.setDataCriacao(LocalDateTime.now());

		lista.getItens().add(itemLista);

		return new ListaMensalTO(listaMensalRepository.save(lista));
	}

	public ListaMensalTO alterarItem(UUID itemId, AlterarItemTO to) {
		ItemLista item = itemListaReporitory.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Item nao encontrado"));

		item.setNomeProduto(to.getNomeProduto());
		item.setQuantidade(to.getQuantidade());
		item.setValorUnitario(to.getValorUnitario());
		item.setValorTotal(to.getValorUnitario().multiply(new BigDecimal(to.getQuantidade())));

		itemListaReporitory.save(item);

		return new ListaMensalTO(item.getListaMensal());
	}

	public void deletarItem(UUID itemId) {
		itemListaReporitory.deleteById(itemId);
	}


	public ResumoListaTO getResumoLista(UUID listaId) {
		ListaMensal lista = listaMensalRepository.findById(listaId).orElseThrow(() -> new ResourceNotFoundException("Lista nao encontrada"));

		lista.getItens().stream().map(ItemLista::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new ResumoListaTO(lista);
	}
}
