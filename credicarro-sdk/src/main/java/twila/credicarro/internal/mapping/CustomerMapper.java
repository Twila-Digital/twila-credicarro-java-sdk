package twila.credicarro.internal.mapping;

import twila.credicarro.customers.model.CreateCustomerRequest;
import twila.credicarro.customers.model.CustomerAddress;
import twila.credicarro.customers.model.ProfessionalData;
import twila.credicarro.customers.model.UpsertCustomerRequest;
import twila.credicarro.internal.generated.customer.CreateCustomerRequestWire;
import twila.credicarro.internal.generated.customer.CustomerAddressRequestWire;
import twila.credicarro.internal.generated.customer.CustomerAddressWire;
import twila.credicarro.internal.generated.customer.CustomerProfessionalRequestWire;
import twila.credicarro.internal.generated.customer.ProfessionalDataWire;
import twila.credicarro.internal.generated.customer.UpsertCustomerRequestWire;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CreateCustomerRequestWire toWire(CreateCustomerRequest request) {
        CreateCustomerRequestWire wire = new CreateCustomerRequestWire();
        wire.cpf = request.getCpf();
        wire.nome = request.getFirstName();
        wire.sobrenome = request.getLastName();
        wire.email = request.getEmail();
        wire.celular = request.getPhoneNumber();
        wire.dataDeNascimento = request.getDateOfBirth();
        return wire;
    }

    public static UpsertCustomerRequestWire toWire(UpsertCustomerRequest request) {
        UpsertCustomerRequestWire wire = new UpsertCustomerRequestWire();
        wire.cpf = request.getCpf();
        wire.nome = request.getFirstName();
        wire.sobrenome = request.getLastName();
        wire.email = request.getEmail();
        wire.celular = request.getPhoneNumber();
        wire.dataDeNascimento = request.getDateOfBirth();
        wire.endereco = request.getAddress() != null ? toWire(request.getAddress()) : null;
        wire.dadosProfissionais = request.getProfessionalData() != null ? toWire(request.getProfessionalData()) : null;
        return wire;
    }

    public static CustomerAddressRequestWire toWire(String cpf, CustomerAddress address) {
        CustomerAddressRequestWire wire = new CustomerAddressRequestWire();
        wire.cpf = cpf;
        wire.endereco = toWire(address);
        return wire;
    }

    public static CustomerProfessionalRequestWire toWire(String cpf, ProfessionalData data) {
        CustomerProfessionalRequestWire wire = new CustomerProfessionalRequestWire();
        wire.cpf = cpf;
        wire.tipoOcupacao = data.getOccupation().wireValue();
        wire.renda = data.getMonthlyIncome();
        return wire;
    }

    public static CustomerAddressWire toWire(CustomerAddress address) {
        CustomerAddressWire wire = new CustomerAddressWire();
        wire.rua = address.getStreet();
        wire.numero = address.getNumber();
        wire.complemento = address.getComplement();
        wire.bairro = address.getNeighborhood();
        wire.cidade = address.getCity();
        wire.estado = address.getState();
        wire.cep = address.getPostalCode();
        return wire;
    }

    public static ProfessionalDataWire toWire(ProfessionalData data) {
        ProfessionalDataWire wire = new ProfessionalDataWire();
        wire.tipoOcupacao = data.getOccupation().wireValue();
        wire.renda = data.getMonthlyIncome();
        return wire;
    }
}
