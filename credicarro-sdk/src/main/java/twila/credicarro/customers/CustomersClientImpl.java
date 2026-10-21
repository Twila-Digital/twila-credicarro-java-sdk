package twila.credicarro.customers;

import java.util.Objects;
import java.util.UUID;
import twila.credicarro.customers.model.CreateCustomerRequest;
import twila.credicarro.customers.model.CustomerAddress;
import twila.credicarro.customers.model.ProfessionalData;
import twila.credicarro.customers.model.UpsertCustomerRequest;
import twila.credicarro.internal.generated.customer.CustomerIdResponseWire;
import twila.credicarro.internal.http.ApiRequestExecutor;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.JsonSupport;
import twila.credicarro.internal.http.Routes;
import twila.credicarro.internal.mapping.CustomerMapper;

public final class CustomersClientImpl implements CustomersClient {

    private final ApiRequestExecutor executor;

    public CustomersClientImpl(ApiRequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public UUID upsert(UpsertCustomerRequest request) {
        return upsert(request, null);
    }

    @Override
    public UUID upsert(UpsertCustomerRequest request, String idempotencyKey) {
        Objects.requireNonNull(request, "request é obrigatório.");

        ApiResponse response = executor.post(Routes.CUSTOMER_UNIFIED, JsonSupport.write(CustomerMapper.toWire(request)), idempotencyKey);
        ApiRequestExecutor.ensureSuccess(response);

        return JsonSupport.read(response, CustomerIdResponseWire.class).idCliente;
    }

    @Override
    public UUID create(CreateCustomerRequest request) {
        Objects.requireNonNull(request, "request é obrigatório.");

        ApiResponse response = executor.post(Routes.CUSTOMER, JsonSupport.write(CustomerMapper.toWire(request)));
        ApiRequestExecutor.ensureSuccess(response);

        return JsonSupport.read(response, CustomerIdResponseWire.class).idCliente;
    }

    @Override
    public void upsertAddress(String cpf, CustomerAddress address) {
        Objects.requireNonNull(cpf, "cpf é obrigatório.");
        Objects.requireNonNull(address, "address é obrigatório.");

        ApiResponse response = executor.post(Routes.CUSTOMER_ADDRESS, JsonSupport.write(CustomerMapper.toWire(cpf, address)));
        ApiRequestExecutor.ensureSuccess(response);
    }

    @Override
    public void upsertProfessionalData(String cpf, ProfessionalData professionalData) {
        Objects.requireNonNull(cpf, "cpf é obrigatório.");
        Objects.requireNonNull(professionalData, "professionalData é obrigatório.");

        ApiResponse response = executor.post(Routes.CUSTOMER_PROFESSIONAL, JsonSupport.write(CustomerMapper.toWire(cpf, professionalData)));
        ApiRequestExecutor.ensureSuccess(response);
    }
}
