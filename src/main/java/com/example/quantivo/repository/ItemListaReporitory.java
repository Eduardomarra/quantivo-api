package com.example.quantivo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quantivo.entity.ItemLista;

public interface ItemListaReporitory extends JpaRepository<ItemLista, UUID> {
}
