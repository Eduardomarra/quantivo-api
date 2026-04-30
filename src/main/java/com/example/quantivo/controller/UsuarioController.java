package com.example.quantivo.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quantivo.service.UsuarioService;
import com.example.quantivo.to.AlterarSenhaTO;
import com.example.quantivo.to.UsuarioTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/usuarios")
public class UsuarioController {

	@Autowired private UsuarioService usuarioService;

	@GetMapping
	public ResponseEntity<Page<UsuarioTO>> buscarAllUsuarios(Pageable pageable){
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		Page<UsuarioTO> to = usuarioService.buscarAllUsuarios(emailLogado, pageable);
		return ResponseEntity.ok(to);
	}

	@GetMapping(value = "/email/{email}")
	public ResponseEntity<UsuarioTO> buscarPorEmail(@PathVariable String email){
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(usuarioService.buscarPorEmail(emailLogado, email));
	}

	@GetMapping(value = "/id/{id}")
	public ResponseEntity<UsuarioTO> buscarPorId(@PathVariable UUID id){
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(usuarioService.buscarPorId(emailLogado, id));
	}

	@PutMapping(value = "/alterar-senha/{email}")
	public ResponseEntity<Void> alterarSenha(@PathVariable String email, @Valid @RequestBody AlterarSenhaTO senha){
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		usuarioService.alterarSenha(emailLogado, email, senha.getSenhaAtual(), senha.getSenhaNova());
		return ResponseEntity.ok().build();
	}

	@PutMapping(value = "/excluir/{id}")
	public ResponseEntity<Void> excluirUsuario(@PathVariable UUID id){
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		usuarioService.excluirUsuario(emailLogado, id);
		return ResponseEntity.ok().build();
	}

	@PutMapping(value = "/ativar/{id}")
	public ResponseEntity<Void> ativarUsuario(@PathVariable UUID id){
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		usuarioService.ativarUsuario(emailLogado, id);
		return ResponseEntity.ok().build();
	}

	@PutMapping(value = "/desativar/{id}")
	public ResponseEntity<Void> desativarUsuario(@PathVariable UUID id){
		String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
		usuarioService.desativarUsuario(emailLogado, id);
		return ResponseEntity.ok().build();
	}
}
