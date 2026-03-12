CREATE TABLE urls (
    id            BIGSERIAL       PRIMARY KEY,
    short_code    VARCHAR(10)     NOT NULL UNIQUE,
    original_url  TEXT            NOT NULL,
    created_by    VARCHAR(255)    NOT NULL,
    click_count   BIGINT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    expires_at    TIMESTAMP,
    active        BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_urls_short_code ON urls(short_code);
CREATE INDEX idx_urls_created_by ON urls(created_by);
