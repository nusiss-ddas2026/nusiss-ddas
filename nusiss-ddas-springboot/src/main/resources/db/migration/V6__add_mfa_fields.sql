ALTER TABLE users ADD COLUMN mfa_secret VARCHAR(64);
ALTER TABLE users ADD COLUMN mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users ALTER COLUMN mfa_enabled DROP DEFAULT;

ALTER TABLE users
    ADD CONSTRAINT chk_users_mfa_secret_if_enabled
        CHECK (NOT mfa_enabled OR mfa_secret IS NOT NULL);
