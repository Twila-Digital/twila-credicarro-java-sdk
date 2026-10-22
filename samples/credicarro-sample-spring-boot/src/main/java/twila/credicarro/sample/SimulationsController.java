package twila.credicarro.sample;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import twila.credicarro.CredicarroClient;
import twila.credicarro.simulations.model.FinancingRangeOption;

@RestController
public class SimulationsController {

    private final CredicarroClient client;

    public SimulationsController(CredicarroClient client) {
        this.client = client;
    }

    @GetMapping("/simulations/financing/range")
    public List<FinancingRangeOption> financingRange(@RequestParam String licensePlate, @RequestParam String storeCnpj) {
        return client.simulations().getFinancingRange(licensePlate, storeCnpj);
    }
}
