ALTER TABLE document_approvals
    ADD COLUMN signing_key_name VARCHAR(255) NOT NULL DEFAULT 'document-signing';

ALTER TABLE document_approvals
    ADD CONSTRAINT chk_document_approvals_signing_key_name_not_blank
        CHECK (signing_key_name <> '');

ALTER TABLE document_approvals ALTER COLUMN signing_key_name DROP DEFAULT;
