# entity-api-guide

This guide shall be used to help provide a client side implementation for usage of the new Abacus Entity API.

Entity API is a new Interface for processing and returning REST Request/Response Pairs as per OData 4.0 Specification.

It is intended to be the new way to communicate with the Abacus Buisness Software.

Essentially this quickstart provides the reader with information about how to wrap his webrequests to communicate with the API as intended.

## OData Specification

The [OData 4.0 Specification](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html) provides its users with a multitude of possible queryparameters and resulting manipulations of response datasets.

### Supported Access Schemas

Mainly data querying is done through the Entity API at the moment. Usually this is done through a GET request to the server with a URL corresponding to the following Schema:

- Access of Metainformation about queryable entity endpoints e.g. /
- Access of Entity e.g. /Entity
- Access of specific Entity via primary key e.g. /Entity(2)
- Access of specific Entity property via key & property e.g. /Entity(2)/LastName

### Supported Queryparameters 

Further there are system operations defined for certain queryparameters if supplied in the request:

| Queryparameter | Usage Explaination | Example |
|----------------|--------------------|---------|
| filter | Used to filter response set by a property. Implements logical operators: eq, lt, gt, le, ge | $filter=LastName eq 'Strasser' |
| select | Used to query only certain properties of entity. If the primary key is not selected an odata.id is inserted to uniquely identify the items of the returned set. | $select=LastName,AddressNumber |
| top | Used to select only the top number of elements returned by query. | $top=10 |
| skip | Used to skip a certain number of elements in the returned query | $skip=50 |
| orderby | Used to order the returned query by a certain attribute in asc or desc order | $orderby=LastName desc |
| search | Used to fulltext search for a property or nested property with the given string as value. | $search=Strasser |
| expand | Used to expand a navigation property into the returning query | $expand=Friends |

## Authentication

Obviously to access the entity API and thus the underlying Abacus installation an authentication method is required. At the moment the authentication is done via the /oauth/oauth2/v1/token endpoint of the Abacus installation.

Authentication is done via OAuth 2.0 Flow of supplying clientID and clientSecret to the authentication endpoint and receiveing a bearer token.

This bearer token is then set in the authorization header of the client requests

## Implemented Entities

At the moment we are in the process of migrating the various subsystems to the new Entity API Model. Subsequently only a portion of possible Entities is available at this moment.

Access is achieved via the following basic Url: **https://<your_domain>/api/entity/<used_version>/mandants/<mandant_number>/**.

<small>*NOTE: <your_domain> is most likely localhost:40001, <used_version> most likey v1, <mandant_number> most likely 7777 or 300*</small>

Implemented Entities at this time:

| Entity | Explaination | Route |
|--------|--------------|-------|
| Auth   | Used to acquire the bearer token necessary to access other endpoints | /oauth/oauth2/v1/token <br>*Note: inserted directly after domain, does not require base url* |
| Addresses | Example Entity developed as showcase of odata specification | /Addresses |
| Subjects | Entity | /Subjects

### Work in Progress

Most of the work for the read queries is done and mostly client side implementations are now necessary. But the Odata specifications defines several possible ways of doing various actions through PUT, DELETE & POST Request. Theses are work in progress at the moment.

### Limitations

There are some operations which work very well with our underlying database structure and query very efficiently using the existing indexes consequently there are some operations defined in the specification which do not work efficiently with our implementation and are thus unsupported.

These are:
- count

