CREATE SEQUENCE core.password_reset_token_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE core.password_reset_token (
    id BIGINT NOT NULL,
    token VARCHAR(255),
    user_id UUID NOT NULL,
    expiry_date TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES core.usuario(id)
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_user_id
    ON core.password_reset_token (user_id);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_lookup
    ON core.password_reset_token (token);
