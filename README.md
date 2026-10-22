<p align="center">
  <img src="https://raw.githubusercontent.com/Twila-Digital/twila-credicarro-java-sdk/production/assets/logo-light.svg" alt="Credicarro" width="180" style="max-width: 100%;">
</p>

<p align="center">
  <a href="LICENSE"><img alt="License" src="https://img.shields.io/github/license/Twila-Digital/twila-credicarro-java-sdk"></a>
  <a href="https://github.com/Twila-Digital/twila-credicarro-java-sdk/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/Twila-Digital/twila-credicarro-java-sdk/actions/workflows/ci.yml/badge.svg"></a>
  <a href="https://github.com/Twila-Digital/twila-credicarro-java-sdk/actions/workflows/quality.yml"><img alt="Quality" src="https://github.com/Twila-Digital/twila-credicarro-java-sdk/actions/workflows/quality.yml/badge.svg"></a>
  <a href="https://github.com/Twila-Digital/twila-credicarro-java-sdk/security/code-scanning"><img alt="Security" src="https://github.com/Twila-Digital/twila-credicarro-java-sdk/actions/workflows/security.yml/badge.svg"></a>
  <a href="https://codecov.io/gh/Twila-Digital/twila-credicarro-java-sdk"><img alt="Coverage" src="https://codecov.io/gh/Twila-Digital/twila-credicarro-java-sdk/branch/production/graph/badge.svg"></a>
  <img alt="Java" src="https://img.shields.io/badge/Java-8%2B-512BD4">
</p>

# Credicarro (Java SDK)

SDK oficial em Java para a API do Credicarro — financiamento e refinanciamento de veículos: cadastro do cliente, simulação, proposta, envio de documentos e webhooks.

> Uso restrito a server-side. O `clientSecret` nunca deve ser embarcado em um app Android, applet ou qualquer código que rode no dispositivo do usuário final.

## Compatibilidade

| Runtime | Versões aceitas |
| --- | --- |
| JDK | 8 ou superior (compilado com `--release 8`; testes unitários rodam em CI nos JDK 11 e 21, pois o WireMock 3 usado nos testes exige JDK 11+, e o `scripts/LegacySmoke.java` roda no JDK 8) |

HTTP via [OkHttp](https://square.github.io/okhttp/) — `java.net.http.HttpClient` do próprio JDK exigiria Java 11+, incompatível com o piso de Java 8 do SDK.

## Instalação

Maven:

```xml
<dependency>
    <groupId>br.com.twila</groupId>
    <artifactId>credicarro</artifactId>
    <version>1.0.0</version>
</dependency>
```

Gradle (Kotlin DSL):

```kotlin
implementation("br.com.twila:credicarro:1.0.0")
```

## Quick start

```java
import twila.credicarro.CredicarroClient;
import twila.credicarro.config.CredicarroEnvironment;

try (CredicarroClient client = CredicarroClient.builder()
        .clientId("<client-id>")
        .clientSecret("<client-secret>")
        .environment(CredicarroEnvironment.STAGING)
        .build()) {
    // client.customers(), client.simulations(), client.proposals(), client.webhooks(), client.v1()
}
```

O ambiente padrão é `PRODUCTION`. As URLs são `https://api.staging.credicarro.com.br/integration/` (staging) e `https://api.credicarro.com.br/integration/` (produção). `baseUrl(URI)` sobrescreve a URL e precisa ser HTTPS (exceto `localhost` e `127.0.0.1`, para testes).

`CredicarroClient` é thread-safe e deve ser reaproveitado como singleton na sua aplicação (ele mantém o pool de conexões HTTP, o cache do token de acesso e o estado do circuit breaker) — feche-o só no shutdown, daí o `try-with-resources` acima ser normalmente o `main`/bootstrap da aplicação, não cada chamada.

O token de acesso é gerado (com retry próprio) e renovado automaticamente: fica em cache até o `expira_em`, a renovação acontece uma única vez mesmo com várias threads, e um `401` numa rota autenticada renova o token e repete a chamada uma vez.

## Fluxo completo

### 1. Cadastrar o cliente

O cliente é identificado pelo CPF. `upsert` envia dados pessoais, endereço e dados profissionais de uma vez (`POST v1/customer/unified`) e já deixa o cliente pronto para a proposta.

```java
import twila.credicarro.customers.model.CustomerAddress;
import twila.credicarro.customers.model.OccupationType;
import twila.credicarro.customers.model.ProfessionalData;
import twila.credicarro.customers.model.UpsertCustomerRequest;

UUID clienteId = client.customers().upsert(UpsertCustomerRequest.builder()
        .cpf("12345678909")
        .firstName("Maria")
        .lastName("Souza")
        .email("maria.souza@exemplo.com.br")
        .phoneNumber("+55 (11) 99876-5432")
        .dateOfBirth(OffsetDateTime.of(1990, 5, 20, 0, 0, 0, 0, ZoneOffset.of("-03:00")))
        .address(CustomerAddress.builder()
                .street("Av. Paulista")
                .number("1578")
                .neighborhood("Bela Vista")
                .city("São Paulo")
                .state("SP")
                .postalCode("01311000")
                .build())
        .professionalData(ProfessionalData.builder()
                .occupation(OccupationType.EMPLOYEE)
                .monthlyIncome(new BigDecimal("6500.00"))
                .build())
        .build());
```

> **Criar x atualizar.** Sem `Idempotency-Key`, `upsert` cria ou atualiza o cadastro. Com a chave (`upsert(request, idempotencyKey)`), a rota **só cria** — um CPF já cadastrado retorna `409 Cpf.CpfAlreadyAdded`. Por isso o SDK **nunca** gera chave automática nesta rota, e ela não é repetida automaticamente quando você não informa uma chave.

### 2. Simular

As simulações v2 selecionam a loja de integração pelo CNPJ (`storeCnpj`). Cada opção devolvida é um prazo; o `simulationId` da opção escolhida é usado para criar a proposta.

```java
import twila.credicarro.simulations.model.CreateFinancingSimulationRequest;
import twila.credicarro.simulations.model.FinancingRangeOption;
import twila.credicarro.simulations.model.SimulationOption;

List<FinancingRangeOption> faixa = client.simulations().getFinancingRange("ABC1D23", "11.222.333/0001-81");

List<SimulationOption> opcoes = client.simulations().createFinancing(CreateFinancingSimulationRequest.builder()
        .cpf("12345678909")
        .licensePlate("ABC1D23")
        .vehicleAmount(new BigDecimal("45000"))
        .downPayment(new BigDecimal("5000"))
        .state("SP")
        .isZeroKm(false)
        .storeCnpj("11.222.333/0001-81")
        // .commissionReturn(2) // opcional, dentro da faixa configurada na loja (ver getCommissionReturns)
        .build());

for (SimulationOption opcao : opcoes)
    System.out.printf("%dx de %s%n", opcao.getTerm(), opcao.getInstallmentAmount());
```

Para refinanciamento use `getRefinancingRange` e `createRefinancing(CreateRefinancingSimulationRequest)`.

### 3. Criar a proposta e acompanhar

```java
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.PendingAction;
import twila.credicarro.proposals.model.ProposalDetails;

UUID propostaId = client.proposals().create(CreateProposalRequest.builder()
        .cpf("12345678909")
        .simulationId(opcoes.get(0).getSimulationId())
        .build());

ProposalDetails proposta = client.proposals().get(propostaId);

System.out.println(proposta.getStatus() + " (" + proposta.getStatusName() + ")");

for (PendingAction acao : proposta.getPendingActions())
    System.out.println(acao.getParticipant() + ": " + acao.getKind() + " " + acao.getDocumentTypes());
```

`getPendingActions()` lista só o que depende do integrador (`ActionOwner.INTEGRATOR`): `SEND_DOCUMENTS` (etapa 6, documentos pendentes), `CORRECT_DOCUMENTS` (etapa 7, documentos recusados), `SEND_VEHICLE_DETAILS` (etapa 8) e `SEND_DEPOSIT_ACCOUNT` (etapa 5, refinanciamento).

Todo valor de status, etapa, situação, responsável, biometria e documento vem como enum **mais** o código bruto e o nome (`getStatus()`, `getStatusRaw()`, `getStatusName()`). Valores novos que o SDK ainda não conhece viram `UNKNOWN`, sem perder o valor original.

### 4. Enviar documentos, dados do veículo e conta de recebimento

```java
import java.nio.file.Paths;
import twila.credicarro.proposals.model.DocumentFile;
import twila.credicarro.proposals.model.DocumentType;
import twila.credicarro.proposals.model.ProposalDocumentUpload;
import twila.credicarro.proposals.model.ProposalParticipant;

client.proposals().uploadDocument(propostaId, ProposalDocumentUpload.builder()
        .participant(ProposalParticipant.BUYER)
        .documentType(DocumentType.CNH)
        .file(DocumentFile.fromFile(Paths.get("cnh-comprador.pdf")))
        .build(), chaveDoEnvio);
```

- `DocumentFile` valida antes de enviar: nome sem caminho com até 255 caracteres, extensão `.pdf`, `.jpg`, `.jpeg` ou `.png`, e de 1 byte a 10 MiB. O conteúdo vai em Base64 puro, sem prefixo `data:`. Use `fromBytes`, `fromStream` ou `fromFile`.
- `chaveDoEnvio` é a `Idempotency-Key` (1 a 200 caracteres), **obrigatória** na API. Persista uma por envio: com a mesma chave e o mesmo conteúdo, um envio bem-sucedido nas últimas 24 horas não é processado de novo. Na **correção** de um documento recusado use uma chave **nova**. Sem a chave, o SDK gera uma e a reutiliza em todas as tentativas daquela chamada.
- `sendVehicleDetails(propostaId, VehicleDetails)` (etapa `DADOS_VEICULO`) e `sendDepositAccount(propostaId, DepositAccount)` (refinanciamento, etapa `CONTA_RECEBIMENTO`) substituem o que foi enviado antes, por isso podem ser repetidos com segurança.

## Clientes por recurso

| Cliente | Métodos |
| --- | --- |
| `client.customers()` | `upsert`, `create`, `upsertAddress`, `upsertProfessionalData` |
| `client.simulations()` | `getFinancingRange`, `getRefinancingRange`, `getCommissionReturns`, `createFinancing`, `createRefinancing`, `get`, `list` |
| `client.proposals()` | `create`, `get`, `list`, `sendVehicleDetails`, `sendDepositAccount`, `uploadDocument` |
| `client.webhooks()` | `create`, `list`, `update`, `delete` |
| `client.v1().simulations()` | `getFinancingRange`, `getRefinancingRange`, `createFinancing`, `createRefinancing` (rotas v1, sem CNPJ da loja) |
| `client.v1().proposals()` | `create`, `get` (rotas v1) |

## Idempotência e repetição automática (retry)

O SDK só repete uma requisição quando isso é seguro:

| Requisição | `Idempotency-Key` | Repetida automaticamente? |
| --- | --- | --- |
| `GET` e token | — | Sim |
| `customers.create`, `upsertAddress`, `upsertProfessionalData` | Gerada pelo SDK (uma por chamada, a mesma em todas as tentativas) | Sim |
| `simulations.createFinancing/createRefinancing`, `proposals.create` (v1 e v2) | Gerada pelo SDK | Sim |
| `proposals.uploadDocument` | A informada, ou gerada pelo SDK (sempre enviada) | Sim |
| `customers.upsert(request, idempotencyKey)` | A informada | Sim |
| `customers.upsert(request)` | **Nunca** (mudaria o comportamento da rota) | **Não** |
| `proposals.sendVehicleDetails`, `sendDepositAccount` | — (substituição) | Sim |
| `webhooks.update`, `webhooks.delete` | — | Sim |
| `webhooks.create` | — (a rota não aceita) | **Não** |

São repetidos: `408`, `429` (respeitando `Retry-After`), `500` (padrão; desligue com `retryOn500(false)`), `502`, `503`, `504`, falha de rede e timeout de tentativa — até `maxRetryAttempts` (padrão 3), com backoff exponencial e jitter. Os demais `4xx` nunca são repetidos.

### Resiliência

```java
import java.time.Duration;
import twila.credicarro.config.CredicarroResilienceOptions;

CredicarroClient client = CredicarroClient.builder()
        .clientId("<client-id>")
        .clientSecret("<client-secret>")
        .resilience(CredicarroResilienceOptions.builder()
                .totalTimeout(Duration.ofSeconds(30))
                .attemptTimeout(Duration.ofSeconds(10))
                .documentUploadAttemptTimeout(Duration.ofSeconds(60))
                .maxRetryAttempts(3)
                .retryOn500(true)
                .disableAutomaticIdempotencyKey(false)
                .build())
        .build();
```

Os valores acima são os padrões. Há ainda um circuit breaker (abre com 50% de falhas numa janela de 30 segundos, mínimo de 10 chamadas, por 15 s). O orçamento de tempo (`totalTimeout`, incluindo o token e as esperas entre tentativas) vale para toda a chamada; no envio de documentos ele é `documentUploadAttemptTimeout × maxRetryAttempts` (180 s por padrão). `Retry-After` (segundos ou data HTTP) é respeitado; se for maior que o tempo restante, a chamada falha na hora com `CredicarroRateLimitException`. Redirects (3xx) não são seguidos. `disableAutomaticIdempotencyKey(true)` desliga a chave automática — as rotas afetadas deixam de ser repetidas; o envio de documentos continua sempre com chave, pois ela é obrigatória.

## Paginação

`simulations().list(...)`, `proposals().list(...)` e `webhooks().list(...)` retornam `PagedResult<T>` — sem auto-paginação; você controla o avanço de página. `ListRequest` valida `page >= 1` e `pageSize` de 1 a 100 (padrão: página 1, 10 itens).

```java
PagedResult<ProposalSummary> pagina = client.proposals().list(ListRequest.of(1, 20));

for (ProposalSummary resumo : pagina.getItems())
    System.out.println(resumo.getId() + " " + resumo.getStatus());

if (pagina.isHasNext()) {
    PagedResult<ProposalSummary> proxima = client.proposals().list(ListRequest.of(2, 20));
}
```

## Logs e serialização

`toString` de requisições e respostas é **mascarado** (CPF `***.456.789-**`, demais dados pessoais, bancários, credenciais e segredos como `***`), para que um log acidental não vaze dados. A serialização (Jackson, por exemplo) dos modelos de dados carrega os **valores reais**, pois serve para persistir em fila, cache ou sessão e para responder. Objetos internos que guardam segredo (o cliente, o provedor de token) recusam a serialização.

## Tratamento de erros

| Exceção | Quando |
| --- | --- |
| `CredicarroConfigurationException` | Configuração do `CredicarroClient.Builder` inválida (ex: `clientId`/`clientSecret` ausentes, `baseUrl` sem HTTPS) |
| `CredicarroAuthenticationException` | Credenciais inválidas (`401`/`400` no token) ou `401` persistente após renovar o token |
| `CredicarroValidationException` | `400` — o campo inválido vem em `getField()` |
| `CredicarroBusinessRuleException` | `422` — trate pelo código estável em `getErrorCode()` |
| `CredicarroRateLimitException` | `429`, com `getRetryAfter()` |
| `CredicarroApiException` | Qualquer outro erro de API (`403`, `404`, `409`, `5xx`); base das exceções `400`, `422` e `429` |
| `CredicarroTimeoutException` | Timeout de tentativa ou total, falha de rede, ou circuit breaker aberto |
| `CredicarroWebhookSignatureException` | Assinatura de webhook inválida ou expirada |

```java
try {
    client.proposals().create(request);
} catch (CredicarroBusinessRuleException ex) {
    if ("Proposal.SimulationAlreadyExpired".equals(ex.getErrorCode()))
        refazerSimulacao();
} catch (CredicarroApiException ex) {
    System.out.printf("%d %s%n", ex.getStatusCode(), ex.getErrorCode());
}
```

`getErrorCode()` é o campo `tipo` da resposta (`Modulo.Contexto.Motivo`, ex.: `Store.Integration.Intermediation`). Também há `getTitle()`, `getDetail()`, `getField()`, `getErrors()` (itens de `erros` com `codigo`, `mensagem` e `campo`) e `getRawBody()`. A resposta de falhas inesperadas no formato padrão (`type`/`title`/`detail`) é lida da mesma forma.

A **mensagem** da exceção traz só o status, o `tipo` e o `titulo`. O `detalhe` e o corpo ficam em propriedades porque podem conter dados pessoais — registre-os com cuidado. O `clientSecret` nunca aparece em log, `toString` ou mensagem do SDK.

## Webhooks

Cadastre o webhook uma vez; o `signingSecret` só é devolvido nessa resposta, então guarde-o com segurança.

```java
import twila.credicarro.webhooks.model.CreateWebhookRequest;
import twila.credicarro.webhooks.model.CreateWebhookResult;
import twila.credicarro.webhooks.model.WebhookAuthenticationType;
import twila.credicarro.webhooks.model.WebhookType;

CreateWebhookResult webhook = client.webhooks().create(CreateWebhookRequest.builder()
        .type(WebhookType.PROPOSAL)
        .url("https://parceiro.com.br/webhooks/credicarro")
        .authenticationType(WebhookAuthenticationType.BASIC)
        .credential("dXN1YXJpbzpzZW5oYQ==")
        .build());

String segredo = webhook.getSigningSecret();
```

Ao receber a notificação, valide a assinatura contra o **corpo bruto** (antes de desserializar):

```java
import twila.credicarro.webhooks.CredicarroWebhookEvent;
import twila.credicarro.webhooks.model.ProposalWebhookEvent;

// rawBody = corpo exato da requisição; header = X-CrediCarro-Signature
ProposalWebhookEvent evento = CredicarroWebhookEvent.parse(rawBody, signatureHeader, segredo);

if (evento.isDetailed()) {
    // payload v2 (propostas criadas por POST v2/proposal): retrato completo
    evento.getBuyer().getActiveStep();
}
```

- Assinatura: cabeçalho `t={timestamp},v1={assinatura}`, HMAC-SHA256 hexadecimal de `"{t}.{corpo bruto}"` com o `signingSecret` (texto em UTF-8) como chave, comparado em tempo constante. A tolerância de replay é de 5 minutos nos dois sentidos (`parse(rawBody, header, segredo, Duration)` e `verifySignature(...)` aceitam outra); segredo vazio é inválido.
- `verifySignature(rawBody, header, segredo)` devolve `boolean` em vez de lançar exceção.
- `verifyAuthorization(recebido, esperado)` compara o cabeçalho `Authorization` configurado no cadastro (Basic ou Bearer) em tempo constante.
- `CredicarroWebhookEvent.parseUnverified(rawBody)` lê o evento **sem** verificar a assinatura (só para testes). O corpo precisa trazer um `id_proposta` UUID válido; `t` ou `v1` repetidos no cabeçalho invalidam a assinatura; `parse` exige cabeçalho e segredo.
- Aceita os payloads v1 (`id_proposta`, `enum_status`, `status` em inglês) e v2 (nomes em português, `motivo_evento`, `comprador`, `loja`).
- Responda `2xx` rápido e processe de forma assíncrona; deduplique pelo cabeçalho `Idempotency-Key` da notificação. As notificações podem chegar fora de ordem — consulte a proposta para saber o estado mais recente.

## Samples

- `samples/credicarro-sample-plain` — Java puro, sem framework, `main()` standalone
- `samples/credicarro-sample-spring-boot` — Spring Boot, `CredicarroClient` como `@Bean` singleton

## Qualidade, segurança e cobertura

- **Build** (`ci.yml`) — compila com `--release 8` em `ubuntu-latest` e `windows-latest`.
- **Test** (`ci.yml`) — testes unitários rodando sob JDK 11 e JDK 21 (o WireMock 3 dos testes exige JDK 11+; o SDK em si é compilado com `--release 8`). No JDK 8, o job `test (8, ubuntu-latest)` roda o `scripts/LegacySmoke.java` contra um servidor HTTP do próprio JDK: token, listagem, consulta e erro `422`.
- **Contract tests** (`contract-tests.yml`) — comparam os tipos do SDK com o `openapi.yaml` público da documentação (`https://documentacao.credicarro.com.br/openapi.yaml`; sobrescreva com `CREDICARRO_CONTRACT_TESTS_OPENAPI_URL`). O swagger de staging exige autenticação, por isso não é usado.
- **Quality** (`quality.yml`) — análise estática via Codacy CLI, resultados publicados na aba **Security → Code scanning** do repositório.
- **Security** (`security.yml`) — [CodeQL](https://codeql.github.com/) para Java, rodando a cada PR/push e semanalmente.
- **Coverage** — cobertura de testes coletada via JaCoCo e publicada no [Codecov](https://codecov.io/gh/Twila-Digital/twila-credicarro-java-sdk).

## Documentação completa

[documentacao.credicarro.com.br](https://documentacao.credicarro.com.br) — referência de todos os endpoints, autenticação, etapas e status, webhooks e mais.

## Contribuindo

Veja [CONTRIBUTING.md](CONTRIBUTING.md).

## Código de conduta

Este projeto segue o [Código de Conduta](CODE_OF_CONDUCT.md).

## Licença

[MIT](LICENSE)
