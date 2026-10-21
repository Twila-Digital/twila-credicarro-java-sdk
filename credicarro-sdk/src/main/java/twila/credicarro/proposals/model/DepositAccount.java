package twila.credicarro.proposals.model;

import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Conta de recebimento de uma proposta de refinanciamento, aceita na etapa CONTA_RECEBIMENTO (5).
 * Enviar novamente substitui a conta anterior.
 */
@Value
@Builder
public class DepositAccount {
    String holderCpf;
    String bankCode;
    String branch;
    String branchDigit;
    String accountNumber;
    String accountDigit;
    DepositAccountType accountType;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("DepositAccount", "holderCpf", Masking.cpf(holderCpf), "bankCode", Masking.mask(bankCode), "branch", Masking.mask(branch), "branchDigit", Masking.mask(branchDigit), "accountNumber", Masking.mask(accountNumber), "accountDigit", Masking.mask(accountDigit), "accountType", accountType);
    }
}
