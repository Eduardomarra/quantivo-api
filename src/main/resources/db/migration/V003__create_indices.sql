-- Índice para login
CREATE INDEX IF NOT EXISTS idx_usuario_email
    ON core.usuario (email);

-- Índice para busca de listas por usuário
CREATE INDEX IF NOT EXISTS idx_lista_mensal_usuario_id
    ON core.lista_mensal (usuario_id);

-- Índice para busca de itens por lista
CREATE INDEX IF NOT EXISTS idx_item_lista_lista_id
    ON core.item_lista (lista_id);
