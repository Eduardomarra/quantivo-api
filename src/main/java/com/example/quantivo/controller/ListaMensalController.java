package com.example.quantivo.controller;


import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quantivo.services.ListaMensalService;
import com.example.quantivo.to.AdicionarItemTO;
import com.example.quantivo.to.AlterarItemTO;
import com.example.quantivo.to.CriarListaMensalTO;
import com.example.quantivo.to.ListaMensalTO;
import com.example.quantivo.to.ResumoListaTO;

@RestController
@RequestMapping(value = "/lista-mensal")
public class ListaMensalController {

	@Autowired private ListaMensalService listaMensalService;

	@PostMapping(value = "/criar")
	public ResponseEntity<ListaMensalTO> criar(@RequestBody CriarListaMensalTO to) {
		return ResponseEntity.ok(listaMensalService.criarListaMensal(to.getUsuarioId()));
	}

	@GetMapping(value = "/lista-id/{id}")
	public ResponseEntity<ListaMensalTO> getListaPorId(@PathVariable UUID id) {
		return ResponseEntity.ok(listaMensalService.getPorId(id));
	}

	@GetMapping(value = "/usuario-id/{id}")
	public ResponseEntity<List<ListaMensalTO>> getListaPorUsuarioId(@PathVariable UUID id) {
		return ResponseEntity.ok(listaMensalService.getPorUsuarioId(id));
	}

	@DeleteMapping(value = "/deletar/{id}")
	public ResponseEntity<Void> deletar(@PathVariable UUID id) {
		listaMensalService.deletarLista(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{listaId}/itens")
	public ResponseEntity<ListaMensalTO> adicionarItem(@PathVariable UUID listaId, @RequestBody AdicionarItemTO to) {
		return ResponseEntity.ok(listaMensalService.adicionarItem(listaId, to));
	}

	@PutMapping(value = "/itens/{itemId}")
	public ResponseEntity<ListaMensalTO> alterarItem(@PathVariable UUID itemId, @RequestBody AlterarItemTO to) {
		return ResponseEntity.ok(listaMensalService.alterarItem(itemId, to));
	}

	@DeleteMapping(value = "/deletar-item/{id}")
	public ResponseEntity<Void> deletarItem(@PathVariable UUID id) {
		listaMensalService.deletarItem(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = "/resumo/{listaId}")
	public ResponseEntity<ResumoListaTO> getResumoLista(@PathVariable UUID listaId) {
		return ResponseEntity.ok(listaMensalService.getResumoLista(listaId));
	}
}
