package com.example.quantivo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.quantivo.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	@Query(nativeQuery = true, value = "SELECT * FROM CORE.USUARIO")
	List<Usuario> findAllUsuarios();

	Optional<Usuario> findById(UUID id);

	Optional<Usuario> findByEmail(String email);

	Optional<Usuario> findSenhaByEmail(String email);

}
