package nusiss.ddas.app.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "jwt_tokens")
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Integer tokenId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "token", nullable = false, length = 2000)
    private String token;

    @Column(name = "token_type", nullable = false)
    private String tokenType;

    @Column(name = "is_valid", nullable = false)
    private boolean isValid = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public JwtToken() {
    }

    public JwtToken(Integer userId, String token, String tokenType) {
        this.userId = userId;
        this.token = token;
        this.tokenType = tokenType;
    }

    public Integer getTokenId() {
        return tokenId;
    }

    public void setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}