package nusiss.ddas.app.service;

import nusiss.ddas.app.model.User;
import nusiss.ddas.app.util.JwtUtil;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Single chokepoint for minting tokens after any authentication path succeeds
 * (password login, password+MFA, or OIDC) - invalidates previous tokens, issues
 * a fresh access/refresh pair, and sets the jwtToken cookie identically every time.
 */
@Service
public class TokenIssuanceService {

    private final JwtUtil jwtUtil;
    private final JwtTokenService jwtTokenService;

    public TokenIssuanceService(JwtUtil jwtUtil, JwtTokenService jwtTokenService) {
        this.jwtUtil = jwtUtil;
        this.jwtTokenService = jwtTokenService;
    }

    public Map<String, String> issueTokensAndSetCookie(User user, UserDetails userDetails, HttpServletResponse response) {
        jwtTokenService.invalidateUserTokens(user.getUserId());

        String accessToken = jwtUtil.generateToken(userDetails, new HashMap<String, Object>() {{
            put("role", user.getRole().name());
        }});
        String refreshToken = jwtUtil.generateRefreshToken(userDetails, new HashMap<>());

        jwtTokenService.saveToken(user.getUserId(), accessToken, "ACCESS");
        jwtTokenService.saveToken(user.getUserId(), refreshToken, "REFRESH");

        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", accessToken)
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .build();

        response.setHeader("Set-Cookie", jwtCookie.toString());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("role", user.getRole().name());
        tokens.put("username", user.getUsername());

        return tokens;
    }
}
