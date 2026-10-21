package twila.credicarro;

import static org.assertj.core.api.Assertions.assertThat;

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
import twila.credicarro.errors.CredicarroApiError;
import twila.credicarro.internal.util.Masking;
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.DepositAccount;
import twila.credicarro.proposals.model.DepositAccountType;
import twila.credicarro.proposals.model.ProposalDetails;
import twila.credicarro.proposals.model.ProposalSummary;
import twila.credicarro.simulations.model.CreateFinancingSimulationRequest;
import twila.credicarro.webhooks.model.CreateWebhookRequest;
import twila.credicarro.webhooks.model.CreateWebhookResult;
import twila.credicarro.webhooks.model.WebhookAuthenticationType;
import twila.credicarro.webhooks.model.WebhookType;

/**
 * toString de requisições e respostas nunca revela CPF, e-mail, celular, renda, conta, credencial ou segredo.
 */
class PiiMaskingTest {

    @Test
    void cpfIsMaskedKeepingTheMiddleDigits() {
        assertThat(Masking.cpf("12345678909")).isEqualTo("***.456.789-**");
        assertThat(Masking.cpf("123.456.789-09")).isEqualTo("***.456.789-**");
        assertThat(Masking.cpf("11222333000181")).isEqualTo("***");
        assertThat(Masking.cpf(null)).isNull();
    }

    @Test
    void customerRequestsAreMasked() {
        UpsertCustomerRequest request = UpsertCustomerRequest.builder()
                .cpf("12345678909").firstName("Maria").lastName("Souza").email("maria.souza@exemplo.com.br")
                .phoneNumber("+55 (11) 99876-5432").dateOfBirth(OffsetDateTime.of(1990, 5, 20, 0, 0, 0, 0, ZoneOffset.UTC))
                .address(CustomerAddress.builder().street("Av. Paulista").number("1578").postalCode("01311000").city("São Paulo").state("SP").build())
                .professionalData(ProfessionalData.builder().occupation(OccupationType.EMPLOYEE).monthlyIncome(new BigDecimal("6543.21")).build())
                .build();

        assertThat(request.toString())
                .contains("***.456.789-**")
                .doesNotContain("12345678909").doesNotContain("Maria").doesNotContain("Souza").doesNotContain("maria.souza")
                .doesNotContain("99876").doesNotContain("1990").doesNotContain("Paulista").doesNotContain("01311000").doesNotContain("6543");

        assertThat(CreateCustomerRequest.builder().cpf("12345678909").email("a@b.com").build().toString())
                .doesNotContain("12345678909").doesNotContain("a@b.com");
    }

    @Test
    void bankAccountAndCredentialsAreMasked() {
        DepositAccount account = DepositAccount.builder().holderCpf("12345678909").bankCode("001").branch("1234").branchDigit("5")
                .accountNumber("987654").accountDigit("7").accountType(DepositAccountType.CHECKING).build();

        assertThat(account.toString()).doesNotContain("12345678909").doesNotContain("1234").doesNotContain("987654").contains("CHECKING");

        CreateWebhookRequest webhook = CreateWebhookRequest.builder().type(WebhookType.PROPOSAL).url("https://x.com/w")
                .authenticationType(WebhookAuthenticationType.JWT).credential("jwt-super-secreto").build();
        assertThat(webhook.toString()).doesNotContain("jwt-super-secreto");

        assertThat(new CreateWebhookResult(UUID.randomUUID(), "segredo-de-assinatura").toString()).doesNotContain("segredo-de-assinatura");
    }

    @Test
    void proposalAndSimulationObjectsAreMasked() {
        assertThat(CreateProposalRequest.builder().cpf("12345678909").simulationId(UUID.randomUUID()).build().toString()).doesNotContain("12345678909");
        assertThat(CreateFinancingSimulationRequest.builder().cpf("12345678909").licensePlate("ABC1D23").build().toString())
                .doesNotContain("12345678909").doesNotContain("ABC1D23");
        assertThat(new ProposalSummary(null, 1L, null, null, null, "x", "123.456.789-01", "987.654.321-00", null).toString())
                .doesNotContain("123.456.789-01").doesNotContain("987.654.321-00").contains("***.456.789-**");
        assertThat(new ProposalDetails(null, 1L, null, null, 0, "x", null, null, "motivo privado", "https://link/sessao-secreta", null, null, null).toString())
                .doesNotContain("sessao-secreta").doesNotContain("motivo privado");
    }

    @Test
    void apiErrorMessagesAreMasked() {
        assertThat(new CredicarroApiError("X.Y", "O CPF 123.456.789-09 é inválido", "cpf").toString()).doesNotContain("123.456.789-09");
    }
}
