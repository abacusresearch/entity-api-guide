package auth;

public class OAuthToken {
    private String token;
    private long expiresAt;
    private boolean isExp;

    public OAuthToken(String token, long expiresAt, boolean isExp) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.isExp = isExp;
    }

    public String getToken() {
        return token;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExp() {
        return isExp;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setExp(boolean exp) {
        isExp = exp;
    }

    public void setToken(OAuthToken token) {
        setToken(token.getToken());
        setExp(token.isExp());
        setExpiresAt(token.getExpiresAt());
    }
}
