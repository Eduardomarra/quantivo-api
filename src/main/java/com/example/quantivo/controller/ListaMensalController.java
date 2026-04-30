package com.example.quantivo.controller;


import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quantivo.service.ListaMensalService;
import com.example.quantivo.to.AdicionarItemTO;
import com.example.quantivo.to.AlterarItemTO;
import com.example.quantivo.to.CriarListaMensalTO;
import com.example.quantivo.to.ItemListaTO;
import com.example.quantivo.to.ListaMensalTO;
import com.example.quantivo.to.ResumoListaTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/lista-mensal")
public class ListaMensalController {

	@Autowired private ListaMensalService listaMensalService;

	@PostMapping(value = "/criar")
	public ResponseEntity<ListaMensalTO> criar(@RequestBody CriarListaMensalTO to) {
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(listaMensalService.criarListaMensal(emailLogado, to));
	}

	@GetMapping(value = "/lista-id/{id}")
	public ResponseEntity<ListaMensalTO> getListaPorId(@PathVariable UUID id) {
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(listaMensalService.getPorId(emailLogado, id));
	}

	@GetMapping(value = "/usuario-id/{id}")
	public ResponseEntity<List<ListaMensalTO>> getListaPorUsuarioId(@PathVariable UUID id) {
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(listaMensalService.getPorUsuarioId(emailLogado, id));
	}

	@PutMapping(value = "/editar-lista/{id}")
	public ResponseEntity<ListaMensalTO> editarLista(@PathVariable UUID id, @Valid @RequestBody String descricao ) {
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(listaMensalService.editarDescricaoLista(emailLogado, descricao, id));
	}

	@DeleteMapping(value = "/deletar/{id}")
	public ResponseEntity<Void> deletar(@PathVariable UUID id) {
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		listaMensalService.deletarLista(emailLogado, id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{listaId}/itens")
	public ResponseEntity<ItemListaTO> adicionarItem(@PathVariable UUID listaId, @Valid @RequestBody AdicionarItemTO to) {
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(listaMensalService.adicionarItem(emailLogado, listaId, to));
	}

	@PutMapping(value = "/itens/{itemId}")
	public ResponseEntity<ItemListaTO> alterarItem(@PathVariable UUID itemId, @Valid @RequestBody AlterarItemTO to) {
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(listaMensalService.alterarItem(emailLogado, itemId, to));
	}

	@DeleteMapping(value = "/deletar-item/{id}")
	public ResponseEntity<Void> deletarItem(@PathVariable UUID id) {
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		listaMensalService.deletarItem(emailLogado, id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = "/resumo/{listaId}")
	public ResponseEntity<ResumoListaTO> getResumoLista(@PathVariable UUID listaId) {
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(listaMensalService.getResumoLista(emailLogado, listaId));
	}
}
