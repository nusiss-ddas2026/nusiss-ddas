package nusiss.ddas.app.repository;

import nusiss.ddas.app.model.DocumentApproval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentApprovalRepository extends JpaRepository<DocumentApproval, Integer> {
}
