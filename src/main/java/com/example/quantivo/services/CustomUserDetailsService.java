package com.example.quantivo.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.quantivo.entity.Usuario;
import com.example.quantivo.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email)
			throws UsernameNotFoundException {

		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() ->
						new UsernameNotFoundException("Usuário não encontrado"));

		return org.springframework.security.core.userdetails.User
				.builder()
				.username(usuario.getEmail())
				.password(usuario.getSenha())
				.roles("USER")
				.build();
	}

}
