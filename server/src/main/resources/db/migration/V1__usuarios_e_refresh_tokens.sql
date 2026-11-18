-- Identidade e acesso (docs/16-modelo-dados.md §2): tabelas `usuarios` e `refresh_tokens`.

-- Necessária para migrações futuras (índices GIST compostos); mais simples fixar já aqui.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TYPE papel_usuario AS ENUM ('ATLETA', 'ORGANIZADOR', 'GESTOR_ARENA', 'OPERADOR_ARENA');

CREATE TABLE usuarios (
    id                  uuid PRIMARY KEY,
    nome                text NOT NULL,
    celular             text NOT NULL,
    email               text,
    cpf                 text,
    celular_verificado  boolean NOT NULL DEFAULT true,
    papeis              papel_usuario[] NOT NULL DEFAULT ARRAY['ATLETA']::papel_usuario[],
    arena_id            uuid,
    avatar_url          text,
    criado_em           timestamptz NOT NULL
);

CREATE UNIQUE INDEX usuarios_celular_unico ON usuarios (celular);

CREATE TABLE refresh_tokens (
    id                  uuid PRIMARY KEY,
    usuario_id          uuid NOT NULL REFERENCES usuarios (id),
    token_hash          text NOT NULL,
    criado_em           timestamptz NOT NULL,
    expira_em           timestamptz NOT NULL,
    revogado_em         timestamptz,
    substituido_por_id  uuid REFERENCES refresh_tokens (id)
);
