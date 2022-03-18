package auth;

import com.auth0.jwt.JWT;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class AuthServiceTokenCache {
    private String token;
    private boolean isExp;

    public AuthServiceTokenCache(String token) {
        this.token = token;
        this.isExp = false;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isExp() {
        var exp = LocalDateTime.ofInstant(
                JWT.decode(getToken()).getExpiresAt().toInstant(), ZoneId.systemDefault()
        );
        if (exp.compareTo(LocalDateTime.now()) <= 0) {
            setExp(true);
        }
        return isExp;
    }

    public void setExp(boolean exp) {
        isExp = exp;
    }

}
