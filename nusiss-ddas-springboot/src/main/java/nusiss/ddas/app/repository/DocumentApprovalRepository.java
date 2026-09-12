package nusiss.ddas.app.repository;

import nusiss.ddas.app.model.Document;
import nusiss.ddas.app.model.DocumentApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentApprovalRepository extends JpaRepository<DocumentApproval, Integer> {
    List<DocumentApproval> findByDocumentOrderByCreatedAtDesc(Document document);
}
