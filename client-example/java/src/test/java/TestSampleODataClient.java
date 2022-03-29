import org.apache.olingo.client.api.communication.request.batch.ODataBatchResponseItem;
import org.apache.olingo.client.api.communication.response.ODataResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.*;
import org.apache.olingo.commons.api.edm.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.*;


public class TestSampleODataClient {

    private static final String SERVICE_URL = "https://entity-api1-1.demo.abacus.ch/api/entity/v1/mandants/7777";
    private static SampleODataClient client;

    @BeforeAll
    static void init() {
        client =  new SampleODataClient(AuthClientType.SCRIBE);
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
       print("\n----- Read Entities ------------------------------");
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator =
                client.readEntities(SERVICE_URL, "Subjects");

        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();
           print("Entry:\n" + prettyPrint(ce.getProperties(), 0));
        }
    }

    @Test
    void testReadSubjectsWithSelect() {
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator;
        print("\n----- Read Entry with $select ------------------------------");
        iterator = client.readEntitiesWithSelect(
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
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator;
        print("\n----- Read Entry with $orderby ------------------------------");
        iterator = client.readEntitiesWithOrderBy(
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
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator;
        print("\n----- Read Entry with $format ------------------------------");
        iterator = client.readEntitiesWithFormat(
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
       print("\n----- Read Entities with $filter  ------------------------------");
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = client.readEntitiesWithFilter(
                SERVICE_URL,
                "Subjects",
                "Id eq 2"
        );
        while (iterator.hasNext()) {
            ClientEntity ce = iterator.next();
           print("Entry:\n" + prettyPrint(ce.getProperties(), 0));
        }
    }

    @Test
    void testReadSubjectsWithKey() {
       print("\n----- Read Entry with key segment -----------------------");
        ClientEntity entry = client.readEntityWithKey(SERVICE_URL, "Subjects", 1);
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
    void testReadSubjectsWithKeyAndExpand() {
        print("\n----- Read Entity with $expand  ------------------------------");
        var entry = client.readEntityWithKeyExpand(
                "http://localhost:8081/api/entity/v1/mandants/7777/",
                "Activities",
                UUID.fromString("dc021300-947f-e301-0204-e03720524153"),
                "Address"
        );
        print("Single Activities Entry with expanded Addresses relation:\n" + prettyPrint(entry.getProperties(), 0));
    }

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
        builder.append("  ".repeat(Math.max(0, intendLevel)));
    }
    //endregion
}
