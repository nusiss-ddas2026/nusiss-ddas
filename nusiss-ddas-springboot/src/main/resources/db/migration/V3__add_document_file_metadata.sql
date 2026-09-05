ALTER TABLE documents
    ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN content_type VARCHAR(100) NOT NULL DEFAULT 'application/octet-stream';

ALTER TABLE documents
    ADD CONSTRAINT chk_documents_file_size_non_negative CHECK (file_size >= 0);

ALTER TABLE documents ALTER COLUMN file_size DROP DEFAULT;
ALTER TABLE documents ALTER COLUMN content_type DROP DEFAULT;
