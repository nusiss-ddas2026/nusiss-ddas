package nusiss.ddas.app.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Rows are write-once: no setters are exposed and there is no is_deleted column,
 * matching the audit log's immutability requirement.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer auditId;

    @ManyToOne
    @JoinColumn(name = "action_performed_by")
    private User actionPerformedBy;

    @Column(name = "action_performed", nullable = false)
    private String actionPerformed;

    @ManyToOne
    @JoinColumn(name = "target_doc_id")
    private Document targetDoc;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(User actionPerformedBy, String actionPerformed, Document targetDoc, String ipAddress) {
        this.actionPerformedBy = actionPerformedBy;
        this.actionPerformed = actionPerformed;
        this.targetDoc = targetDoc;
        this.ipAddress = ipAddress;
    }

    public Integer getAuditId() {
        return auditId;
    }

    public User getActionPerformedBy() {
        return actionPerformedBy;
    }

    public String getActionPerformed() {
        return actionPerformed;
    }

    public Document getTargetDoc() {
        return targetDoc;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
