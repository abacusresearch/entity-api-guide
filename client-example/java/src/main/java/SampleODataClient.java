import auth.EmptySystemPropertyException;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.batch.BatchManager;
import org.apache.olingo.client.api.communication.request.batch.ODataBatchRequest;
import org.apache.olingo.client.api.communication.request.batch.ODataBatchResponseItem;
import org.apache.olingo.client.api.communication.request.retrieve.*;
import org.apache.olingo.client.api.communication.response.*;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.edm.xml.Reference;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.apache.olingo.client.core.ODataClientFactory.getClient;


/**
 * Following client is an example implementations of a Wrapper around the Apache Olingo client. It is configured
 * to query an Abacus TestVM with example data.
 */

public class SampleODataClient {

  private final ODataClient client;

  /**
   * Configuration of Apache Olingo OData Client with self implemented Olingo HttpClientFactory Interface
   * using request interceptors for OAuth 2.0 Application Credential Flow.
   */
  public SampleODataClient() throws EmptySystemPropertyException, IOException, ExecutionException, InterruptedException {
    client = getClient();
    client.getConfiguration().setHttpClientFactory(new SampleHttpClientFactory());
    client.getConfiguration().setGzipCompression(true);
    client.getConfiguration().setDefaultBatchAcceptFormat(ContentType.APPLICATION_OCTET_STREAM);
  }

  /**
   * Reads the Entity Data Model auf the given URL if controlled by a OData Service
   * @param serviceUrl URI of the Service
   * @return The Entity Data Model definition as XML with available entities and their properties
   */
  public Edm readEdm(String serviceUrl) {
    final var requestFactory = client.getRetrieveRequestFactory();
    final var xmlMetadataRequest = requestFactory.getXMLMetadataRequest(serviceUrl);
    final var body = xmlMetadataRequest.execute().getBody();
    final var referenceStreams = body.getReferences().stream()
            .map(Reference::getUri)
            .map(requestFactory::getRawRequest)
            .map(ODataRawRequest::execute)
            .map(ODataResponse::getRawResponse)
            .collect(Collectors.toUnmodifiableList());

    return client.getReader().readMetadata(body, referenceStreams);
  }

  /**
   * Following methods show the capabilities of an Apache Olingo Client. The used naming schema reflects the capabilities
   * of the method and the required parameters. There are implemented test under folder /test showing the usage of these
   * request helper methods.
   *
   * The requests could also be implemented by providing:
   * - Service URL + EntitySetName + Queryparams
   * - Auth
   * - Request Headers
   */

  public ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntities(Edm edm, String serviceUri,
                                                                             String entitySetName) {
    URI absoluteUri = client.newURIBuilder(serviceUri).appendEntitySetSegment(entitySetName).build();
    return readEntities(edm, absoluteUri);
  }

  public ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntitiesWithFilter(Edm edm, String serviceUri,
                                                                                       String entitySetName, String filterName) {
    URI absoluteUri = client.newURIBuilder(serviceUri).appendEntitySetSegment(entitySetName).filter(filterName).build();
    return readEntities(edm, absoluteUri);
  }

  public ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntitiesWithSelect(Edm edm, String serviceUri,
                                                                                       String entitySetName, String... selectionNames) {
    URI absoluteUri = client.newURIBuilder(serviceUri).appendEntitySetSegment(entitySetName).select(selectionNames).build();
    return readEntities(edm, absoluteUri);
  }

  public ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntitiesWithOrderBy(Edm edm, String serviceUri,
                                                                                       String entitySetName, String orderByName) {
    URI absoluteUri = client.newURIBuilder(serviceUri).appendEntitySetSegment(entitySetName).orderBy(orderByName).build();
    return readEntities(edm, absoluteUri);
  }

  public ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntitiesWithFormat(Edm edm, String serviceUri,
                                                                                        String entitySetName, String formatName) {
    URI absoluteUri = client.newURIBuilder(serviceUri).appendEntitySetSegment(entitySetName).format(formatName).build();
    return readEntities(edm, absoluteUri);
  }

  private ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntities(Edm edm, URI absoluteUri) {
    System.out.println("URI = " + absoluteUri);
    ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request =
            client.getRetrieveRequestFactory().getEntitySetIteratorRequest(absoluteUri);
    request.setAccept("application/json");
    ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();

    return response.getBody();
  }

  public ClientEntity readEntityWithKey(Edm edm, String serviceUri, String entitySetName, Object keyValue) {
    URI absoluteUri = client.newURIBuilder(serviceUri).appendEntitySetSegment(entitySetName)
            .appendKeySegment(keyValue).build();
    return readEntity(edm, absoluteUri);
  }

  public ClientEntity readEntityWithKeyExpand(Edm edm, String serviceUri, String entitySetName, Object keyValue,
                                              String expandRelationName) {
    URI absoluteUri = client.newURIBuilder(serviceUri).appendEntitySetSegment(entitySetName).appendKeySegment(keyValue)
            .expand(expandRelationName).build();
    return readEntity(edm, absoluteUri);
  }

  private ClientEntity readEntity(Edm edm, URI absoluteUri) {
    ODataEntityRequest<ClientEntity> request = client.getRetrieveRequestFactory().getEntityRequest(absoluteUri);
    request.setAccept("application/json;odata.metadata=full");
    ODataRetrieveResponse<ClientEntity> response = request.execute();

    return response.getBody();
  }

  public Iterator<ODataBatchResponseItem> readBatch(String serviceUri, String entitySetName, boolean isRelative) {
    ODataBatchRequest request = client.getBatchRequestFactory().getBatchRequest(serviceUri);
    request.addCustomHeader(HttpHeader.ACCEPT_CHARSET, "UTF-8");

    final BatchManager payloadManager = request.payloadManager();

    final URI targetURI1 = client.newURIBuilder(serviceUri).appendEntitySetSegment(entitySetName).appendKeySegment(1).build();
    final URI uri1 = isRelative ? URI.create(serviceUri).relativize(targetURI1) : targetURI1;
    ODataEntityRequest<ClientEntity> queryReq1 = client.getRetrieveRequestFactory().getEntityRequest(uri1);
    queryReq1.setAccept(String.valueOf(ContentType.APPLICATION_JSON));
    payloadManager.addRequest(queryReq1);

    final URI targetURI2 = client.newURIBuilder(serviceUri).appendEntitySetSegment(entitySetName).appendKeySegment(1).build();
    final URI uri2 = isRelative ? URI.create(serviceUri).relativize(targetURI2) : targetURI1;
    ODataEntityRequest<ClientEntity> queryReq2 = client.getRetrieveRequestFactory().getEntityRequest(uri2);
    queryReq2.setAccept(String.valueOf(ContentType.APPLICATION_JSON));
    payloadManager.addRequest(queryReq2);

    final ODataBatchResponse response = payloadManager.getResponse();

    return response.getBody();
  }
}