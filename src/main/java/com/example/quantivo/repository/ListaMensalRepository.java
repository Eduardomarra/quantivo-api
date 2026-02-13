package com.example.quantivo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quantivo.entity.ListaMensal;

public interface ListaMensalRepository extends JpaRepository<ListaMensal, UUID> {

	Optional<ListaMensal> findByUsuario_IdAndMesAndAno(UUID usuarioId, Integer mes, Integer ano);
	Optional<ListaMensal> findById(UUID id);
	Optional<ListaMensal> findByUsuario_Id(UUID usuarioId);
	void deleteById(UUID id);
}
