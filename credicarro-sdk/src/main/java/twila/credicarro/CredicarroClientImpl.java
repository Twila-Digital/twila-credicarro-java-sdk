package twila.credicarro;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import twila.credicarro.config.CredicarroResilienceOptions;
import twila.credicarro.customers.CustomersClient;
import twila.credicarro.customers.CustomersClientImpl;
import twila.credicarro.internal.auth.AccessTokenProvider;
import twila.credicarro.internal.auth.AccessTokenProviderImpl;
import twila.credicarro.internal.auth.TokenApiClientImpl;
import twila.credicarro.internal.http.ApiRequestExecutor;
import twila.credicarro.internal.resilience.ResilienceExecutor;
import twila.credicarro.proposals.ProposalsClient;
import twila.credicarro.proposals.ProposalsClientImpl;
import twila.credicarro.simulations.SimulationsClient;
import twila.credicarro.simulations.SimulationsClientImpl;
import twila.credicarro.v1.V1Client;
import twila.credicarro.v1.V1ClientImpl;
import twila.credicarro.webhooks.WebhooksClient;
import twila.credicarro.webhooks.WebhooksClientImpl;

final class CredicarroClientImpl implements CredicarroClient {

    private final OkHttpClient httpClient;
    private final URI baseUrl;

    private final CustomersClient customers;
    private final SimulationsClient simulations;
    private final ProposalsClient proposals;
    private final WebhooksClient webhooks;
    private final V1Client v1;

    CredicarroClientImpl(String clientId, String clientSecret, URI baseUrl, CredicarroResilienceOptions resilience) {
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .connectTimeout(0, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        ResilienceExecutor resilienceExecutor = new ResilienceExecutor(resilience);
        TokenApiClientImpl tokenApiClient = new TokenApiClientImpl(httpClient, baseUrl, resilience.getAttemptTimeout(), resilienceExecutor);
        AccessTokenProvider tokenProvider = new AccessTokenProviderImpl(tokenApiClient, clientId, clientSecret);
        ApiRequestExecutor apiRequestExecutor = new ApiRequestExecutor(httpClient, baseUrl, tokenProvider, resilienceExecutor, resilience);

        this.customers = new CustomersClientImpl(apiRequestExecutor);
        this.simulations = new SimulationsClientImpl(apiRequestExecutor);
        this.proposals = new ProposalsClientImpl(apiRequestExecutor);
        this.webhooks = new WebhooksClientImpl(apiRequestExecutor);
        this.v1 = new V1ClientImpl(apiRequestExecutor);
    }

    @Override
    public CustomersClient customers() {
        return customers;
    }

    @Override
    public SimulationsClient simulations() {
        return simulations;
    }

    @Override
    public ProposalsClient proposals() {
        return proposals;
    }

    @Override
    public WebhooksClient webhooks() {
        return webhooks;
    }

    @Override
    public V1Client v1() {
        return v1;
    }

    @Override
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

    /**
     * Nunca inclui credenciais.
     */
    @Override
    public String toString() {
        return "CredicarroClient[baseUrl=" + baseUrl + "]";
    }
}
