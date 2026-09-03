package nusiss.ddas.app.repository;

import nusiss.ddas.app.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findByDeptName(String deptName);
}
