package auth;

import com.github.scribejava.core.model.OAuth2AccessToken;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class AuthServiceTokenCache {
    private String token;
    private boolean isExp;
    private long expiresAt;
    private static final ZoneOffset zoneOffset = ZoneOffset.UTC;

    public AuthServiceTokenCache(OAuth2AccessToken token) {
        this.token = token.getAccessToken();
        this.isExp = false;
        this.expiresAt = LocalDateTime.now().toEpochSecond(zoneOffset) + token.getExpiresIn();
    }

    public String getToken() {
        return token;
    }

    public void setToken(OAuth2AccessToken token) {
        this.token = token.getAccessToken();
        setExp(false);
        setExpiresAt(LocalDateTime.now().toEpochSecond(zoneOffset) + token.getExpiresIn());
    }

    public boolean isExp() {
        if (getExpiresAt() < LocalDateTime.now().toEpochSecond(zoneOffset)) {
            setExp(true);
        }
        return isExp;
    }

    private void setExp(boolean isExp) {
        this.isExp = isExp;
    }

    private long getExpiresAt() {
        return expiresAt;
    }

    private void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
