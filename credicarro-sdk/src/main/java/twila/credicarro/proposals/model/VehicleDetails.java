package twila.credicarro.proposals.model;

import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Dados do veículo ({@code POST v2/proposal/{id}/vehicle-detail}), aceitos na etapa DADOS_VEICULO (8).
 */
@Value
@Builder
public class VehicleDetails {
    String renavam;
    String chassis;
    String licensePlate;
    String color;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("VehicleDetails", "renavam", Masking.mask(renavam), "chassis", Masking.mask(chassis), "licensePlate", Masking.mask(licensePlate), "color", color);
    }
}
