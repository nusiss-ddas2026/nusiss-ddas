package nusiss.ddas.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auditId;

    @Column(name = "auditLogOn", nullable = false)
    private LocalDateTime auditLogOn;

    @Column(name = "actionPerformedBy", nullable = false)
    private String actionPerformedBy;

    @Column(name = "actionPerformed", nullable = false)
    private String actionPerformed;

    public AuditLog() {
    }

    public AuditLog(LocalDateTime auditLogOn, String actionPerformedBy, String actionPerformed) {
        this.auditLogOn = auditLogOn;
        this.actionPerformedBy = actionPerformedBy;
        this.actionPerformed = actionPerformed;
    }

    public Integer getAuditId() {
        return auditId;
    }

    public void setAuditId(Integer auditId) {
        this.auditId = auditId;
    }

    public LocalDateTime getAuditLogOn() {
        return auditLogOn;
    }

    public void setAuditLogOn(LocalDateTime auditLogOn) {
        this.auditLogOn = auditLogOn;
    }

    public String getActionPerformedBy() {
        return actionPerformedBy;
    }

    public void setActionPerformedBy(String actionPerformedBy) {
        this.actionPerformedBy = actionPerformedBy;
    }

    public String getActionPerformed() {
        return actionPerformed;
    }

    public void setActionPerformed(String actionPerformed) {
        this.actionPerformed = actionPerformed;
    }
}