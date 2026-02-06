-- =========================
-- Tabela: usuario
-- =========================
CREATE TABLE IF NOT EXISTS core.USUARIO (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    ativo BOOLEAN NOT NULL
);

-- =========================
-- Tabela: lista_mensal
-- =========================
CREATE TABLE IF NOT EXISTS core.LISTA_MENSAL (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    usuario_id UUID NOT NULL,
    mes INTEGER NOT NULL,
    ano INTEGER NOT NULL,
    data_criacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_lista_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES core.usuario(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_lista_usuario_mes_ano
        UNIQUE (usuario_id, mes, ano),

    CONSTRAINT ck_lista_mes
        CHECK (mes BETWEEN 1 AND 12)
);

-- =========================
-- Tabela: item_lista
-- =========================
CREATE TABLE IF NOT EXISTS core.ITEM_LISTA (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    lista_id UUID NOT NULL,
    nome_produto VARCHAR(255) NOT NULL,
    quantidade INTEGER NOT NULL,
    valor_unitario NUMERIC(10,2) NOT NULL,
    valor_total NUMERIC(10,2) NOT NULL,
    data_criacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_item_lista
        FOREIGN KEY (lista_id)
        REFERENCES core.lista_mensal(id)
        ON DELETE CASCADE,

    CONSTRAINT ck_item_quantidade
        CHECK (quantidade > 0),

    CONSTRAINT ck_item_valor_unitario
        CHECK (valor_unitario >= 0),

    CONSTRAINT ck_item_valor_total
        CHECK (valor_total >= 0)
);
