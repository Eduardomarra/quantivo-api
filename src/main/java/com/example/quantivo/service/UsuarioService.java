package com.example.quantivo.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.quantivo.entity.Usuario;
import com.example.quantivo.exception.BusinessException;
import com.example.quantivo.exception.ResourceNotFoundException;
import com.example.quantivo.repository.UsuarioRepository;
import com.example.quantivo.to.UsuarioCreateTO;
import com.example.quantivo.to.UsuarioTO;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

	@Autowired private UsuarioRepository usuarioRepository;
	@Autowired private PasswordEncoder passwordEncoder;

	@Transactional
	public Page<UsuarioTO> buscarAllUsuarios(String emailLogado, Pageable pageable){
		throw new BusinessException("Acesso negado. Não é permitido listar todos os usuários.");
	}

	@Transactional
	public UsuarioTO buscarPorEmail(String emailLogado, String email){
		if (!emailLogado.equals(email)) {
			throw new BusinessException("Acesso negado. Você não tem permissão para visualizar este usuário.");
		}
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
		return new UsuarioTO(usuario);
	}

	@Transactional
	public UsuarioTO buscarPorId(String emailLogado, UUID id){
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
		
		if (!emailLogado.equals(usuario.getEmail())) {
			throw new BusinessException("Acesso negado. Você não tem permissão para visualizar este usuário.");
		}
		
		return new UsuarioTO(usuario);
	}

	@Transactional
	public UsuarioTO criarUsuario(UsuarioCreateTO usuario) {

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
	public void ativarUsuario(String emailLogado, UUID id){
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
		
		if (!emailLogado.equals(usuario.getEmail())) {
			throw new BusinessException("Você não tem permissão para alterar o status deste usuário.");
		}
		
		usuario.setAtivo(true);
		usuarioRepository.save(usuario);
	}

	@Transactional
	public void desativarUsuario(String emailLogado, UUID id){
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
		
		if (!emailLogado.equals(usuario.getEmail())) {
			throw new BusinessException("Você não tem permissão para alterar o status deste usuário.");
		}
		
		usuario.setAtivo(false);
		usuarioRepository.save(usuario);
	}

	@Transactional
	public void alterarSenha(String emailLogado, String emailAlvo, String senhaAtual, String senhaNova) {
		if (!emailLogado.equals(emailAlvo)) {
			throw new BusinessException("Você não tem permissão para alterar a senha deste usuário.");
		}

		Usuario usuario = usuarioRepository.findByEmail(emailAlvo)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
			throw new BusinessException("Senha atual incorreta");
		}

		usuario.setSenha(passwordEncoder.encode(senhaNova));
		usuarioRepository.save(usuario);
	}

	@Transactional
	public void excluirUsuario(String emailLogado, UUID id){
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
		
		if (!emailLogado.equals(usuario.getEmail())) {
			throw new BusinessException("Você não tem permissão para excluir este usuário.");
		}

		usuario.setAtivo(false);
		usuarioRepository.save(usuario);
	}
}
