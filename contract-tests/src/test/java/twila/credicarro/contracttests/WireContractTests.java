package twila.credicarro.contracttests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import twila.credicarro.internal.auth.GenerateAccessTokenRequest;
import twila.credicarro.internal.auth.GenerateAccessTokenResponse;
import twila.credicarro.internal.generated.PagedResultWire;
import twila.credicarro.internal.generated.customer.CreateCustomerRequestWire;
import twila.credicarro.internal.generated.customer.CustomerAddressRequestWire;
import twila.credicarro.internal.generated.customer.CustomerIdResponseWire;
import twila.credicarro.internal.generated.customer.CustomerProfessionalRequestWire;
import twila.credicarro.internal.generated.customer.UpsertCustomerRequestWire;
import twila.credicarro.internal.generated.proposal.CreateProposalRequestWire;
import twila.credicarro.internal.generated.proposal.DepositAccountRequestWire;
import twila.credicarro.internal.generated.proposal.DocumentUploadRequestWire;
import twila.credicarro.internal.generated.proposal.ProposalIdResponseWire;
import twila.credicarro.internal.generated.proposal.ProposalV2Wire;
import twila.credicarro.internal.generated.proposal.ProposalWire;
import twila.credicarro.internal.generated.proposal.VehicleDetailRequestWire;
import twila.credicarro.internal.generated.simulation.CreateFinancingSimulationRequestWire;
import twila.credicarro.internal.generated.simulation.CreateRefinancingSimulationRequestWire;
import twila.credicarro.internal.generated.simulation.FinancingRangeResponseWire;
import twila.credicarro.internal.generated.simulation.RefinancingRangeResponseWire;
import twila.credicarro.internal.generated.simulation.SimulationListResponseWire;
import twila.credicarro.internal.generated.simulation.SimulationWire;
import twila.credicarro.internal.generated.simulation.V1CreateFinancingSimulationRequestWire;
import twila.credicarro.internal.generated.simulation.V1CreateRefinancingSimulationRequestWire;
import twila.credicarro.internal.generated.webhook.CreateWebhookRequestWire;
import twila.credicarro.internal.generated.webhook.CreateWebhookResponseWire;
import twila.credicarro.internal.generated.webhook.UpdateWebhookRequestWire;
import twila.credicarro.internal.generated.webhook.WebhookWire;
import twila.credicarro.internal.http.Routes;
import twila.credicarro.internal.idempotency.IdempotencyClassifier;
import twila.credicarro.proposals.model.DepositAccountType;
import twila.credicarro.proposals.model.ProposalParticipant;
import twila.credicarro.proposals.model.ProposalStatus;
import twila.credicarro.proposals.model.ProposalType;
import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.simulations.model.SimulationType;

/**
 * Compara o SDK com o {@code openapi.yaml} público: cada operação usada existe, cada wire type de requisição/resposta só
 * usa propriedades do schema da operação, os campos obrigatórios das requisições existem no wire type, e as rotas com
 * {@code Idempotency-Key} batem com o que o SDK envia.
 */
class WireContractTests {

    private static final String GENERATED_PACKAGE = "twila.credicarro.internal.generated";
    private static final String[] PAGING_QUERY = {"pagina_inicial", "limite"};

    /**
     * Uma operação do SDK: método, rota (modelo do {@link Routes}), wire de requisição/resposta e parâmetros de query.
     */
    private static final class Operation {
        final String method;
        final String route;
        final Class<?> request;
        final Class<?> response;
        final String successStatus;
        final Class<?> itemType;
        final String[] queryParameters;

        Operation(String method, String route, Class<?> request, Class<?> response, String successStatus, Class<?> itemType, String... queryParameters) {
            this.method = method;
            this.route = route;
            this.request = request;
            this.response = response;
            this.successStatus = successStatus;
            this.itemType = itemType;
            this.queryParameters = queryParameters;
        }

        String openApiPath() {
            return "/" + route.replace("%s", "{}");
        }

        @Override
        public String toString() {
            return method + " " + openApiPath();
        }
    }

    static List<Operation> operations() {
        return Arrays.asList(
                new Operation("POST", Routes.ACCESS_TOKEN, GenerateAccessTokenRequest.class, GenerateAccessTokenResponse.class, "200", null),

                new Operation("POST", Routes.CUSTOMER, CreateCustomerRequestWire.class, CustomerIdResponseWire.class, "200", null),
                new Operation("POST", Routes.CUSTOMER_UNIFIED, UpsertCustomerRequestWire.class, CustomerIdResponseWire.class, "200", null),
                new Operation("POST", Routes.CUSTOMER_ADDRESS, CustomerAddressRequestWire.class, null, "202", null),
                new Operation("POST", Routes.CUSTOMER_PROFESSIONAL, CustomerProfessionalRequestWire.class, null, "202", null),

                new Operation("GET", Routes.V1_RANGE_FINANCING, null, FinancingRangeResponseWire.class, "200", null, "placa"),
                new Operation("GET", Routes.V1_RANGE_REFINANCING, null, RefinancingRangeResponseWire.class, "200", null, "placa"),
                new Operation("POST", Routes.V1_SIMULATION_FINANCING, V1CreateFinancingSimulationRequestWire.class, SimulationListResponseWire.class, "201", null),
                new Operation("POST", Routes.V1_SIMULATION_REFINANCING, V1CreateRefinancingSimulationRequestWire.class, SimulationListResponseWire.class, "201", null),
                new Operation("GET", Routes.V1_SIMULATIONS, null, PagedResultWire.class, "200", SimulationWire.class, PAGING_QUERY),
                new Operation("GET", Routes.V1_SIMULATION_BY_ID, null, SimulationWire.class, "200", null),
                new Operation("POST", Routes.V1_PROPOSALS, CreateProposalRequestWire.class, ProposalIdResponseWire.class, "201", null),
                new Operation("GET", Routes.V1_PROPOSALS, null, PagedResultWire.class, "200", ProposalWire.class, PAGING_QUERY),
                new Operation("GET", Routes.V1_PROPOSAL_BY_ID, null, ProposalWire.class, "200", null),

                new Operation("GET", Routes.V2_RANGE_FINANCING, null, FinancingRangeResponseWire.class, "200", null, "placa", "cnpjLoja"),
                new Operation("GET", Routes.V2_RANGE_REFINANCING, null, RefinancingRangeResponseWire.class, "200", null, "placa", "cnpjLoja"),
                new Operation("POST", Routes.V2_SIMULATION_FINANCING, CreateFinancingSimulationRequestWire.class, SimulationListResponseWire.class, "201", null),
                new Operation("POST", Routes.V2_SIMULATION_REFINANCING, CreateRefinancingSimulationRequestWire.class, SimulationListResponseWire.class, "201", null),
                new Operation("POST", Routes.V2_PROPOSALS, CreateProposalRequestWire.class, ProposalIdResponseWire.class, "201", null),
                new Operation("GET", Routes.V2_PROPOSAL_BY_ID, null, ProposalV2Wire.class, "200", null),
                new Operation("POST", Routes.V2_PROPOSAL_VEHICLE_DETAIL, VehicleDetailRequestWire.class, null, "204", null),
                new Operation("POST", Routes.V2_PROPOSAL_DEPOSIT_ACCOUNT, DepositAccountRequestWire.class, null, "204", null),
                new Operation("POST", Routes.V2_PROPOSAL_DOCUMENT, DocumentUploadRequestWire.class, null, "204", null),

                new Operation("POST", Routes.WEBHOOKS, CreateWebhookRequestWire.class, CreateWebhookResponseWire.class, "201", null),
                new Operation("GET", Routes.WEBHOOKS, null, PagedResultWire.class, "200", WebhookWire.class, PAGING_QUERY),
                new Operation("PUT", Routes.WEBHOOK_BY_ID, UpdateWebhookRequestWire.class, null, "204", null),
                new Operation("DELETE", Routes.WEBHOOK_BY_ID, null, null, "204", null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void operationExistsInTheOpenApi(Operation operation) {
        JsonNode openApi = OpenApiFixture.fetch();

        JsonNode node = operationNode(openApi, operation);

        assertThat(node.isMissingNode())
                .withFailMessage("Operação '%s' não encontrada no openapi.yaml — o SDK e a documentação divergiram.", operation)
                .isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void queryParametersExist(Operation operation) {
        JsonNode openApi = OpenApiFixture.fetch();
        Set<String> documented = new TreeSet<>();
        for (JsonNode parameter : operationNode(openApi, operation).path("parameters")) {
            JsonNode resolved = OpenApiFixture.resolve(openApi, parameter);
            if ("query".equals(resolved.path("in").asText()))
                documented.add(resolved.path("name").asText());
        }

        for (String name : operation.queryParameters)
            assertThat(documented).as("%s: parâmetro de query '%s'", operation, name).contains(name);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void requestWireMatchesTheRequestSchema(Operation operation) {
        if (operation.request == null)
            return;

        JsonNode openApi = OpenApiFixture.fetch();
        JsonNode schema = OpenApiFixture.resolve(openApi, operationNode(openApi, operation).path("requestBody").path("content")
                .path("application/json").path("schema"));

        assertThat(schema.isMissingNode()).as("%s: schema de requisição", operation).isFalse();
        assertWireMatchesSchema(openApi, operation.request, null, schema, operation + " (requisição)", true);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void responseWireMatchesTheResponseSchema(Operation operation) {
        if (operation.response == null)
            return;

        JsonNode openApi = OpenApiFixture.fetch();
        JsonNode schema = OpenApiFixture.resolve(openApi, operationNode(openApi, operation).path("responses").path(operation.successStatus)
                .path("content").path("application/json").path("schema"));

        assertThat(schema.isMissingNode()).as("%s: schema de resposta %s", operation, operation.successStatus).isFalse();
        assertWireMatchesSchema(openApi, operation.response, operation.itemType, schema, operation + " (resposta)", false);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void successStatusIsDocumented(Operation operation) {
        JsonNode openApi = OpenApiFixture.fetch();

        assertThat(operationNode(openApi, operation).path("responses").has(operation.successStatus))
                .withFailMessage("%s: status de sucesso %s não documentado.", operation, operation.successStatus)
                .isTrue();
    }

    @Test
    void everyOperationOfTheOpenApiIsCoveredByTheSdk() {
        JsonNode openApi = OpenApiFixture.fetch();
        Set<String> documented = new TreeSet<>();

        Iterator<Map.Entry<String, JsonNode>> paths = openApi.path("paths").fields();
        while (paths.hasNext()) {
            Map.Entry<String, JsonNode> path = paths.next();
            Iterator<String> methods = path.getValue().fieldNames();
            while (methods.hasNext()) {
                String method = methods.next();
                if (Arrays.asList("get", "post", "put", "delete", "patch").contains(method))
                    documented.add(method.toUpperCase() + " " + normalize(path.getKey()));
            }
        }

        Set<String> covered = operations().stream().map(operation -> operation.method + " " + normalize(operation.openApiPath()))
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(covered).as("operações do openapi.yaml sem método no SDK (ou o inverso)").isEqualTo(documented);
    }

    @Test
    void idempotencyKeyRoutesMatchWhatTheSdkSends() {
        JsonNode openApi = OpenApiFixture.fetch();

        for (Operation operation : operations()) {
            if (!"POST".equals(operation.method))
                continue;

            boolean documentedHeader = hasHeader(openApi, operationNode(openApi, operation), "Idempotency-Key");
            String route = operation.route.replace("%s", "bea8d060-f93d-48eb-b6c4-45f64483cdd9");

            assertThat(IdempotencyClassifier.acceptsIdempotencyKey("POST", route))
                    .withFailMessage("%s: o openapi.yaml %s o cabeçalho Idempotency-Key e o SDK %s.", operation,
                            documentedHeader ? "documenta" : "não documenta", documentedHeader ? "não o aceita" : "o aceita")
                    .isEqualTo(documentedHeader);

            if (IdempotencyClassifier.requiresAutomaticKey("POST", route))
                assertThat(documentedHeader).as("%s: chave automática exige o header documentado", operation).isTrue();
        }
    }

    @Test
    void documentUploadRequiresTheIdempotencyKeyHeader() {
        JsonNode openApi = OpenApiFixture.fetch();
        JsonNode operation = openApi.path("paths").path("/v2/proposal/{propostaId}/document").path("post");

        boolean required = false;
        for (JsonNode parameter : operation.path("parameters")) {
            JsonNode resolved = OpenApiFixture.resolve(openApi, parameter);
            if ("Idempotency-Key".equals(resolved.path("name").asText()))
                required = resolved.path("required").asBoolean();
        }

        assertThat(required).as("Idempotency-Key obrigatório no envio de documento").isTrue();
    }

    @Test
    void textualEnumsCoverEveryDocumentedValue() {
        JsonNode openApi = OpenApiFixture.fetch();
        JsonNode schemas = openApi.path("components").path("schemas");

        assertEnumCovered(schemas.path("ProposalV2Response").path("properties").path("status"), ProposalStatus.class);
        assertEnumCovered(schemas.path("ProposalResponse").path("properties").path("status"), ProposalStatus.class);
        assertEnumCovered(schemas.path("ProposalV2Response").path("properties").path("tipo"), ProposalType.class);
        assertEnumCovered(schemas.path("ProposalResponse").path("properties").path("tipo"), ProposalType.class);
        assertEnumCovered(schemas.path("SimulationResponse").path("properties").path("tipo"), SimulationType.class);

        JsonNode deposit = openApi.path("paths").path("/v2/proposal/{propostaId}/deposit-account").path("post").path("requestBody")
                .path("content").path("application/json").path("schema").path("properties").path("tipoConta");
        assertEnumCovered(deposit, DepositAccountType.class);

        JsonNode document = openApi.path("paths").path("/v2/proposal/{propostaId}/document").path("post").path("requestBody")
                .path("content").path("application/json").path("schema").path("properties").path("participante");
        assertEnumCovered(document, ProposalParticipant.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void assertEnumCovered(JsonNode schemaWithEnum, Class enumType) {
        assertThat(schemaWithEnum.has("enum")).as("schema com enum para %s", enumType.getSimpleName()).isTrue();

        for (JsonNode value : schemaWithEnum.get("enum")) {
            Enum<?> mapped;
            if (enumType == ProposalStatus.class) {
                mapped = ProposalStatus.fromName(value.asText());
            } else {
                mapped = (Enum<?>) EnumMapping.fromWireName((Class) enumType, value.asText());
            }

            assertThat(mapped.name()).as("valor '%s' do openapi.yaml em %s", value.asText(), enumType.getSimpleName()).isNotEqualTo("UNKNOWN");
        }
    }

    private static JsonNode operationNode(JsonNode openApi, Operation operation) {
        Iterator<Map.Entry<String, JsonNode>> paths = openApi.path("paths").fields();
        while (paths.hasNext()) {
            Map.Entry<String, JsonNode> path = paths.next();
            if (normalize(path.getKey()).equals(normalize(operation.openApiPath())))
                return path.getValue().path(operation.method.toLowerCase());
        }

        return com.fasterxml.jackson.databind.node.MissingNode.getInstance();
    }

    private static boolean hasHeader(JsonNode openApi, JsonNode operation, String name) {
        for (JsonNode parameter : operation.path("parameters")) {
            JsonNode resolved = OpenApiFixture.resolve(openApi, parameter);
            if ("header".equals(resolved.path("in").asText()) && name.equalsIgnoreCase(resolved.path("name").asText()))
                return true;
        }

        return false;
    }

    private static String normalize(String path) {
        return path.replaceAll("\\{[^}]*}", "{}");
    }

    /**
     * Garante que todo campo do wire existe no schema (recursivamente) e, para requisições, que todo campo obrigatório existe no wire.
     */
    private static void assertWireMatchesSchema(JsonNode openApi, Class<?> wire, Class<?> itemType, JsonNode schema, String where, boolean checkRequired) {
        JsonNode resolved = OpenApiFixture.resolve(openApi, schema);
        JsonNode properties = collectProperties(openApi, resolved);
        Set<String> required = collectRequired(openApi, resolved);

        Set<String> wireNames = new LinkedHashSet<>();

        for (Field field : wire.getFields()) {
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            if (annotation == null || Modifier.isStatic(field.getModifiers()))
                continue;

            String name = annotation.value();
            wireNames.add(name);

            assertThat(properties.has(name))
                    .withFailMessage("%s: o campo '%s' de %s não existe no schema (propriedades: %s).", where, name, wire.getSimpleName(), names(properties))
                    .isTrue();

            JsonNode propertySchema = OpenApiFixture.resolve(openApi, properties.get(name));
            assertTypeCompatible(where + " -> " + wire.getSimpleName() + "." + name, field.getGenericType(), itemType, propertySchema, openApi);
        }

        if (checkRequired) {
            for (String name : required)
                assertThat(wireNames).as("%s: campo obrigatório '%s' ausente em %s", where, name, wire.getSimpleName()).contains(name);
        }
    }

    private static void assertTypeCompatible(String where, Type javaType, Class<?> itemType, JsonNode schema, JsonNode openApi) {
        Class<?> raw = rawClass(javaType);
        String schemaType = schema.path("type").asText("");

        if (schemaType.isEmpty() && !schema.has("allOf") && !schema.has("properties"))
            return;

        if (List.class.isAssignableFrom(raw)) {
            assertThat(schemaType).as("%s deveria ser array no schema", where).isEqualTo("array");

            Type element = ((ParameterizedType) javaType).getActualTypeArguments()[0];
            Class<?> elementClass = element instanceof TypeVariable ? itemType : rawClass(element);
            JsonNode items = OpenApiFixture.resolve(openApi, schema.path("items"));

            if (elementClass != null && elementClass.getName().startsWith(GENERATED_PACKAGE))
                assertWireMatchesSchema(openApi, elementClass, null, items, where + "[]", false);

            return;
        }

        if (raw.getName().startsWith(GENERATED_PACKAGE)) {
            assertWireMatchesSchema(openApi, raw, null, schema, where, false);
            return;
        }

        if (raw == int.class || raw == Integer.class || raw == long.class || raw == Long.class)
            assertThat(schemaType).as("%s deveria ser integer", where).isEqualTo("integer");
        else if (raw == BigDecimal.class || raw == double.class || raw == Double.class)
            assertThat(schemaType).as("%s deveria ser number", where).isEqualTo("number");
        else if (raw == boolean.class || raw == Boolean.class)
            assertThat(schemaType).as("%s deveria ser boolean", where).isEqualTo("boolean");
        else if (raw == String.class || raw == UUID.class || raw == OffsetDateTime.class)
            assertThat(schemaType).as("%s deveria ser string", where).isEqualTo("string");
    }

    private static Class<?> rawClass(Type type) {
        if (type instanceof Class)
            return (Class<?>) type;

        if (type instanceof ParameterizedType)
            return (Class<?>) ((ParameterizedType) type).getRawType();

        return Object.class;
    }

    /**
     * Junta {@code properties} do schema e dos {@code allOf}.
     */
    private static JsonNode collectProperties(JsonNode openApi, JsonNode schema) {
        com.fasterxml.jackson.databind.node.ObjectNode merged = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();

        JsonNode properties = schema.path("properties");
        for (Iterator<String> it = properties.fieldNames(); it.hasNext(); ) {
            String name = it.next();
            merged.set(name, properties.get(name));
        }

        for (JsonNode part : schema.path("allOf")) {
            JsonNode inner = collectProperties(openApi, OpenApiFixture.resolve(openApi, part));
            for (Iterator<String> it = inner.fieldNames(); it.hasNext(); ) {
                String name = it.next();
                merged.set(name, inner.get(name));
            }
        }

        return merged;
    }

    private static Set<String> collectRequired(JsonNode openApi, JsonNode schema) {
        Set<String> required = new LinkedHashSet<>();

        for (JsonNode name : schema.path("required"))
            required.add(name.asText());

        for (JsonNode part : schema.path("allOf"))
            required.addAll(collectRequired(openApi, OpenApiFixture.resolve(openApi, part)));

        return required;
    }

    private static List<String> names(JsonNode properties) {
        List<String> names = new ArrayList<>();
        properties.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
