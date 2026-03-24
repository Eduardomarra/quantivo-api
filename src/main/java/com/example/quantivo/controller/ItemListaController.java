package com.example.quantivo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quantivo.services.ItemListaService;
import com.example.quantivo.to.ItemListaTO;

@RestController
@RequestMapping(value = "/item-lista")
public class ItemListaController {

	@Autowired private ItemListaService itemListaService;

	@GetMapping(value = "/itens/{idLista}")
	public ResponseEntity<List<ItemListaTO>> getItem(@PathVariable UUID idLista){

		if (idLista == null) {
			throw new IllegalArgumentException("ID da lista não pode ser nulo");
		}

		return ResponseEntity.ok(itemListaService.getItens(idLista));
	}
}
