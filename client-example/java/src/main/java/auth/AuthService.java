package auth;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.oauth.OAuth20Service;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Requires setting CLIENT_ID and CLIENT_SECRET as Environment Variables to access the VM
 * For valid Client Credentials please contact Abacus Research AG
 */

public class AuthService {
    private static final String AUTH_ENDPOINT = "https://entity-api1-1.demo.abacus.ch/oauth/oauth2/v1/token";

    private static final String CLIENT_ID = System.getProperty("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getProperty("CLIENT_SECRET");

    private final OAuth20Service service;
    private final AuthServiceTokenCache cache;


    public AuthService() throws EmptySystemPropertyException, IOException, ExecutionException, InterruptedException {
        checkSystemPropertiesSet();
        this.service = new ServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .build(AbacusApi.instance());
        this.cache = new AuthServiceTokenCache(
                service.getAccessTokenClientCredentialsGrant()
        );
    }

    private void checkSystemPropertiesSet() throws EmptySystemPropertyException {
        if (StringUtil.isNullOrEmpty(CLIENT_ID)) {
            throw new EmptySystemPropertyException("CLIENT_ID");
        }
        if(StringUtil.isNullOrEmpty(CLIENT_SECRET)) {
            throw new EmptySystemPropertyException("CLIENT_SECRET");
        }
    }

    private void checkTokenExpired() throws IOException, ExecutionException, InterruptedException {
        if (cache.isExp()) {
            cache.setToken(service.getAccessTokenClientCredentialsGrant());
        }
    }

    public String getClientCredentialsToken() throws IOException, ExecutionException, InterruptedException {
        checkTokenExpired();
        return cache.getToken();
    }

    static class AbacusApi extends DefaultApi20 {
        private AbacusApi() {}

        @Override
        public String getAccessTokenEndpoint() {
            return AUTH_ENDPOINT;
        }

        @Override
        protected String getAuthorizationBaseUrl() {
            throw new UnsupportedOperationException();
        }

        public static AbacusApi instance() {
            return new AbacusApi();
        }
    }

}
