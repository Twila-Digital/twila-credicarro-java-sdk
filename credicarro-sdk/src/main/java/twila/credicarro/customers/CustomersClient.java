package twila.credicarro.customers;

import java.util.UUID;
import twila.credicarro.customers.model.CreateCustomerRequest;
import twila.credicarro.customers.model.CustomerAddress;
import twila.credicarro.customers.model.ProfessionalData;
import twila.credicarro.customers.model.UpsertCustomerRequest;

/**
 * Cadastro do cliente (comprador). O cliente é identificado pelo CPF em todas as chamadas.
 */
public interface CustomersClient {

    /**
     * Cadastro completo em uma chamada ({@code POST v1/customer/unified}): cria ou atualiza. <b>Sem
     * {@code Idempotency-Key}</b> a rota faz upsert (CPF existente é atualizado), por isso o SDK não repete
     * automaticamente esta chamada.
     */
    UUID upsert(UpsertCustomerRequest request);

    /**
     * Igual a {@link #upsert(UpsertCustomerRequest)}, mas com {@code Idempotency-Key} explícita. <b>Atenção:</b> com a
     * chave a rota só cria — CPF já cadastrado retorna {@code 409 Cpf.CpfAlreadyAdded}. O SDK nunca gera essa chave
     * automaticamente; a chave informada (1 a 200 caracteres) é a mesma em todas as tentativas, então esta chamada
     * pode ser repetida com segurança.
     */
    UUID upsert(UpsertCustomerRequest request, String idempotencyKey);

    /**
     * Cria o cliente só com os dados pessoais ({@code POST v1/customer}); CPF já cadastrado retorna {@code 409}.
     */
    UUID create(CreateCustomerRequest request);

    /**
     * Cria ou atualiza o endereço de um cliente existente ({@code POST v1/customer/address}, {@code 202}).
     */
    void upsertAddress(String cpf, CustomerAddress address);

    /**
     * Cria ou atualiza ocupação e renda de um cliente existente ({@code POST v1/customer/professional}, {@code 202}).
     */
    void upsertProfessionalData(String cpf, ProfessionalData professionalData);
}
