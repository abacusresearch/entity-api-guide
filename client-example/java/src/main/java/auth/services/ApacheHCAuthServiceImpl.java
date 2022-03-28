package auth.services;

import auth.EmptySystemPropertyException;
import auth.IAuthService;
import auth.OAuthToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.StringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.olingo.client.api.http.HttpClientException;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ApacheHCAuthServiceImpl implements IAuthService {
    private static final String CLIENT_ID = System.getProperty("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getProperty("CLIENT_SECRET");
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String baseUri;

    public ApacheHCAuthServiceImpl(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public OAuthToken getToken() {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            checkSystemPropertiesSet();
            return getAuthToken(httpclient);
        } catch (IOException | EmptySystemPropertyException e) {
            throw new HttpClientException("");
        }
    }


    private OAuthToken getAuthToken(CloseableHttpClient httpclient) throws IOException {
        final var unencodedString = CLIENT_ID + ":" + CLIENT_SECRET;
        final var encodedString = Base64.getEncoder().encodeToString(unencodedString.getBytes());
        final var authEndpointUri = getAuthEndpointURI(httpclient);
        final var authReq = new HttpPost(URI.create(authEndpointUri));
        final List<BasicNameValuePair> body = new ArrayList<>();
        body.add(new BasicNameValuePair("grant_type", "client_credentials"));

        authReq.addHeader("Authorization", "Basic " + encodedString);
        authReq.setEntity(new UrlEncodedFormEntity(body));

        try (CloseableHttpResponse response = httpclient.execute(authReq)) {
            HttpEntity entity = response.getEntity();
            final var responseJson = mapper.readValue(entity.getContent(), Map.class);
            final var token = (String) responseJson.get("access_token");
            final var expiresIn = calcExpiresAt((Integer) responseJson.get("expires_in"));
            EntityUtils.consume(entity);
            return new OAuthToken(token, expiresIn, true);
        }
    }

    private long calcExpiresAt(int expiresIn) {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + expiresIn;
    }

    private String getAuthEndpointURI(CloseableHttpClient httpClient) {
            HttpGet httpGet = new HttpGet(URI.create(baseUri + "/.well-known/openid-configuration"));
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                final var responseJson = mapper.readValue(response.getEntity().getContent(), Map.class);
                return (String) responseJson.get("token_endpoint");
            } catch (IOException e) {
                throw new HttpClientException("");
            }
    }

    private void checkSystemPropertiesSet() throws EmptySystemPropertyException {
        if (StringUtil.isNullOrEmpty(CLIENT_ID)) {
            throw new EmptySystemPropertyException("CLIENT_ID");
        }
        if(StringUtil.isNullOrEmpty(CLIENT_SECRET)) {
            throw new EmptySystemPropertyException("CLIENT_SECRET");
        }
    }
}
