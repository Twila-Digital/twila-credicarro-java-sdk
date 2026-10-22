# Contribuindo

## Pré-requisitos

- JDK 11 ou superior para rodar os testes (o WireMock 3 exige JDK 11+); o SDK compila com `--release 8`
- Maven 3.9+
- (Opcional) Credenciais de staging para rodar os samples manualmente

## Build e testes

```bash
mvn -pl credicarro-sdk -am install
mvn -pl credicarro-sdk test
```

Rode os testes em JDK 11 e 21 antes de abrir o PR. O código de produção usa só a API do Java 8 (`--release 8`).

O runtime do JDK 8 é verificado pelo smoke test sem framework (`scripts/LegacySmoke.java`), o mesmo do job `test (8, ubuntu-latest)`. Empacote com o JDK 11+ e rode com o JDK 8 (`JAVA8` = pasta do JDK 8):

```bash
mvn -pl credicarro-sdk -am package -DskipTests
mvn -q -pl credicarro-sdk dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/legacy-deps
CP="credicarro-sdk/target/classes:credicarro-sdk/target/legacy-deps/*"   # no Windows, separe com ";"
mkdir -p target/legacy-smoke
"$JAVA8/bin/javac" -cp "$CP" -d target/legacy-smoke scripts/LegacySmoke.java
"$JAVA8/bin/java" -cp "target/legacy-smoke:$CP" LegacySmoke
```

Os contract tests (`contract-tests/`) baixam o `openapi.yaml` público da documentação (`https://documentacao.credicarro.com.br/openapi.yaml`, ou a URL em `CREDICARRO_CONTRACT_TESTS_OPENAPI_URL`) e precisam de rede:

```bash
mvn -pl contract-tests -am test
```

Eles verificam que cada wire type de requisição/resposta só usa propriedades presentes no schema da operação, que os campos obrigatórios das requisições existem no wire type e que as rotas com `Idempotency-Key` batem com o que o SDK envia. Veja o workflow `contract-tests.yml`.

## Convenções

- Mensagens de erro e documentação em português do Brasil; código e nomes públicos em inglês (camelCase).
- Erros esperados viram exceções tipadas (`Credicarro*Exception`), com o código estável da API em `getErrorCode()`.
- A mensagem de uma exceção nunca inclui `detalhe`, corpo da resposta, `clientSecret`, token ou dados pessoais.
- Rotas novas entram em `Routes`, com wire type em `internal/generated`, mapeamento em `internal/mapping` e a regra de retry/chave em `IdempotencyClassifier`.
- Não adicione dependências sem necessidade; versões ficam centralizadas no `pom.xml` pai.

## Instalando a partir do código-fonte

Enquanto o pacote não é publicado no Maven Central, instale no repositório Maven local:

```bash
mvn -pl credicarro-sdk -am install -DskipTests
```

E declare a dependência normalmente no seu `pom.xml`/`build.gradle.kts` apontando para a mesma versão (`1.0.0-SNAPSHOT`).

## Abrindo um PR

1. Crie uma branch a partir de `production`
2. Adicione testes para qualquer mudança de comportamento
3. Rode `mvn -pl credicarro-sdk -am test` localmente antes de abrir o PR
4. Abra o PR contra `production` — o CI roda build + testes automaticamente

## Release (publicação no Maven Central)

Maven Central (via [Sonatype Central Portal](https://central.sonatype.com/)) **ainda não oferece Trusted Publishing/OIDC** por execução de workflow — a autenticação de publish usa um Sonatype User Token tradicional, e todo artefato precisa ser assinado com GPG. `release.yml` usa um secret de escopo mínimo guardado no `environment` do GitHub, nunca no repositório inteiro.

Antes do primeiro release, alguém com acesso à conta/organização do Sonatype Central Portal precisa configurar:

1. O namespace `br.com.twila` no [Central Portal](https://central.sonatype.com/) (verificação por registro TXT no DNS).
2. Um **User Token** (Account → Generate User Token) com permissão apenas de publish.
3. Uma chave GPG usada só para assinar releases deste SDK, publicada num keyserver (`keys.openpgp.org` ou `keyserver.ubuntu.com`).
4. No repositório do GitHub, o [environment](https://docs.github.com/actions/deployment/targeting-different-environments/using-environments-for-deployment) `production` (Settings → Environments) com **Required reviewers** configurado, e os secrets:
   - `CENTRAL_TOKEN_USERNAME` / `CENTRAL_TOKEN_PASSWORD` — o User Token do Central Portal.
   - `GPG_PRIVATE_KEY` — a chave privada exportada em ASCII-armor (`gpg --export-secret-keys --armor`).
   - `GPG_PASSPHRASE` — a senha da chave.

Com isso configurado, `git push --tags` numa tag `v*` (ex.: `v1.0.0`) dispara build → contract tests → **pausa esperando aprovação manual do `environment` `production`** → assinatura → publicação automática no Central Portal (`autoPublish: true`) → GitHub Release. O gate de segurança fica na aprovação do `environment`, não na publicação em si.

## Reportando problemas

Abra uma [issue](https://github.com/Twila-Digital/twila-credicarro-java-sdk/issues) com passos para reproduzir, versão do pacote/JDK e o comportamento esperado vs. observado. Nunca inclua `clientId`/`clientSecret` reais, tokens ou dados pessoais no relato.
