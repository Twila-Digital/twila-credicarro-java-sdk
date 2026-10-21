package twila.credicarro.sample;

import java.util.List;
import twila.credicarro.CredicarroClient;
import twila.credicarro.config.CredicarroEnvironment;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.simulations.model.FinancingRangeOption;

public final class Main {

    public static void main(String[] args) {
        String clientId = System.getenv("CREDICARRO_CLIENT_ID");
        String clientSecret = System.getenv("CREDICARRO_CLIENT_SECRET");
        String licensePlate = System.getenv("CREDICARRO_SAMPLE_PLATE");
        String storeCnpj = System.getenv("CREDICARRO_SAMPLE_STORE_CNPJ");

        if (clientId == null || clientSecret == null || licensePlate == null || storeCnpj == null) {
            System.err.println("Defina CREDICARRO_CLIENT_ID, CREDICARRO_CLIENT_SECRET, CREDICARRO_SAMPLE_PLATE e "
                    + "CREDICARRO_SAMPLE_STORE_CNPJ no ambiente antes de rodar este sample.");
            System.exit(1);
        }

        try (CredicarroClient client = CredicarroClient.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .environment(CredicarroEnvironment.STAGING)
                .build()) {

            List<FinancingRangeOption> faixa = client.simulations().getFinancingRange(licensePlate, storeCnpj);

            for (FinancingRangeOption opcao : faixa)
                System.out.printf("%d meses: entrada mínima de %s%n", opcao.getTerm(), opcao.getMinimumDownPayment());

        } catch (CredicarroApiException ex) {
            // O detalhe pode conter dados pessoais: registre só o status e o código estável.
            System.err.printf("%d %s%n", ex.getStatusCode(), ex.getErrorCode());
        }
    }
}
