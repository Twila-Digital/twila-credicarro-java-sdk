package twila.credicarro.customers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.customers.model.CreateCustomerRequest;
import twila.credicarro.customers.model.CustomerAddress;
import twila.credicarro.customers.model.OccupationType;
import twila.credicarro.customers.model.ProfessionalData;
import twila.credicarro.customers.model.UpsertCustomerRequest;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.errors.CredicarroValidationException;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;

class CustomersClientWireMockTest extends ApiTestSupport {

    private static final OffsetDateTime BIRTH = OffsetDateTime.of(1990, 5, 20, 0, 0, 0, 0, ZoneOffset.ofHours(-3));

    private static final String UNIFIED_BODY = "{'cpf':'12345678909','nome':'Maria','sobrenome':'Souza','email':'maria.souza@exemplo.com.br',"
            + "'celular':'+55 (11) 99876-5432','dataDeNascimento':'1990-05-20T00:00:00-03:00',"
            + "'endereco':{'rua':'Av. Paulista','numero':'1578','complemento':'Bloco B, Apto 1203','bairro':'Bela Vista','cidade':'São Paulo','estado':'SP','cep':'01311000'},"
            + "'dadosProfissionais':{'tipoOcupacao':3,'renda':6500.00}}";

    private static CustomerAddress address() {
        return CustomerAddress.builder().street("Av. Paulista").number("1578").complement("Bloco B, Apto 1203")
                .neighborhood("Bela Vista").city("São Paulo").state("SP").postalCode("01311000").build();
    }

    @Test
    void upsertSendsTheUnifiedPayloadAndReturnsTheCustomerId() {
        server.stubFor(post(urlEqualTo(path("v1/customer/unified"))).willReturn(jsonResponse(200, "{'id_cliente':'" + Fixtures.CUSTOMER_ID + "'}")));

        UUID id = client.customers().upsert(UpsertCustomerRequest.builder()
                .cpf("12345678909").firstName("Maria").lastName("Souza").email("maria.souza@exemplo.com.br")
                .phoneNumber("+55 (11) 99876-5432").dateOfBirth(BIRTH).address(address())
                .professionalData(ProfessionalData.builder().occupation(OccupationType.EMPLOYEE).monthlyIncome(new BigDecimal("6500.00")).build())
                .build());

        assertThat(id).isEqualTo(UUID.fromString(Fixtures.CUSTOMER_ID));
        server.verify(postRequestedFor(urlEqualTo(path("v1/customer/unified"))).withRequestBody(equalToJson(json(UNIFIED_BODY))));
    }

    @Test
    void dateOfBirthIsSerializedAsIso8601WithOffsetNeverAsANumber() {
        server.stubFor(post(urlEqualTo(path("v1/customer"))).willReturn(jsonResponse(200, "{'id_cliente':'" + Fixtures.CUSTOMER_ID + "'}")));

        client.customers().create(CreateCustomerRequest.builder().cpf("12345678909").firstName("Maria").lastName("Souza")
                .email("maria.souza@exemplo.com.br").phoneNumber("+55 (11) 99876-5432").dateOfBirth(BIRTH).build());

        server.verify(postRequestedFor(urlEqualTo(path("v1/customer"))).withRequestBody(
                equalToJson(json("{'cpf':'12345678909','nome':'Maria','sobrenome':'Souza','email':'maria.souza@exemplo.com.br',"
                        + "'celular':'+55 (11) 99876-5432','dataDeNascimento':'1990-05-20T00:00:00-03:00'}"), false, false)));
    }

    @Test
    void createSendsOnlyThePersonalFields() {
        server.stubFor(post(urlEqualTo(path("v1/customer"))).willReturn(jsonResponse(200, "{'id_cliente':'" + Fixtures.CUSTOMER_ID + "'}")));

        UUID id = client.customers().create(CreateCustomerRequest.builder().cpf("12345678909").firstName("Maria").lastName("Souza")
                .email("maria.souza@exemplo.com.br").phoneNumber("+55 (11) 99876-5432").dateOfBirth(BIRTH).build());

        assertThat(id).isEqualTo(UUID.fromString(Fixtures.CUSTOMER_ID));
        server.verify(postRequestedFor(urlEqualTo(path("v1/customer"))).withRequestBody(
                equalToJson(json("{'cpf':'12345678909','nome':'Maria','sobrenome':'Souza','email':'maria.souza@exemplo.com.br',"
                        + "'celular':'+55 (11) 99876-5432','dataDeNascimento':'1990-05-20T00:00:00-03:00'}"))));
    }

    @Test
    void upsertAddressSendsCpfAndAddressAndAccepts202() {
        server.stubFor(post(urlEqualTo(path("v1/customer/address"))).willReturn(aResponse().withStatus(202).withBody("Accepted")));

        client.customers().upsertAddress("12345678909", CustomerAddress.builder().street("Av. Paulista").neighborhood("Bela Vista")
                .city("São Paulo").state("SP").postalCode("01311000").build());

        // numero e complemento opcionais são omitidos (e não enviados como null).
        server.verify(postRequestedFor(urlEqualTo(path("v1/customer/address"))).withRequestBody(
                equalToJson(json("{'cpf':'12345678909','endereco':{'rua':'Av. Paulista','bairro':'Bela Vista','cidade':'São Paulo','estado':'SP','cep':'01311000'}}"),
                        false, false)));
    }

    @Test
    void upsertProfessionalDataSendsOccupationCodeAndIncome() {
        server.stubFor(post(urlEqualTo(path("v1/customer/professional"))).willReturn(aResponse().withStatus(202)));

        client.customers().upsertProfessionalData("12345678909",
                ProfessionalData.builder().occupation(OccupationType.BUSINESS_OWNER).monthlyIncome(new BigDecimal("6500.00")).build());

        server.verify(postRequestedFor(urlEqualTo(path("v1/customer/professional"))).withRequestBody(
                equalToJson(json("{'cpf':'12345678909','tipoOcupacao':7,'renda':6500.00}"))));
    }

    @Test
    void conflictOnAlreadyRegisteredCpfSurfacesTheStableErrorCode() {
        server.stubFor(post(urlEqualTo(path("v1/customer"))).willReturn(jsonResponse(409,
                "{'tipo':'Cpf.CpfAlreadyAdded','titulo':'Conflito','status':409,'detalhe':'CPF já cadastrado.'}")));

        assertThatThrownBy(() -> client.customers().create(CreateCustomerRequest.builder().cpf("12345678909").build()))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> {
                    CredicarroApiException api = (CredicarroApiException) ex;
                    assertThat(api.getStatusCode()).isEqualTo(409);
                    assertThat(api.getErrorCode()).isEqualTo("Cpf.CpfAlreadyAdded");
                });
    }

    @Test
    void validationErrorsCarryTheFieldName() {
        server.stubFor(post(urlEqualTo(path("v1/customer/address"))).willReturn(jsonResponse(400,
                "{'tipo':'Validation.ZipCode.Invalid','titulo':'Requisição inválida','status':400,'detalhe':'CEP inválido.','campo':'endereco.cep'}")));

        assertThatThrownBy(() -> client.customers().upsertAddress("12345678909", address()))
                .isInstanceOf(CredicarroValidationException.class)
                .satisfies(ex -> assertThat(((CredicarroValidationException) ex).getField()).isEqualTo("endereco.cep"));
    }
}
