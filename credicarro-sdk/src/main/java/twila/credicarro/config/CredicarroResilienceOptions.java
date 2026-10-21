package twila.credicarro.config;

import java.time.Duration;
import lombok.Builder;
import lombok.Value;

/**
 * Configuração da pipeline de resiliência (timeout, retry, circuit breaker, idempotência) do client.
 */
@Value
@Builder
public class CredicarroResilienceOptions {

    /**
     * Timeout total de uma chamada lógica, incluindo todas as tentativas de retry e a geração do token. Default: 30s.
     * No envio de documentos o orçamento é {@link #documentUploadAttemptTimeout} × {@link #maxRetryAttempts} (180 s por padrão).
     */
    @Builder.Default
    Duration totalTimeout = Duration.ofSeconds(30);

    /**
     * Timeout de uma única tentativa HTTP. Default: 10s.
     */
    @Builder.Default
    Duration attemptTimeout = Duration.ofSeconds(10);

    /**
     * Timeout de uma única tentativa para o envio de documentos (payload em Base64, maior). Default: 60s.
     */
    @Builder.Default
    Duration documentUploadAttemptTimeout = Duration.ofSeconds(60);

    /**
     * Número máximo de tentativas (incluindo a original) para requisições seguras de repetir. Default: 3.
     */
    @Builder.Default
    int maxRetryAttempts = 3;

    /**
     * Delay base do backoff exponencial com jitter entre tentativas. Default: 500ms.
     */
    @Builder.Default
    Duration retryBaseDelay = Duration.ofMillis(500);

    /**
     * Proporção de falhas na janela de amostragem que abre o circuit breaker. Default: 0.5 (50%).
     */
    @Builder.Default
    double circuitBreakerFailureRatio = 0.5;

    /**
     * Janela de amostragem do circuit breaker, em segundos (janela por tempo). Default: 30.
     */
    @Builder.Default
    int circuitBreakerSlidingWindowSize = 30;

    /**
     * Quantidade mínima de chamadas na janela para o circuit breaker considerar abrir. Default: 10.
     */
    @Builder.Default
    int circuitBreakerMinimumThroughput = 10;

    /**
     * Tempo que o circuit breaker permanece aberto antes de testar meia-abertura. Default: 15s.
     */
    @Builder.Default
    Duration circuitBreakerBreakDuration = Duration.ofSeconds(15);

    /**
     * Trata HTTP 500 como transitório (repetível). Default: {@code true} — a documentação do Credicarro manda
     * repetir {@code 500} com a mesma {@code Idempotency-Key}; em rotas sem chave o SDK só repete GET e rotas de
     * substituição.
     */
    @Builder.Default
    boolean retryOn500 = true;

    /**
     * Desabilita o envio automático de {@code Idempotency-Key} nas rotas que a aceitam (cadastro de cliente,
     * endereço, dados profissionais, simulações e propostas). Sem a chave essas rotas deixam de ser repetidas
     * automaticamente. O envio de documentos sempre usa chave, pois ela é obrigatória. Default: {@code false}.
     */
    boolean disableAutomaticIdempotencyKey;
}
