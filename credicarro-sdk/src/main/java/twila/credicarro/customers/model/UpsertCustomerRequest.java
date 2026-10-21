package twila.credicarro.customers.model;

import java.time.OffsetDateTime;
import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Cadastro completo para {@code POST v1/customer/unified}: dados pessoais, endereço e dados profissionais.
 * O celular segue o formato {@code +55 (DD) 99999-9999}.
 */
@Value
@Builder
public class UpsertCustomerRequest {
    String cpf;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    OffsetDateTime dateOfBirth;
    CustomerAddress address;
    ProfessionalData professionalData;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("UpsertCustomerRequest", "cpf", Masking.cpf(cpf), "firstName", Masking.mask(firstName), "lastName", Masking.mask(lastName), "email", Masking.mask(email), "phoneNumber", Masking.mask(phoneNumber), "dateOfBirth", Masking.mask(dateOfBirth), "address", address, "professionalData", professionalData);
    }
}
