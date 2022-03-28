package auth;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


/**
 * A cache is not strictly necessary it is just more efficient to cache the auth token and use it until it expires.
 * Otherwise, the Request Interceptor needs to request a token everytime before sending the actual Rest Query.
 * This is especially important, because according to the docs the RequestInterceptor implementation needs to be
 * threadsafe and is thus synchronized in this sample implementation.
 **/

public class AuthTokenCache {

    private final OAuthToken token;

    public AuthTokenCache() {
        this.token = new OAuthToken("", 0, true);
    }

    public String getToken() {
        return token.getToken();
    }

    public void setToken(OAuthToken token) {
        this.token.setToken(token);
    }

    public boolean checkTokenExpired() {
        if(!token.isExp()) {
            if (token.getExpiresAt() < LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {
                token.setExp(true);
                return true;
            }
            return false;
        }
        return true;
    }
}
