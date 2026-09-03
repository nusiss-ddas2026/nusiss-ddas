package nusiss.ddas.app.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "document_approvals")
public class DocumentApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Integer approvalId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @ManyToOne(optional = false)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Column(name = "signature_value", nullable = false, columnDefinition = "TEXT")
    private String signatureValue;

    @Column(name = "signed_doc_hash", nullable = false, length = 64)
    private String signedDocHash;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public DocumentApproval() {
    }

    public DocumentApproval(Document document, User approver, String signatureValue, String signedDocHash) {
        this.document = document;
        this.approver = approver;
        this.signatureValue = signatureValue;
        this.signedDocHash = signedDocHash;
    }

    public Integer getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Integer approvalId) {
        this.approvalId = approvalId;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public String getSignatureValue() {
        return signatureValue;
    }

    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
    }

    public String getSignedDocHash() {
        return signedDocHash;
    }

    public void setSignedDocHash(String signedDocHash) {
        this.signedDocHash = signedDocHash;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
