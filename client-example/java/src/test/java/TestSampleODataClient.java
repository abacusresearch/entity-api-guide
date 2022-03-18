import auth.EmptySystemPropertyException;
import org.apache.olingo.client.api.communication.request.batch.ODataBatchResponseItem;
import org.apache.olingo.client.api.communication.response.ODataResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.*;
import org.apache.olingo.commons.api.edm.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class TestSampleODataClient {

    private static final String SERVICE_URL = "https://entity-api1-1.demo.abacus.ch/api/entity/v1/mandants/7777";
    private static SampleODataClient client;

    @BeforeAll
    static void init() throws IOException, ExecutionException, InterruptedException, EmptySystemPropertyException {
        client =  new SampleODataClient();
    }

    @Test
    void testMetadata() {
       print("\n----- Read Edm ------------------------------");
        Edm edm = client.readEdm(SERVICE_URL);

        List<FullQualifiedName> ctFqns = new ArrayList<>();
        List<FullQualifiedName> etFqns = new ArrayList<>();
        for (EdmSchema schema : edm.getSchemas()) {
            for (EdmComplexType complexType : schema.getComplexTypes()) {
                ctFqns.add(complexType.getFullQualifiedName());
            }
            for (EdmEntityType entityType : schema.getEntityTypes()) {
                etFqns.add(entityType.getFullQualifiedName());
                System.out.println(entityType + ": " + getDescription(edm, entityType));
            }
        }
       print("Found ComplexTypes", ctFqns);
       print("Found EntityTypes", etFqns);

        for (FullQualifiedName etFqn : etFqns) {
           print("\n----- Inspect each property and its type of the first entity: " + etFqn + "----");
            EdmEntityType etype = edm.getEntityType(etFqn);
            for (String propertyName : etype.getPropertyNames()) {
                EdmProperty property = etype.getStructuralProperty(propertyName);
                FullQualifiedName typeName = property.getType().getFullQualifiedName();
                String description = getDescription(edm, property);
               print("property '" + propertyName + "' " + typeName + ": " + description);
            }
        }
    }

    @Test
    void testReadSubjects() {
        Edm edm = client.readEdm(SERVICE_URL);

       print("\n----- Read Entities ------------------------------");
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator =
                client.readEntities(edm, SERVICE_URL, "Subjects");

        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();
           print("Entry:\n" + prettyPrint(ce.getProperties(), 0));
        }
    }

    @Test
    void testReadSubjectsWithSelect() {
        Edm edm = client.readEdm(SERVICE_URL);

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator;
        print("\n----- Read Entry with $select ------------------------------");
        iterator = client.readEntitiesWithSelect(
                edm,
                SERVICE_URL,
                "Subjects",
                "FirstName",
                "LastName"
        );
        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();
            print("Entry:\n" + prettyPrint(ce.getProperties(), 0));
        }
    }

    @Test
    void testReadSubjectsWithOrderBy() {
        Edm edm = client.readEdm(SERVICE_URL);

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator;
        print("\n----- Read Entry with $orderby ------------------------------");
        iterator = client.readEntitiesWithOrderBy(
                edm,
                SERVICE_URL,
                "Subjects",
                "Id desc"
        );
        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();
            print("Entry:\n" + prettyPrint(ce.getProperties(), 0));
        }
    }

    @Test
    void testReadSubjectsWithFormat() {
        Edm edm = client.readEdm(SERVICE_URL);

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator;
        print("\n----- Read Entry with $format ------------------------------");
        iterator = client.readEntitiesWithFormat(
                edm,
                SERVICE_URL,
                "Subjects",
                "xml"
        );
        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();
            print("Entry:\n" + prettyPrint(ce.getProperties(), 0));
        }
    }

    @Test
    void testReadSubjectsWithFilter() {
        Edm edm = client.readEdm(SERVICE_URL);

       print("\n----- Read Entities with $filter  ------------------------------");
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = client.readEntitiesWithFilter(
                edm,
                SERVICE_URL,
                "Subjects",
                "LastName eq 'Schwarz'"
        );
        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();
           print("Entry:\n" + prettyPrint(ce.getProperties(), 0));
        }
    }

    @Test
    void testReadSubjectsWithKey() {
        Edm edm = client.readEdm(SERVICE_URL);
       print("\n----- Read Entry with key segment -----------------------");
        ClientEntity entry = client.readEntityWithKey(edm, SERVICE_URL, "Subjects", 1);
       print("Single Entry:\n" + prettyPrint(entry.getProperties(), 0));
    }

    @Test
    void testReadSubjectsWithKeysBatch() {
        print("\n----- Read batch with keys ------------------------------");
        Iterator<ODataBatchResponseItem> iterator = client.readBatch(SERVICE_URL, "Subjects", true);
        while (iterator.hasNext()) {
            ODataBatchResponseItem item = iterator.next();
            ODataResponse oDataResponse = item.next();
            if(oDataResponse instanceof ODataRetrieveResponse) {
                ODataRetrieveResponse<ClientEntity> res = (ODataRetrieveResponse<ClientEntity>) oDataResponse;
                print("Single Entry:\n" + prettyPrint(res.getBody().getProperties(), 0));
            }
        }
    }

    @Test
    void testGzipCompression() {
        print("\n----- Read batch with keys ------------------------------");
        var res = client.getMetadataResponse(SERVICE_URL);
        print(String.valueOf(res.getHeader("Accept-Encoding")));
    }

    /*
    @Test
    void testReadSubjectsWithKeyAndExpand() {
        Edm edm = client.readEdm(SERVICE_URL);
        print("\n----- Read Entity with $expand  ------------------------------");
        entry = readEntityWithKeyExpand(edm, SERVICE_URL, "Addresses", 4, "Activities");
        print("Single Entry with expanded Activities relation:\n" + prettyPrint(entry.getProperties(), 0));
    }


        @Test
       void testCUDSubjects() {
        // skip everything as odata4 sample/server only supporting retrieval
       print("\n----- Create Entry ------------------------------");
        ClientEntity ce = client.loadEntity("myaddress.json");
        entry = client.createEntity(edm, SERVICE_URL, "Addresses", ce);
       print("Created Entry successfully: " + prettyPrint(entry.getProperties(), 0));
        long insertedKey = entry.getProperty("AddressNumber").getPrimitiveValue().toCastValue(Long.class);

       print("\n----- Update Entry ------------------------------");
        ce = client.loadEntity("myaddress2.json");
        int sc = client.updateEntity(edm, SERVICE_URL, "Addresses", insertedKey, ce);
       print("Updated successfully: " + sc);
        entry = client.readEntityWithKey(edm, SERVICE_URL, "Addresses", insertedKey);
       print("Updated Entry successfully: " + prettyPrint(entry.getProperties(), 0));

       print("\n----- Delete Entry ------------------------------");
        sc = client.deleteEntity(SERVICE_URL, "Addresses", insertedKey);
       print("Deletion of Entry was successfully: " + sc);

        try {
           print("\n----- Verify Delete Entry ------------------------------");
            client.readEntityWithKey(edm, SERVICE_URL, "Addresses", 123);
        } catch (Exception e) {
           print(e.getMessage());
        }
    }

        @Test
        void testUpdateBinary() throws Exception {
            Future<ODataMediaEntityUpdateResponse<ClientEntity>> future = client.updateEntityMedia(SERVICE_URL, "Addresses", 105, getClass().getResourceAsStream("large.mp4"));
            while(!future.isDone()) {
                System.out.println("Calculating...");
                Thread.sleep(1000);
            }

            System.out.println(future.get().getStatusCode());
        }
    */

    //region Print Helpers
    private static String getDescription(Edm edm, EdmAnnotatable entityType) {
        final var descriptionFQN = new FullQualifiedName("Org.OData.Core.V1.Description");
        final var annotation = entityType.getAnnotation(edm.getTerm(descriptionFQN), null);
        if (annotation == null) {
            return "";
        }
        final var constantExpression = annotation.getExpression().asConstant();
        if (constantExpression == null) {
            return "";
        }
        return constantExpression.getValueAsString();
    }

    private static void print(String content) {
        System.out.println(content);
    }

    private static void print(String content, List<?> list) {
        System.out.println(content);
        for (Object o : list) {
            System.out.println("    " + o);
        }
        System.out.println();
    }

    private static String prettyPrint(Map<String, Object> properties, int level) {
        StringBuilder b = new StringBuilder();
        Set<Map.Entry<String, Object>> entries = properties.entrySet();

        for (Map.Entry<String, Object> entry : entries) {
            intend(b, level);
            b.append(entry.getKey()).append(": ");
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = prettyPrint((Map<String, Object>) value, level + 1);
            } else if (value instanceof Calendar) {
                Calendar cal = (Calendar) value;
                value = SimpleDateFormat.getInstance().format(cal.getTime());
            }
            b.append(value).append("\n");
        }
        // remove last line break
        b.deleteCharAt(b.length() - 1);
        return b.toString();
    }

    private static String prettyPrint(Collection<ClientProperty> properties, int level) {
        StringBuilder b = new StringBuilder();

        for (ClientProperty entry : properties) {
            intend(b, level);
            ClientValue value = entry.getValue();
            if (value.isCollection()) {
                ClientCollectionValue cclvalue = value.asCollection();
                b.append(prettyPrint(cclvalue.asJavaCollection(), level + 1));
            } else if (value.isComplex()) {
                ClientComplexValue cpxvalue = value.asComplex();
                b.append(prettyPrint(cpxvalue.asJavaMap(), level + 1));
            } else if (value.isEnum()) {
                ClientEnumValue cnmvalue = value.asEnum();
                b.append(entry.getName()).append(": ");
                b.append(cnmvalue.getValue()).append("\n");
            } else if (value.isPrimitive()) {
                b.append(entry.getName()).append(": ");
                b.append(entry.getValue()).append("\n");
            }
        }
        return b.toString();
    }

    private static void intend(StringBuilder builder, int intendLevel) {
        for (int i = 0; i < intendLevel; i++) {
            builder.append("  ");
        }
    }
    //endregion
}
