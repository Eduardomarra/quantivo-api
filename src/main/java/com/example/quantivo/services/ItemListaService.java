package com.example.quantivo.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quantivo.entity.ItemLista;
import com.example.quantivo.entity.ListaMensal;
import com.example.quantivo.exception.ResourceNotFoundException;
import com.example.quantivo.repository.ItemListaReporitory;
import com.example.quantivo.repository.ListaMensalRepository;
import com.example.quantivo.to.ItemListaTO;

@Service
public class ItemListaService {

	@Autowired private ItemListaReporitory itemListaReporitory;
	@Autowired private ListaMensalRepository listaMensalRepository;

	@Transactional(readOnly = true)
	public List<ItemListaTO> getItens(UUID idLista) {
		ListaMensal lista = listaMensalRepository.findById(idLista)
				.orElseThrow(() -> new ResourceNotFoundException("Lista mensal não encontrada."));

		List<ItemLista> item = itemListaReporitory.findByListaMensalId(idLista);

		return item.stream().map(ItemListaTO::new).toList();
	}
}
