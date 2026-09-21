CREATE TABLE IF NOT EXISTS jwt_tokens (
    token_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id),
    token VARCHAR(2000) NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    is_valid BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_jwt_tokens_user_id ON jwt_tokens(user_id);
CREATE INDEX idx_jwt_tokens_token ON jwt_tokens(token);
CREATE INDEX idx_jwt_tokens_valid ON jwt_tokens(is_valid);