import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import auth.IAuthService;
import auth.services.ApacheHCAuthServiceImpl;
import auth.AuthTokenCache;
import auth.services.ScribeAuthServiceImpl;
import auth.EmptySystemPropertyException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.olingo.client.api.http.HttpClientException;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.commons.api.http.HttpMethod;

/**
 * Specific implementation of Apache Olingo HttpClientFactory interface to use Scribejava Authentication Library
 * to provide Bearer Token in Authorization Header of requests going to AbaTest VM
 *
 */

public class SampleHttpClientFactory implements HttpClientFactory {

    private final AuthTokenCache cache;
    private final AuthClientType type;

    public SampleHttpClientFactory(AuthClientType type) {
        this.type = type;
        this.cache = new AuthTokenCache();
    }

    @Override
    public HttpClient create(final HttpMethod method, final URI uri) {
            return HttpClients.custom()
                    .addInterceptorLast((HttpRequestInterceptor) (httpRequest, httpContext) -> {
                        if(uri.getHost().contains("abacus")){
                            signRequest(uri, httpRequest);
                        }
                    })
                    .build();
    }

    private synchronized void signRequest(URI uri, HttpRequest httpRequest) {
        final IAuthService service;
        try {
            service = createService(uri);
            checkTokenExpired(service);
            httpRequest.addHeader(
                    "Authorization",
                    "Bearer " + cache.getToken()
            );
        } catch (EmptySystemPropertyException | ExecutionException | InterruptedException | IOException e) {
            throw new HttpClientException("Could not sign request to abacus", e);
        }
    }

    @Override
    public void close(HttpClient httpClient) {
        try {
            if(httpClient instanceof CloseableHttpClient)
                ((CloseableHttpClient)httpClient).close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private IAuthService createService(URI uri) throws EmptySystemPropertyException, IOException, ExecutionException, InterruptedException {
        final var url = uri.toURL();
        var base = new URL(url.getProtocol(), url.getHost(), url.getPort(), "").toString();
        switch (type) {
            case APACHE: return new ApacheHCAuthServiceImpl(base);
            case SCRIBE: return new ScribeAuthServiceImpl(base);
            default: throw new RuntimeException("Must supply AuthClientType");
        }
    }

    private void checkTokenExpired(IAuthService service) throws ExecutionException, InterruptedException {
        if (cache.checkTokenExpired()) {
            cache.setToken(service.getToken());
        }
    }
}
