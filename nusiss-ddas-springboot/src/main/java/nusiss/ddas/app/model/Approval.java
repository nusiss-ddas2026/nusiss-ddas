package nusiss.ddas.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approvals")
public class Approval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer approvalId;

    @Column(name = "documentId", nullable = false)
    private Integer documentId;

    @Column(nullable = false)
    private String approver;

    @Column(columnDefinition = "VARBINARY(512)")
    private byte[] signature;

    @Column(name = "createdOn", nullable = false)
    private LocalDateTime createdOn;

    public Approval() {
    }

    public Approval(Integer documentId, String approver, byte[] signature, LocalDateTime createdOn) {
        this.documentId = documentId;
        this.approver = approver;
        this.signature = signature;
        this.createdOn = createdOn;
    }

    public Integer getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Integer approvalId) {
        this.approvalId = approvalId;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
}