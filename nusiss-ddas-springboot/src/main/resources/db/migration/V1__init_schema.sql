-- 0. DEPARTMENTS TABLE
-- Not in the original design doc, added so users.dept_id has somewhere real to point to
-- instead of being a bare, unchecked integer.
CREATE TABLE departments (
    dept_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL UNIQUE
);

-- 1. USERS TABLE
CREATE TABLE users (
    user_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role INT NOT NULL DEFAULT 1 CHECK (role IN (1, 2, 3)), -- 1: User, 2: Approver, 3: Admin
    dept_id INT REFERENCES departments(dept_id) ON DELETE SET NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. DOCUMENTS TABLE
CREATE TABLE documents (
    doc_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    doc_name VARCHAR(255) NOT NULL,
    doc_type INT NOT NULL CHECK (doc_type IN (1, 2, 3)), -- 1: Leave, 2: Procurement, 3: Medical
    uploader_id INT NOT NULL REFERENCES users(user_id),
    file_path VARCHAR(512) NOT NULL, -- Path to stored file
    file_hash VARCHAR(64) NOT NULL,  -- SHA-256 hash for integrity verification
    -- 1: Pending, 2: Approved, 3: Rejected, 4: Reviewing (extra status kept from the
    -- existing app's workflow; the original design only listed 1-3)
    status INT NOT NULL DEFAULT 1 CHECK (status IN (1, 2, 3, 4)),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. APPROVALS / SIGNATURES TABLE
CREATE TABLE document_approvals (
    approval_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    doc_id INT NOT NULL REFERENCES documents(doc_id) ON DELETE CASCADE,
    approver_id INT NOT NULL REFERENCES users(user_id),
    signature_value TEXT NOT NULL,  -- Holds Vault transit signature string
    signed_doc_hash VARCHAR(64) NOT NULL, -- Hash of document at moment of approval
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. IMMUTABLE AUDIT LOG TABLE
CREATE TABLE audit_logs (
    audit_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    action_performed_by INT REFERENCES users(user_id),
    action_performed VARCHAR(255) NOT NULL,
    target_doc_id INT REFERENCES documents(doc_id), -- Keeps track of what was modified
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- Note: NO is_deleted column! Audit logs are immutable.
);

CREATE INDEX idx_users_dept_id ON users(dept_id);
CREATE INDEX idx_documents_uploader_id ON documents(uploader_id);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_document_approvals_doc_id ON document_approvals(doc_id);
CREATE INDEX idx_audit_logs_target_doc_id ON audit_logs(target_doc_id);
