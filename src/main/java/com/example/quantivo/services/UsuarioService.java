package com.example.quantivo.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.quantivo.entity.Usuario;
import com.example.quantivo.exception.BusinessException;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.to.UsuarioTO;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

	@Autowired private UsuarioRepository usuarioRepository;
	@Autowired private PasswordEncoder passwordEncoder;

	@Transactional
	public Page<UsuarioTO> buscarAllUsuarios(Pageable pageable){
		Page<Usuario> usuario = usuarioRepository.findAll(pageable);
		return usuario.map(UsuarioTO::new);
	}

	@Transactional
	public UsuarioTO buscarPorEmail(String email){
		Usuario usuario = usuarioRepository.findByEmail(email).get();
		return new UsuarioTO(usuario);
	}

	@Transactional
	public UsuarioTO buscarPorId(UUID id){
		Usuario usuario = usuarioRepository.findById(id).get();
		return new UsuarioTO(usuario);
	}

	@Transactional
	public UsuarioTO criarUsuario(Usuario usuario) {

		if(usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
			throw new BusinessException("Email ja cadastrado");
		}

		Usuario user = new Usuario();
		user.setAtivo(true);
		user.setDataCriacao(LocalDateTime.now());
		user.setEmail(usuario.getEmail());
		user.setSenha(passwordEncoder.encode(usuario.getSenha()));

		return new UsuarioTO(usuarioRepository.save(user));
	}

	@Transactional
	public void ativarUsuario(UUID id){
		Usuario usuario = usuarioRepository.findById(id).get();
		usuario.setAtivo(true);
		usuarioRepository.save(usuario);
	}

	@Transactional
	public void desativarUsuario(UUID id){
		Usuario usuario = usuarioRepository.findById(id).get();
		usuario.setAtivo(false);
		usuarioRepository.save(usuario);
	}

	@Transactional
	public void alterarSenha(String email, String senhaAtual, String senhaNova) {
		try {
			Usuario usuario = usuarioRepository.findByEmail(email).get();
			if(usuario != null) {
				String senhaBD = usuarioRepository.findSenhaByEmail(email).map(Usuario::getSenha).get();
				if(senhaBD.equals(senhaAtual)) {
					usuario.setSenha(senhaNova);
					usuarioRepository.save(usuario);
				} else {
					throw new Exception("Senha atual incorreta");
				}
			}
		} catch (Exception e) {
			new Exception("Senha atual incorreta.");
		}
	}

	@Transactional
	public void excluirUsuario(UUID id){
		Usuario usuario = usuarioRepository.findById(id).get();
		usuario.setAtivo(false);
		usuarioRepository.save(usuario);
	}
}
