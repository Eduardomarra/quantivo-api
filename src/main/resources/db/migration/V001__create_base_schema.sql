CREATE SCHEMA core;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE core.usuario(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    email varchar(255) NOT NULL UNIQUE,
    senha varchar(255) NOT NULL,
    data_criacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    ativo BOOLEAN NOT NULL
);

CREATE TABLE core.lista_mensal(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    usuario_id uuid NOT NULL REFERENCES core.usuario(id) ON DELETE CASCADE,
    mes INTEGER NOT NULL,
    ano INTEGER NOT NULL,
    data_criacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    UNIQUE (usuario_id, mes, ano),
    CHECK (mes BETWEEN 1 AND 12)
);

CREATE TABLE core.item_lista(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    lista_id uuid NOT NULL REFERENCES core.lista_mensal(id) ON DELETE CASCADE,
    nome_produto varchar(255) NOT NULL,
    quantidade INTEGER NOT NULL,
    valor_unitario NUMERIC(10,2) NOT NULL,
    valor_total NUMERIC(10,2) NOT NULL,
    data_criacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CHECK (quantidade > 0),
    CHECK (valor_unitario >= 0),
    CHECK (valor_total >= 0)
);

CREATE INDEX idx_usuario_email ON core.usuario(email);
CREATE INDEX idx_lista_mensal_usuario_id ON core.lista_mensal(usuario_id);
CREATE INDEX idx_item_lista_lista_id ON core.item_lista(lista_id);
