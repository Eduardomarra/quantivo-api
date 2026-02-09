package com.example.quantivo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quantivo.entity.Usuario;
import com.example.quantivo.services.UsuarioService;
import com.example.quantivo.to.AlterarSenhaTO;
import com.example.quantivo.to.UsuarioTO;


@RestController
@RequestMapping(value = "/usuarios")
public class UsuarioController {

	@Autowired private UsuarioService usuarioService;

	@GetMapping
	public List<UsuarioTO> buscarAllUsuarios(){
		return usuarioService.buscarAllUsuarios();
	}

	@GetMapping(value = "/email/{email}")
	public UsuarioTO buscarPorEmail(@PathVariable String email){
		return usuarioService.buscarPorEmail(email);
	}

	@GetMapping(value = "/id/{id}")
	public UsuarioTO buscarPorId(@PathVariable UUID id){
		return usuarioService.buscarPorId(id);
	}

	@PostMapping(value = "/criar")
	public UsuarioTO criarUsuario(@RequestBody Usuario usuario){
		return usuarioService.criarUsuario(usuario);
	}

	@PutMapping(value = "/alterar-senha/{email}")
	public void alterarSenha(@PathVariable String email, @RequestBody AlterarSenhaTO senha){
		usuarioService.alterarSenha(email, senha.getSenhaAtual(), senha.getSenhaNova());
	}

	@PutMapping(value = "/excluir/{id}")
	public void excluirUsuario(@PathVariable UUID id){
		usuarioService.excluirUsuario(id);
	}

	@PutMapping(value = "/ativar/{id}")
	public void ativarUsuario(@PathVariable UUID id){
		try {
			usuarioService.ativarUsuario(id);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@PutMapping(value = "/desativar/{id}")
	public void desativarUsuario(@PathVariable UUID id){
		try {
			usuarioService.desativarUsuario(id);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
