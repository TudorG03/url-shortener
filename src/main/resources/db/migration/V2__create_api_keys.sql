CREATE TABLE api_keys (
    id            BIGSERIAL       PRIMARY KEY,
    key_hash      VARCHAR(255)    NOT NULL UNIQUE,
    owner         VARCHAR(255)    NOT NULL,
    created_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    active        BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_api_keys_key_hash ON api_keys(key_hash);
