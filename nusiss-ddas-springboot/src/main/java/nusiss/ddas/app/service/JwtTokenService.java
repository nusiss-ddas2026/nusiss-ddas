package nusiss.ddas.app.service;

import nusiss.ddas.app.model.JwtToken;
import nusiss.ddas.app.repository.JwtTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtTokenService {

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    public JwtToken saveToken(Integer userId, String token, String tokenType) {
        JwtToken jwtToken = new JwtToken(userId, token, tokenType);
        return jwtTokenRepository.save(jwtToken);
    }

    public JwtToken findTokenByToken(String token) {
        List<JwtToken> tokens = jwtTokenRepository.findByTokenAndIsValidTrue(token);
        return tokens.isEmpty() ? null : tokens.get(0);
    }

    public void invalidateUserTokens(Integer userId) {
        List<JwtToken> tokens = jwtTokenRepository.findByUserId(userId);
        for (JwtToken token : tokens) {
            token.setValid(false);
        }
        jwtTokenRepository.saveAll(tokens);
    }

    public List<JwtToken> getUserTokens(Integer userId) {
        return jwtTokenRepository.findByUserIdAndIsValidTrue(userId);
    }
}