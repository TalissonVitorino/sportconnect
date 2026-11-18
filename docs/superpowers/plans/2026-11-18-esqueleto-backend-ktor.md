# Esqueleto do backend Ktor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Criar o módulo `:server` (backend Ktor real do SportConnect) com o esqueleto estrutural — módulo Gradle, camadas (routing/config/persistência/auth), envelope de erro, e uma fatia vertical completa (OTP → JWT → `/me`) rodando contra Postgres de verdade — mais a prova concreta de que a máquina de estados pura já escrita em `composeApp` porta sem alteração para o servidor.

**Architecture:** Módulo Gradle novo e independente (`:server`), Kotlin/JVM puro (não KMP) — não depende de `:composeApp` como dependência Gradle; em vez disso, os arquivos de domínio puro (`core/model`, `core/domain/ReservationStateMachine` e afins) são **copiados verbatim** para dentro de `:server`, mesmo pacote `br.com.sportconnect.core.*`, exatamente como o [14 §1](../../14-arquitetura-backend.md#1-como-ler-isto) pede ("portados quase literalmente, não redesenhados"). Isso mantém o ciclo de deploy do servidor desacoplado do app mobile. Camadas dentro de `:server`: `routes` (Ktor routing) → `auth`/`persistence` (serviços) → Exposed sobre Postgres. DI com Koin, igual ao padrão já usado em `composeApp/di/CoreModules.kt`.

**Tech Stack:** Kotlin 2.4.21 / JVM 21, Ktor 3.6.1 (servidor, engine Netty), Koin 4.2.2 (`koin-ktor`), kotlinx.serialization 1.13.0, Exposed (SQL, versão a confirmar — ver nota abaixo), HikariCP, PostgreSQL JDBC, Flyway (migrações), `com.auth0:java-jwt` (emissão/verificação de JWT), Testcontainers (Postgres, só em testes), Logback (SLF4J). `kotlin.test` para os testes (mesma convenção do resto do projeto, não JUnit5 direto).

> **Nota sobre versões de biblioteca:** as versões abaixo (Exposed, HikariCP, driver Postgres, Flyway, java-jwt, Testcontainers, Logback) são a melhor estimativa no momento deste plano — não há acesso à internet para confirmar a última versão estável de cada uma. Antes de fechar a Task 1, confirme no Maven Central se há uma versão mais nova e ajuste o catálogo; o importante é que o valor fique **fixo e explícito** no catálogo, não "a definir".

**Spec:** [14 — Arquitetura técnica do backend](../../14-arquitetura-backend.md), [15 — Contrato de API](../../15-contrato-api.md) §1 (Autenticação), §2 (`GET /me`), §12 (Catálogo de erros), [16 — Modelo de dados](../../16-modelo-dados.md) §1 (convenções), §2 (`usuarios`, `refresh_tokens`), [17 — ADR gateway de pagamento](../../17-adr-gateway-pagamento.md) (contexto, não implementado neste plano).

## Global Constraints

- Toda resposta de erro usa o envelope `{"erro":{"codigo":"...","mensagem":"..."}}`, com `codigo` sendo o texto **literal** já usado pelo cliente (`RegraNegocioException`/`AuthFailure`) — nunca inventar um novo código no servidor ([14 §5](../../14-arquitetura-backend.md#5-envelope-de-erro-e-catálogo-de-códigos)).
- Todo campo JSON usa o mesmo nome/`camelCase` da propriedade Kotlin — zero `@SerialName`, zero mapeador ([14 §3](../../14-arquitetura-backend.md#3-convenção-de-nomes-português-igual-ao-kotlin--de-propósito)).
- Dinheiro é sempre `Long` em centavos — nunca `Double`/`Float`/`BigDecimal` (RN-01).
- Datas/horas são sempre UTC ISO-8601 (`kotlin.time.Instant`); a API nunca envia ou recebe fuso horário ([14 §14](../../14-arquitetura-backend.md#14-plataformas-cliente--o-que-a-api-precisa-respeitar)).
- JWT de acesso: TTL de **15 minutos**. Refresh token: rotativo (o antigo é invalidado ao emitir um novo) ([14 §4](../../14-arquitetura-backend.md#4-autenticação-e-sessão)).
- Regras de OTP são as já fixadas em `AuthRules` (`core/domain/repository/AuthRepository.kt`), **não** valores livres do backend: validade **10 min**, **5** tentativas, bloqueio de **15 min**.
- `entrarComo(usuarioId)` (troca de perfil "demo" do fake) **não existe** no contrato real — não portar esse atalho para `:server`.
- Controle de concorrência otimista via `Jogo.version` ([14 §7](../../14-arquitetura-backend.md#7-concorrência-de-mutex-para-controle-otimista-real)) está **fora do escopo deste plano** — nenhuma task aqui muta `Jogo`/`Reserva`; fica para o plano que implementar `POST /jogos` e as ações de pagamento.
- Persistência real é **PostgreSQL**, não H2/SQLite — o schema do [16 §1](../../16-modelo-dados.md#1-convenções-do-schema) depende de `btree_gist` e de `ENUM` nativo do Postgres. Testes de integração usam Testcontainers e exigem Docker rodando localmente/no CI.
- O módulo `:server` não depende de `:composeApp`; arquivos de domínio puro são copiados, não importados via Gradle.

## Review Focus

- **Celular que já tem usuário cadastrado chamando `/auth/otp/confirmar`** deve devolver o usuário **existente**, nunca criar um segundo registro para o mesmo `celular` (o [15 §1](../../15-contrato-api.md#1-autenticação) só descreve o caminho "cria convidado" quando não existe nenhum; o caminho "já existe" é implícito e fácil de esquecer). Teste na Task 5.
- **Bloqueio exatamente na 5ª tentativa errada** dentro da janela de 10 min (não na 4ª, não na 6ª), e liberação só depois de 15 min — `AuthRules.MAX_TENTATIVAS`/`BLOQUEIO_MIN` são requisito de produto, um off-by-one aqui é o tipo de bug que passa despercebido em teste "feliz". Teste na Task 5.
- **Código OTP é de uso único**: confirmar o mesmo código uma segunda vez (depois de já ter sido aceito) deve falhar com `CODE_INVALID`, não reemitir tokens silenciosamente para quem capturou um código antigo. Teste na Task 5.
- **Token ausente, expirado ou malformado** em `GET /me` devem cair, todos os três, em `401 NAO_AUTENTICADO` no mesmo envelope — nunca um 500 com stacktrace vazando (um JWT malformado faz bibliotecas ingênuas lançarem exceção não tratada). Teste na Task 6.
- **Corpo malformado** (`celular` fora do formato E.164, JSON com campo faltante) em `POST /auth/otp` deve virar `400 PHONE_INVALID`/`VALIDATION_ERROR` no envelope — nunca o 500 genérico que o Ktor devolve por padrão para uma exceção de desserialização não tratada. Teste na Task 2 (mecanismo genérico) e na Task 5 (caso concreto).

---

### Task 1: Módulo Gradle `:server` + verificação de saúde

**Files:**
- Create: `server/build.gradle.kts`
- Modify: `settings.gradle.kts` (adicionar `include(":server")`)
- Modify: `gradle/libs.versions.toml` (adicionar `ktor-server-*`, `koin-ktor`, `logback`)
- Create: `server/src/main/kotlin/br/com/sportconnect/server/Application.kt`
- Create: `server/src/main/resources/logback.xml`
- Test: `server/src/test/kotlin/br/com/sportconnect/server/SaudeRouteTest.kt`

**Interfaces:**
- Produces: `fun Application.module()` (instalado em `main()` e reusado pelos testes via `testApplication { application { module() } }`); rota `GET /saude` → `200` com corpo `{"status":"ok"}`.

- [ ] **Step 1: Escrever o teste de saúde**

```kotlin
class SaudeRouteTest {
    @Test
    fun saude_responde_200_ok() = testApplication {
        application { module() }
        val resposta = client.get("/saude")
        assertEquals(HttpStatusCode.OK, resposta.status)
        assertEquals("""{"status":"ok"}""", resposta.bodyAsText())
    }
}
```

- [ ] **Step 2: Rodar o teste para confirmar que falha**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.SaudeRouteTest" 2>&1 | tail -50`
Expected: FAIL — módulo `:server` ainda não existe (erro de configuração do Gradle).

- [ ] **Step 3: Criar `server/build.gradle.kts`**

Plugins: `org.jetbrains.kotlin.jvm`, `org.jetbrains.kotlin.plugin.serialization`, `application`. `mainClass` apontando para `br.com.sportconnect.server.ApplicationKt`. `jvmToolchain(21)`. Dependências: `ktor-server-core`, `ktor-server-netty`, `ktor-server-content-negotiation`, `ktor-serialization-kotlinx-json` (já no catálogo, versão `ktor`), `logback-classic` em runtime. Registrar em `settings.gradle.kts`: `include(":server")`.

- [ ] **Step 4: Implementar `fun Application.module()` e `fun main()` em `Application.kt`**

```kotlin
fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }
    routing {
        get("/saude") { call.respondText("""{"status":"ok"}""", ContentType.Application.Json) }
    }
}
```

- [ ] **Step 5: Rodar o teste para confirmar que passa**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.SaudeRouteTest" 2>&1 | tail -50`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add server settings.gradle.kts gradle/libs.versions.toml
git commit -m "feat(server): esqueleto do modulo Ktor com rota de saude"
```

---

### Task 2: Envelope de erro e catálogo (`StatusPages`)

**Files:**
- Create: `server/src/main/kotlin/br/com/sportconnect/core/domain/usecase/Errors.kt` (cópia verbatim de `composeApp/src/commonMain/kotlin/br/com/sportconnect/core/domain/usecase/Errors.kt`)
- Create: `server/src/main/kotlin/br/com/sportconnect/server/http/ErroEnvelope.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/http/CatalogoErros.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/http/TratamentoDeErros.kt`
- Modify: `server/src/main/kotlin/br/com/sportconnect/server/Application.kt` (chamar `configurarTratamentoDeErros()` dentro de `module()`)
- Test: `server/src/test/kotlin/br/com/sportconnect/server/http/TratamentoDeErrosTest.kt`

**Interfaces:**
- Consumes: nada de tasks anteriores além do `module()` da Task 1.
- Produces: `@Serializable data class ErroEnvelope(val erro: ErroCorpo)` / `data class ErroCorpo(val codigo: String, val mensagem: String)`; `object CatalogoErros { fun statusPara(codigo: String): HttpStatusCode }` (mapa aberto, um `when`/`Map` indexado por `codigo`, com as entradas da tabela do [15 §12](../../15-contrato-api.md#12-catálogo-de-erros) relevantes às Tasks 5–6: `NAO_AUTENTICADO`→401, `PHONE_INVALID`→400, `CODE_INVALID`→400, `CODE_EXPIRED`→410, `TOO_MANY_ATTEMPTS`→429, `PROVIDER_TOKEN_INVALID`→401, `VALIDATION_ERROR`→422; default para código desconhecido → 400); `fun Application.configurarTratamentoDeErros()` instala `StatusPages` capturando `RegraNegocioException` (→ envelope com `CatalogoErros.statusPara(codigo)`) e `Throwable` genérico (→ 500, envelope com `codigo = "ERRO_INTERNO"`, mensagem genérica, sem vazar detalhe).

- [ ] **Step 1: Escrever o teste**

```kotlin
class TratamentoDeErrosTest {
    @Test
    fun regra_negocio_vira_envelope_com_status_do_catalogo() = testApplication {
        application {
            configurarTratamentoDeErros()
            routing { get("/_teste-regra") { throw RegraNegocioException("CODE_INVALID", "Código inválido.") } }
        }
        val resposta = client.get("/_teste-regra")
        assertEquals(HttpStatusCode.BadRequest, resposta.status)
        assertEquals("""{"erro":{"codigo":"CODE_INVALID","mensagem":"Código inválido."}}""", resposta.bodyAsText())
    }

    @Test
    fun exception_generica_vira_500_sem_vazar_detalhe() = testApplication {
        application {
            configurarTratamentoDeErros()
            routing { get("/_teste-boom") { error("detalhe interno sensível") } }
        }
        val resposta = client.get("/_teste-boom")
        assertEquals(HttpStatusCode.InternalServerError, resposta.status)
        assertEquals("""{"erro":{"codigo":"ERRO_INTERNO","mensagem":"Erro interno. Tente novamente."}}""", resposta.bodyAsText())
    }
}
```

- [ ] **Step 2: Rodar os testes para confirmar que falham**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.http.TratamentoDeErrosTest" 2>&1 | tail -60`
Expected: FAIL — `configurarTratamentoDeErros`/`RegraNegocioException` não existem ainda no módulo.

- [ ] **Step 3: Copiar `Errors.kt` verbatim e implementar `ErroEnvelope`/`CatalogoErros`/`configurarTratamentoDeErros`**

`RegraNegocioException`/`regra(...)` copiados **sem alteração** (mesmo pacote `br.com.sportconnect.core.domain.usecase`). `CatalogoErros.statusPara` é a única parte com lógica: um `when (codigo)` fechado nas 7 entradas acima, `else -> HttpStatusCode.BadRequest`.

- [ ] **Step 4: Rodar os testes para confirmar que passam**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.http.TratamentoDeErrosTest" 2>&1 | tail -60`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add server/src/main/kotlin/br/com/sportconnect/core server/src/main/kotlin/br/com/sportconnect/server/http server/src/test/kotlin/br/com/sportconnect/server/http server/src/main/kotlin/br/com/sportconnect/server/Application.kt
git commit -m "feat(server): envelope de erro e catalogo de codigos via StatusPages"
```

---

### Task 3: Configuração (`AppConfig`) e DI com Koin

**Files:**
- Create: `server/src/main/kotlin/br/com/sportconnect/server/config/AppConfig.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/di/ServerModule.kt`
- Modify: `server/build.gradle.kts` (adicionar `koin-ktor`, versão `koin` já no catálogo)
- Modify: `server/src/main/kotlin/br/com/sportconnect/server/Application.kt` (ler `AppConfig.fromEnv(System.getenv())`, instalar Koin com `serverModule(config)`)
- Test: `server/src/test/kotlin/br/com/sportconnect/server/config/AppConfigTest.kt`

**Interfaces:**
- Consumes: nada novo.
- Produces: `data class AppConfig(val porta: Int, val jwtSecret: String, val jwtIssuer: String, val acessoTtlMinutos: Int, val refreshTtlDias: Int, val databaseUrl: String, val databaseUsuario: String, val databaseSenha: String)` com `companion object { fun fromEnv(env: Map<String, String>): AppConfig }` (defaults de desenvolvimento quando a variável não existe: `porta=8080`, `jwtIssuer="sportconnect"`, `acessoTtlMinutos=15`, `refreshTtlDias=30`, `databaseUrl="jdbc:postgresql://localhost:5432/sportconnect"`, `databaseUsuario="sportconnect"`, `databaseSenha="sportconnect"`; `jwtSecret` sem default — ausente lança `IllegalStateException` explícito, nunca uma string fixa em código). `fun serverModule(config: AppConfig): org.koin.core.module.Module` (Koin), expondo `AppConfig` como `single`.

- [ ] **Step 1: Escrever o teste**

```kotlin
class AppConfigTest {
    @Test
    fun usa_defaults_quando_env_esta_vazio() {
        val config = AppConfig.fromEnv(mapOf("JWT_SECRET" to "segredo-de-teste"))
        assertEquals(8080, config.porta)
        assertEquals(15, config.acessoTtlMinutos)
    }

    @Test
    fun le_overrides_do_env() {
        val config = AppConfig.fromEnv(mapOf("JWT_SECRET" to "x", "PORTA" to "9090"))
        assertEquals(9090, config.porta)
    }

    @Test
    fun exige_jwt_secret_explicito() {
        assertFailsWith<IllegalStateException> { AppConfig.fromEnv(emptyMap()) }
    }
}
```

- [ ] **Step 2: Rodar os testes para confirmar que falham**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.config.AppConfigTest" 2>&1 | tail -40`
Expected: FAIL — `AppConfig` não existe.

- [ ] **Step 3: Implementar `AppConfig.fromEnv` e `serverModule`**

- [ ] **Step 4: Rodar os testes para confirmar que passam**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.config.AppConfigTest" 2>&1 | tail -40`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add server/build.gradle.kts server/src/main/kotlin/br/com/sportconnect/server/config server/src/main/kotlin/br/com/sportconnect/server/di server/src/main/kotlin/br/com/sportconnect/server/Application.kt server/src/test/kotlin/br/com/sportconnect/server/config
git commit -m "feat(server): AppConfig a partir do ambiente e wiring de Koin"
```

---

### Task 4: Persistência — Postgres, Flyway, Exposed, tabela `usuarios`

**Files:**
- Modify: `server/build.gradle.kts` (adicionar Exposed, HikariCP, driver Postgres, Flyway, Testcontainers-postgresql em `testImplementation`)
- Modify: `gradle/libs.versions.toml` (versões: `exposed = "0.56.0"`, `hikaricp = "5.1.0"`, `postgresql-driver = "42.7.4"`, `flyway = "10.20.1"`, `testcontainers = "1.20.4"` — confirmar no Maven Central ao implementar, ver nota do cabeçalho)
- Create: `server/src/main/resources/db/migration/V1__usuarios_e_refresh_tokens.sql`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/persistence/Database.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/persistence/UsuariosTable.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/persistence/RefreshTokensTable.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/persistence/UsuarioRepository.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/persistence/ExposedUsuarioRepository.kt`
- Test: `server/src/test/kotlin/br/com/sportconnect/server/persistence/PostgresTestContainer.kt` (helper, container singleton)
- Test: `server/src/test/kotlin/br/com/sportconnect/server/persistence/ExposedUsuarioRepositoryTest.kt`

**Interfaces:**
- Consumes: `AppConfig` (Task 3) para montar a `databaseUrl` apontando para o container de teste.
- Produces: `fun conectar(config: AppConfig): Database` (Exposed, via HikariCP) e `fun migrar(config: AppConfig)` (roda Flyway); `interface UsuarioRepository { suspend fun buscarPorCelular(celular: String): Usuario?; suspend fun buscarPorId(id: String): Usuario?; suspend fun criar(celular: String, nome: String): Usuario }`; `class ExposedUsuarioRepository(private val db: Database) : UsuarioRepository`. `Usuario` aqui ainda **não existe no módulo** — para esta task, use só os três campos que a tabela `usuarios` exige neste ponto (`id`, `celular`, `nome`, `criadoEm`) como um `data class UsuarioResumo(val id: String, val celular: String, val nome: String, val criadoEm: Instant)` local em `persistence/`; a Task 5 troca `UsuarioResumo` pelo `Usuario` completo portado de `core/model`.

- [ ] **Step 1: Escrever o helper de container e o teste**

```kotlin
object PostgresTestContainer {
    val instancia: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer("postgres:16-alpine").apply { start() }
    }
}

class ExposedUsuarioRepositoryTest {
    private val config = AppConfig.fromEnv(mapOf(
        "JWT_SECRET" to "x",
        "DATABASE_URL" to PostgresTestContainer.instancia.jdbcUrl,
        "DATABASE_USUARIO" to PostgresTestContainer.instancia.username,
        "DATABASE_SENHA" to PostgresTestContainer.instancia.password,
    )).also { migrar(it) }
    private val repo = ExposedUsuarioRepository(conectar(config))

    @Test
    fun cria_e_busca_por_celular() = runBlocking {
        val criado = repo.criar(celular = "+5511988887777", nome = "Convidado")
        val encontrado = repo.buscarPorCelular("+5511988887777")
        assertEquals(criado.id, encontrado?.id)
    }

    @Test
    fun busca_por_celular_inexistente_devolve_null() = runBlocking {
        assertNull(repo.buscarPorCelular("+5511900000000"))
    }
}
```

- [ ] **Step 2: Rodar o teste para confirmar que falha**

Run: `docker info >/dev/null 2>&1 && ./gradlew :server:test --tests "br.com.sportconnect.server.persistence.ExposedUsuarioRepositoryTest" 2>&1 | tail -60 || echo "Docker precisa estar rodando para este teste"`
Expected: FAIL (classe não existe) — se Docker não estiver rodando, suba-o antes de continuar.

- [ ] **Step 3: Migração SQL (`V1__usuarios_e_refresh_tokens.sql`)**

Colunas exatamente como o [16 §2](../../16-modelo-dados.md#2-identidade-e-acesso) descreve para `usuarios` e `refresh_tokens` (incluindo `CREATE EXTENSION IF NOT EXISTS btree_gist;` no topo, já que será necessária por migrações futuras e é mais simples fixar agora).

- [ ] **Step 4: Implementar `Database.kt`, `UsuariosTable`, `RefreshTokensTable`, `UsuarioRepository`, `ExposedUsuarioRepository`**

- [ ] **Step 5: Rodar o teste para confirmar que passa**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.persistence.ExposedUsuarioRepositoryTest" 2>&1 | tail -60`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add server/build.gradle.kts gradle/libs.versions.toml server/src/main/resources/db server/src/main/kotlin/br/com/sportconnect/server/persistence server/src/test/kotlin/br/com/sportconnect/server/persistence
git commit -m "feat(server): persistencia Postgres/Exposed/Flyway para usuarios e refresh_tokens"
```

---

### Task 5: Autenticação — OTP + JWT (`POST /auth/otp`, `POST /auth/otp/confirmar`)

**Files:**
- Create: `server/src/main/kotlin/br/com/sportconnect/core/model/Usuario.kt` (cópia verbatim de `composeApp/.../core/model/Usuario.kt`)
- Create: `server/src/main/kotlin/br/com/sportconnect/core/domain/repository/AuthRepository.kt` (cópia verbatim — só interessam `AuthRules`/`AuthFailure`; a interface `AuthRepository` em si fica sem uso no servidor e não é instanciada, mas o arquivo é copiado como está, sem edição)
- Modify: `server/src/main/kotlin/br/com/sportconnect/server/persistence/UsuarioRepository.kt` (troca `UsuarioResumo` por `Usuario` do `core/model` — ver nota abaixo)
- Create: `server/src/main/kotlin/br/com/sportconnect/server/auth/OtpStore.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/auth/InMemoryOtpStore.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/auth/TokenService.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/auth/AuthRoutes.kt`
- Modify: `server/src/main/kotlin/br/com/sportconnect/server/Application.kt` (registrar `authRoutes()` dentro de `routing { }`)
- Test: `server/src/test/kotlin/br/com/sportconnect/server/auth/InMemoryOtpStoreTest.kt`
- Test: `server/src/test/kotlin/br/com/sportconnect/server/auth/AuthRoutesTest.kt`

**Nota sobre `UsuarioRepository` (ajuste retroativo da Task 4):** nesta task o `data class UsuarioResumo` criado na Task 4 é substituído pelo `Usuario` completo (campo a campo idêntico — `UsuarioResumo` só existia porque `Usuario` ainda não tinha sido portado). `ExposedUsuarioRepository` passa a mapear todas as colunas já existentes da tabela (`email`, `cpf`, `papeis` ficam `null`/`emptyList`/default por enquanto — só `id`, `celular`, `nome`, `criadoEm` são gravados nesta task).

**Interfaces:**
- Consumes: `UsuarioRepository` (Task 4), `AppConfig` (Task 3), `RegraNegocioException`/`CatalogoErros` (Task 2).
- Produces: `sealed interface OtpResultado { data object Ok; data object Invalido; data object Expirado; data class Bloqueado(val ate: Instant) }`; `interface OtpStore { fun gerar(celular: String): String; fun validar(celular: String, codigo: String): OtpResultado }`; `class TokenService(private val config: AppConfig) { fun emitirAcesso(usuarioId: String): String; fun emitirRefresh(usuarioId: String): ParDeTokens }` onde `data class ParDeTokens(val acesso: String, val renovacao: String, val expiraEm: Instant)`; `fun Route.authRoutes(usuarios: UsuarioRepository, otp: OtpStore, tokens: TokenService)` registrando `POST /auth/otp` e `POST /auth/otp/confirmar` exatamente como o [15 §1](../../15-contrato-api.md#1-autenticação) descreve (request/response/erros).

- [ ] **Step 1: Escrever os testes de `InMemoryOtpStore` (cobre Review Focus #2 e #3)**

```kotlin
class InMemoryOtpStoreTest {
    @Test
    fun codigo_gerado_valida_uma_unica_vez() {
        val store = InMemoryOtpStore()
        val codigo = store.gerar("+5511988887777")
        assertEquals(OtpResultado.Ok, store.validar("+5511988887777", codigo))
        assertEquals(OtpResultado.Invalido, store.validar("+5511988887777", codigo)) // uso unico
    }

    @Test
    fun bloqueia_exatamente_na_quinta_tentativa_errada() {
        val store = InMemoryOtpStore()
        store.gerar("+5511988887777")
        repeat(4) { assertEquals(OtpResultado.Invalido, store.validar("+5511988887777", "000000")) }
        val quinta = store.validar("+5511988887777", "000000")
        assertIs<OtpResultado.Bloqueado>(quinta)
    }
}
```

- [ ] **Step 2: Rodar para confirmar que falham**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.auth.InMemoryOtpStoreTest" 2>&1 | tail -40`
Expected: FAIL

- [ ] **Step 3: Implementar `OtpStore`/`InMemoryOtpStore`**

`InMemoryOtpStore` usa `ConcurrentHashMap<String, EntradaOtp>` (código, tentativas, geradoEm, bloqueadoAte). Validade/tentativas/bloqueio vêm de `AuthRules.OTP_VALIDADE_MIN`/`MAX_TENTATIVAS`/`BLOQUEIO_MIN` (Task 5, arquivo copiado no Step acima) — **não** reimplementar esses números. Nota para quem for ligar ao Redis depois: esta implementação é só para este esqueleto, a interface já isola a troca.

- [ ] **Step 4: Rodar para confirmar que passam**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.auth.InMemoryOtpStoreTest" 2>&1 | tail -40`
Expected: PASS

- [ ] **Step 5: Escrever os testes de `AuthRoutes` (cobre Review Focus #1 e #5)**

```kotlin
class AuthRoutesTest {
    // ambiente de teste monta o mesmo module() de produção contra o PostgresTestContainer (Task 4) e um OtpStore real

    @Test
    fun confirmar_cria_usuario_convidado_na_primeira_vez() = testApplication { /* ... */
        client.post("/auth/otp") { setBody("""{"celular":"+5511988887777","canal":"SMS"}""") }
        val codigo = otpStoreDeTeste.ultimoCodigoPara("+5511988887777")
        val resposta = client.post("/auth/otp/confirmar") { setBody("""{"celular":"+5511988887777","codigo":"$codigo"}""") }
        assertEquals(HttpStatusCode.OK, resposta.status)
    }

    @Test
    fun confirmar_duas_vezes_reusa_o_mesmo_usuario_sem_duplicar() = testApplication { /* chama o fluxo completo duas vezes com o mesmo celular, compara usuario.id */ }

    @Test
    fun celular_fora_do_formato_e164_vira_phone_invalid_400() = testApplication {
        val resposta = client.post("/auth/otp") { setBody("""{"celular":"11988887777","canal":"SMS"}""") }
        assertEquals(HttpStatusCode.BadRequest, resposta.status)
        assertTrue(resposta.bodyAsText().contains("PHONE_INVALID"))
    }
}
```

(O helper `otpStoreDeTeste.ultimoCodigoPara(...)` é uma pequena extensão de teste exclusiva do `InMemoryOtpStore` para ler o código sem precisar de um "canal" de SMS real — este esqueleto não integra provedor de SMS/WhatsApp de verdade, só gera e guarda o código; a integração com o provedor real é item de um plano futuro, não deste.)

- [ ] **Step 6: Rodar para confirmar que falham**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.auth.AuthRoutesTest" 2>&1 | tail -60`
Expected: FAIL

- [ ] **Step 7: Implementar `TokenService` e `authRoutes`**

`TokenService.emitirAcesso` usa `com.auth0.jwt.JWT.create().withIssuer(config.jwtIssuer).withClaim("usuarioId", usuarioId).withExpiresAt(...).sign(Algorithm.HMAC256(config.jwtSecret))`. `authRoutes`: `POST /auth/otp` valida formato E.164 (regex simples `^\+\d{10,15}$`) antes de gerar o OTP — fora do formato lança `RegraNegocioException("PHONE_INVALID", ...)`; `POST /auth/otp/confirmar` chama `otp.validar`, mapeia `OtpResultado` para `204`/erros do catálogo, busca-ou-cria o usuário via `UsuarioRepository`, emite o par de tokens, responde `{ "usuario": ..., "tokens": {...} }` igual ao [15 §1](../../15-contrato-api.md#1-autenticação).

- [ ] **Step 8: Rodar para confirmar que passam**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.auth.AuthRoutesTest" 2>&1 | tail -60`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add server/src/main/kotlin/br/com/sportconnect/core server/src/main/kotlin/br/com/sportconnect/server/auth server/src/main/kotlin/br/com/sportconnect/server/persistence server/src/main/kotlin/br/com/sportconnect/server/Application.kt server/src/test/kotlin/br/com/sportconnect/server/auth
git commit -m "feat(server): fluxo de OTP e emissao de JWT (auth/otp, auth/otp/confirmar)"
```

---

### Task 6: Rota protegida `GET /me`

**Files:**
- Modify: `server/build.gradle.kts` (adicionar `ktor-server-auth`, `ktor-server-auth-jwt`)
- Create: `server/src/main/kotlin/br/com/sportconnect/server/auth/JwtAuth.kt`
- Create: `server/src/main/kotlin/br/com/sportconnect/server/routes/MeRoutes.kt`
- Modify: `server/src/main/kotlin/br/com/sportconnect/server/Application.kt` (instalar `Authentication` com `jwtAuth(config)`; registrar `meRoutes(usuarios)`)
- Test: `server/src/test/kotlin/br/com/sportconnect/server/routes/MeRoutesTest.kt`

**Interfaces:**
- Consumes: `AppConfig` (Task 3) — `jwtAuth` só precisa de `jwtSecret`/`jwtIssuer`, o mesmo par que `TokenService` (Task 5) usa para assinar, nunca uma instância de `TokenService`; `UsuarioRepository` (Task 4) para `meRoutes`.
- Produces: `fun Authentication.Configuration.jwtAuth(config: AppConfig)` (provider nomeado `"jwt"`, valida emissor/assinatura/expiração, extrai `usuarioId` do claim; `challenge` customizado responde com o mesmo `ErroEnvelope`/`NAO_AUTENTICADO` em vez do corpo padrão do Ktor); `fun Route.meRoutes(usuarios: UsuarioRepository)` registrando `GET /me` dentro de `authenticate("jwt") { }`.

- [ ] **Step 1: Escrever os testes (cobre Review Focus #4)**

```kotlin
class MeRoutesTest {
    @Test
    fun me_com_token_valido_devolve_o_usuario() = testApplication { /* confirma OTP, usa o acesso recebido */
        val acesso = tokensDoFluxoDeAuth()
        val resposta = client.get("/me") { header(HttpHeaders.Authorization, "Bearer $acesso") }
        assertEquals(HttpStatusCode.OK, resposta.status)
    }

    @Test
    fun me_sem_token_devolve_401_nao_autenticado() = testApplication {
        val resposta = client.get("/me")
        assertEquals(HttpStatusCode.Unauthorized, resposta.status)
        assertTrue(resposta.bodyAsText().contains("NAO_AUTENTICADO"))
    }

    @Test
    fun me_com_token_malformado_devolve_401_nao_autenticado() = testApplication {
        val resposta = client.get("/me") { header(HttpHeaders.Authorization, "Bearer isto-nao-e-um-jwt") }
        assertEquals(HttpStatusCode.Unauthorized, resposta.status)
        assertTrue(resposta.bodyAsText().contains("NAO_AUTENTICADO"))
    }

    @Test
    fun me_com_token_expirado_devolve_401_nao_autenticado() = testApplication { /* TokenService com TTL negativo/instante no passado para gerar um token já expirado */ }
}
```

- [ ] **Step 2: Rodar para confirmar que falham**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.routes.MeRoutesTest" 2>&1 | tail -60`
Expected: FAIL

- [ ] **Step 3: Implementar `jwtAuth` e `meRoutes`**

- [ ] **Step 4: Rodar para confirmar que passam**

Run: `./gradlew :server:test --tests "br.com.sportconnect.server.routes.MeRoutesTest" 2>&1 | tail -60`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add server/build.gradle.kts server/src/main/kotlin/br/com/sportconnect/server/auth/JwtAuth.kt server/src/main/kotlin/br/com/sportconnect/server/routes server/src/main/kotlin/br/com/sportconnect/server/Application.kt server/src/test/kotlin/br/com/sportconnect/server/routes
git commit -m "feat(server): autenticacao JWT e rota protegida GET /me"
```

---

### Task 7: Porte da máquina de estados pura (prova de reaproveitamento)

> Esta task **não adiciona nenhuma rota nova** — o objetivo é só provar, com os mesmos testes que já passam em `composeApp`, que `ReservationStateMachine` e o modelo de domínio ao redor dela portam para `:server` sem alteração de lógica, conforme o [14 §1](../../14-arquitetura-backend.md#1-como-ler-isto) exige. Ligar isso a um endpoint real (`POST /jogos` etc.) é o próximo plano.

**Files:**
- Create: `server/src/main/kotlin/br/com/sportconnect/core/model/{Arena,Cota,Jogo,Money,Pagamento,Reserva,Slot,Social,Sport}.kt` (cópia verbatim de `composeApp/src/commonMain/kotlin/br/com/sportconnect/core/model/`; **não copiar** `PtBrFormat.kt`/`TimeZones.kt` — são formatação pt-BR client-only, sem uso no servidor)
- Create: `server/src/main/kotlin/br/com/sportconnect/core/domain/{ReservationStateMachine,HoldDeadlineCalculator,RefundPolicy,SplitCostUseCase}.kt` (cópia verbatim de `composeApp/src/commonMain/kotlin/br/com/sportconnect/core/domain/`)
- Create: `server/src/test/kotlin/br/com/sportconnect/core/Fixtures.kt` (cópia verbatim de `composeApp/src/commonTest/kotlin/br/com/sportconnect/core/Fixtures.kt`)
- Create: `server/src/test/kotlin/br/com/sportconnect/core/ReservationStateMachineTest.kt` (cópia verbatim de `composeApp/src/commonTest/kotlin/br/com/sportconnect/core/ReservationStateMachineTest.kt`)
- Modify: `server/build.gradle.kts` (adicionar `kotlinx-datetime`, já no catálogo do projeto — os arquivos de `core/model` dependem dele)

**Interfaces:**
- Consumes: nada de `:server` (este código é autocontido, zero I/O) — só precisa compilar isolado dentro do módulo.
- Produces: os mesmos tipos que `composeApp` já expõe sob `br.com.sportconnect.core.model.*`/`br.com.sportconnect.core.domain.ReservationStateMachine` — disponíveis para o próximo plano (`POST /jogos` e as ações de pagamento) usar sem reescrita.

- [ ] **Step 1: Copiar os 9 arquivos de `core/model` listados acima, byte a byte**

Run: `for f in Arena Cota Jogo Money Pagamento Reserva Slot Social Sport; do cp composeApp/src/commonMain/kotlin/br/com/sportconnect/core/model/$f.kt server/src/main/kotlin/br/com/sportconnect/core/model/$f.kt; done`

- [ ] **Step 2: Copiar os 4 arquivos de `core/domain` listados acima, byte a byte**

Run: `for f in ReservationStateMachine HoldDeadlineCalculator RefundPolicy SplitCostUseCase; do cp composeApp/src/commonMain/kotlin/br/com/sportconnect/core/domain/$f.kt server/src/main/kotlin/br/com/sportconnect/core/domain/$f.kt; done`

- [ ] **Step 3: Copiar `Fixtures.kt` e `ReservationStateMachineTest.kt` para `server/src/test`**

Run: `cp composeApp/src/commonTest/kotlin/br/com/sportconnect/core/Fixtures.kt server/src/test/kotlin/br/com/sportconnect/core/Fixtures.kt && cp composeApp/src/commonTest/kotlin/br/com/sportconnect/core/ReservationStateMachineTest.kt server/src/test/kotlin/br/com/sportconnect/core/ReservationStateMachineTest.kt`

- [ ] **Step 4: Rodar o teste copiado dentro de `:server` e confirmar que passa sem nenhuma edição**

Run: `./gradlew :server:test --tests "br.com.sportconnect.core.ReservationStateMachineTest" 2>&1 | tail -80`
Expected: PASS — todos os casos de `ReservationStateMachineTest` passam **sem ter editado uma linha** dos arquivos copiados. Se algo não compilar, o ajuste permitido é só em imports/visibilidade (ex. um `internal` que dependia de outro arquivo do mesmo pacote em `composeApp` e não foi copiado) — nunca em lógica de negócio.

- [ ] **Step 5: Commit**

```bash
git add server/src/main/kotlin/br/com/sportconnect/core server/src/test/kotlin/br/com/sportconnect/core server/build.gradle.kts
git commit -m "feat(server): porta a maquina de estados e o modelo de dominio puro para o servidor"
```

---

*Plano gerado a partir de [14](../../14-arquitetura-backend.md), [15](../../15-contrato-api.md), [16](../../16-modelo-dados.md), [17](../../17-adr-gateway-pagamento.md). Próximos planos naturais depois deste: `POST /jogos` + ações de pagamento (usa o que a Task 7 portou), `/auth/token/renovar` (usa `refresh_tokens` já criado na Task 4), e a troca do `InMemoryOtpStore` por Redis.*
