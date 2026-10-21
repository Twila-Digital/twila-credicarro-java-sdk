package twila.credicarro.v1;

import twila.credicarro.internal.http.ApiRequestExecutor;

public final class V1ClientImpl implements V1Client {

    private final V1SimulationsClient simulations;
    private final V1ProposalsClient proposals;

    public V1ClientImpl(ApiRequestExecutor executor) {
        this.simulations = new V1SimulationsClientImpl(executor);
        this.proposals = new V1ProposalsClientImpl(executor);
    }

    @Override
    public V1SimulationsClient simulations() {
        return simulations;
    }

    @Override
    public V1ProposalsClient proposals() {
        return proposals;
    }
}
