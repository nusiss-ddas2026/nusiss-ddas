package nusiss.ddas.app.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds short-lived "password verified, MFA code still required" login challenges.
 *
 * Deliberately NOT a JWT and NOT persisted anywhere, in particular NOT in the
 * jwt_tokens table: JwtAuthenticationFilter authenticates a request as soon as it
 * finds ANY matching, valid row in that table, regardless of token_type. A pending
 * login must not be able to grant access the way a real access/refresh token does,
 * so it is kept entirely out of that path, in a plain in-memory map instead.
 *
 * Time-limited and only consumed (removed) once a correct TOTP code is presented
 * for it - a wrong code just fails that attempt without burning the challenge, so
 * a mistyped code doesn't force the user back to square one (re-entering their
 * password) the way single-use-per-lookup semantics would. Expired entries are
 * only reaped lazily, on lookup - acceptable at demo scale, no background sweep
 * thread. This is also single-instance/non-clustered: a challenge created on one
 * JVM isn't visible to another, which is fine for this project's single-instance
 * deployment but would need a shared store (e.g. the database or Redis) to scale
 * out. No rate-limiting on verification attempts against a live challenge - an
 * accepted limitation for this demo.
 */
@Service
public class MfaChallengeService {

    private static final long TTL_MILLIS = 5 * 60 * 1000;

    private record Challenge(Integer userId, long expiresAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();

    public String createChallenge(Integer userId) {
        String challengeId = UUID.randomUUID().toString();
        challenges.put(challengeId, new Challenge(userId, System.currentTimeMillis() + TTL_MILLIS));
        return challengeId;
    }

    /**
     * Looks up the userId a (still-live) challenge was issued for, without
     * consuming it - lets a wrong code be retried against the same challenge.
     * Returns null if the challenge id is unknown or expired (expired entries
     * are removed here as a lazy cleanup).
     */
    public Integer peekChallenge(String challengeId) {
        if (challengeId == null) {
            return null;
        }
        Challenge challenge = challenges.get(challengeId);
        if (challenge == null) {
            return null;
        }
        if (challenge.isExpired()) {
            challenges.remove(challengeId);
            return null;
        }
        return challenge.userId();
    }

    /** Invalidates a challenge once it has been successfully verified, so it can't be replayed. */
    public void invalidateChallenge(String challengeId) {
        challenges.remove(challengeId);
    }
}
