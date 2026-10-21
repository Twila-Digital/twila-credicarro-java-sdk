package twila.credicarro.customers.model;

import java.time.OffsetDateTime;
import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Dados pessoais para {@code POST v1/customer}. O celular segue o formato {@code +55 (DD) 99999-9999}.
 */
@Value
@Builder
public class CreateCustomerRequest {
    String cpf;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    OffsetDateTime dateOfBirth;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("CreateCustomerRequest", "cpf", Masking.cpf(cpf), "firstName", Masking.mask(firstName), "lastName", Masking.mask(lastName), "email", Masking.mask(email), "phoneNumber", Masking.mask(phoneNumber), "dateOfBirth", Masking.mask(dateOfBirth));
    }
}
