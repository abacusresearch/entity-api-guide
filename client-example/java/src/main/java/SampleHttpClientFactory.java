import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import auth.AuthService;
import auth.EmptySystemPropertyException;
import com.github.scribejava.core.model.OAuthConstants;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.commons.api.http.HttpMethod;

/**
 * Specific implementation of Apache Olingo HttpClientFactory interface to use Scribejava Authentication Library
 * to provide Bearer Token in Authorization Header of requests going to AbaTest VM
 *
 */

public class SampleHttpClientFactory implements HttpClientFactory {

    private final AuthService service;

    public SampleHttpClientFactory() throws EmptySystemPropertyException, IOException, ExecutionException, InterruptedException {
        this.service = new AuthService();
    }

    @Override
    public HttpClient create(final HttpMethod method, final URI uri) {
        return HttpClients.custom()
                .addInterceptorLast((HttpRequestInterceptor) (httpRequest, httpContext) -> {
                    synchronized (service) {
                        try {
                            httpRequest.addHeader(
                                    OAuthConstants.HEADER,
                                    "Bearer " + service.getClientCredentialsToken()
                            );
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .build();
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
}
