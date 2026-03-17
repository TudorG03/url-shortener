ALTER TABLE api_keys ADD COLUMN key_prefix VARCHAR(8);
CREATE INDEX idx_api_keys_key_prefix ON api_keys(key_prefix);
