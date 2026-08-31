package nusiss.ddas.app.repository;

import nusiss.ddas.app.model.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Integer> {
}