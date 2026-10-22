package twila.credicarro.customers.model;

import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Endereço do cliente (`endereco`). {@code number} e {@code complement} são opcionais.
 */
@Value
@Builder
public class CustomerAddress {
    String street;
    String number;
    String complement;
    String neighborhood;
    String city;
    String state;
    String postalCode;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("CustomerAddress", "street", Masking.mask(street), "number", Masking.mask(number), "complement", Masking.mask(complement), "neighborhood", Masking.mask(neighborhood), "city", city, "state", state, "postalCode", Masking.mask(postalCode));
    }
}
