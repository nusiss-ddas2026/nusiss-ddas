-- Adds file size and content-type metadata to documents. Needed so the
-- application can validate the declared content-type server-side at upload
-- time and so the UI can display file size, neither of which is derivable
-- from the existing columns (file_path/file_hash only).
ALTER TABLE documents
    ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN content_type VARCHAR(100) NOT NULL DEFAULT 'application/octet-stream';

-- Guard against negative sizes at the DB layer. content_type is deliberately
-- NOT constrained to a fixed CHECK(...) list here: it's a MIME string, not a
-- small closed enum like doc_type/status. The type/extension whitelist is
-- enforced in FileStorageService instead.
ALTER TABLE documents
    ADD CONSTRAINT chk_documents_file_size_non_negative CHECK (file_size >= 0);

-- Drop the defaults now that any pre-existing rows are backfilled; new
-- inserts must supply real values, matching file_path / file_hash.
ALTER TABLE documents ALTER COLUMN file_size DROP DEFAULT;
ALTER TABLE documents ALTER COLUMN content_type DROP DEFAULT;
