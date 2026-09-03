package nusiss.ddas.app.repository;

import nusiss.ddas.app.model.Document;
import nusiss.ddas.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByUploader(User uploader);
    List<Document> findByUploaderIn(List<User> uploaders);
}
