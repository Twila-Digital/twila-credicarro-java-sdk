package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import lombok.Value;
import twila.credicarro.internal.util.Masking;

@Value
public class SimulationVehicle {
    String brand;
    String model;
    String version;
    Integer year;
    BigDecimal value;
    String licensePlate;
    Boolean zeroKm;
    String chassis;
    String renavam;
    String color;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("SimulationVehicle", "brand", brand, "model", model, "version", version, "year", year, "value", value, "licensePlate", Masking.mask(licensePlate), "zeroKm", zeroKm, "chassis", Masking.mask(chassis), "renavam", Masking.mask(renavam), "color", color);
    }
}
