package auth.services;

import auth.EmptySystemPropertyException;
import auth.IAuthService;
import auth.OAuthToken;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.oauth.OAuth20Service;
import io.netty.util.internal.StringUtil;
import org.apache.olingo.client.api.http.HttpClientException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Requires setting CLIENT_ID and CLIENT_SECRET as Environment Variables to access the VM
 * For valid Client Credentials please contact Abacus Research AG
 *
 * This implementation uses the Scribe OAuth Library
 */

public class ScribeAuthServiceImpl implements IAuthService {
    private static final String AUTH_ENDPOINT = "/oauth/oauth2/v1/token";

    private static final String CLIENT_ID = System.getProperty("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getProperty("CLIENT_SECRET");

    private final OAuth20Service service;


    public ScribeAuthServiceImpl(String baseUrl) throws EmptySystemPropertyException {
        checkSystemPropertiesSet();
        this.service = new ServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .build(AbacusApi.instance(baseUrl));
    }

    private void checkSystemPropertiesSet() throws EmptySystemPropertyException {
        if (StringUtil.isNullOrEmpty(CLIENT_ID)) {
            throw new EmptySystemPropertyException("CLIENT_ID");
        }
        if(StringUtil.isNullOrEmpty(CLIENT_SECRET)) {
            throw new EmptySystemPropertyException("CLIENT_SECRET");
        }
    }

    @Override
    public OAuthToken getToken() {
        try {
            final var token = service.getAccessTokenClientCredentialsGrant();
            return new OAuthToken(token.getAccessToken(), token.getExpiresIn(), false);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new HttpClientException("Failed to fetch Token with scribejava");
        }
    }

    private static class AbacusApi extends DefaultApi20 {

        private final String baseURL;

        private AbacusApi(final String baseURL) {
            this.baseURL = baseURL;
        }

        @Override
        public String getAccessTokenEndpoint() {
            return baseURL + AUTH_ENDPOINT;
        }

        @Override
        protected String getAuthorizationBaseUrl() {
            throw new UnsupportedOperationException();
        }

        public static AbacusApi instance(final String baseUrl) {
            return new AbacusApi(baseUrl);
        }
    }

}
