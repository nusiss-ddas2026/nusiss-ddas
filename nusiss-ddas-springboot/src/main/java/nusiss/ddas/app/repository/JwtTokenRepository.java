package nusiss.ddas.app.repository;

import nusiss.ddas.app.model.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Integer> {
    List<JwtToken> findByUserIdAndIsValidTrue(Integer userId);
    List<JwtToken> findByTokenAndIsValidTrue(String token);
    List<JwtToken> findByUserId(Integer userId);
}