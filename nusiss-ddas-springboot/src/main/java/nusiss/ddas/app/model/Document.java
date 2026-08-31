package nusiss.ddas.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer documentId;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private String uploader;

    @Column(nullable = false)
    private String status;

    @Column(name = "createdOn", nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private String documentName;

    public Document() {
    }

    public Document(String documentType, String uploader, String status, LocalDateTime createdOn, String documentName) {
        this.documentType = documentType;
        this.uploader = uploader;
        this.status = status;
        this.createdOn = createdOn;
        this.documentName = documentName;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}