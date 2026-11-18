# 14 — Arquitetura técnica do backend · SportConnect

> Revisão de 18/11/2026. Fonte: o app já construído (`composeApp/src/commonMain/kotlin/br/com/sportconnect/core/`) — domínio, casos de uso, máquina de estados e os "repositórios fake" que hoje simulam o servidor — cruzado com as regras de negócio do [03](03-product-design-prd.md) §8–§12. Este documento vem **antes** do [15 — Contrato de API](15-contrato-api.md) e do [16 — Modelo de dados](16-modelo-dados.md): aqui ficam as decisões que os dois outros documentos assumem como dadas, para não repeti-las em cada endpoint/tabela.
>
> **Por que nesta ordem (backend → banco):** o cliente (Android, iOS, Web, Desktop) já está pronto e já define, no código, exatamente que operações, formatos e erros ele espera. Isso inverte a ordem usual de projeto — aqui o contrato de API não é desenhado do zero, é **extraído** do cliente existente. O banco de dados, por sua vez, é a camada que serve esse contrato por dentro: desenhá-lo depois da API garante que toda tabela exista para servir um endpoint real, não o contrário.

---

## 1. Como ler isto

Hoje, "o backend" é simulado inteiramente dentro do app: `FakeGameRepository`, `FakeArenaRepository`, `FakeAuthRepository`, `FakePaymentGateway`, mais um `ReservationService` que orquestra tudo com um único `Mutex` em memória. Esse código é o **protótipo funcional do backend** — a máquina de estados (`ReservationStateMachine`), as regras de prazo (`HoldDeadlineCalculator`), o cálculo de rateio (`SplitCostUseCase`), a política de reembolso (`RefundPolicy`) e o repasse (`PayoutCalculator`) são lógica de negócio pura, sem I/O, já testada (`composeApp/src/commonTest/`) — e devem ser **portados quase literalmente** para o servidor real, não redesenhados. O que muda é só a casca: `Mutex` em processo único → controle otimista no banco; `StateFlow` em memória → Postgres + pub/sub; `gateway fake` → PSP de verdade.

Cada decisão abaixo cita o arquivo Kotlin de onde veio, para quem for portar saber exatamente o que ler.

---

## 2. Estilo de API e por que REST

**Decisão: REST sobre HTTPS, JSON, recursos derivados do modelo de domínio do [03 §10](03-product-design-prd.md).** Não GraphQL, não RPC genérico.

Por quê: os clientes já pensam em recursos com identidade e ciclo de vida (`Jogo`, `Arena`, `Cota`, `Pagamento`) e em **transições de estado** sobre eles (`EventoReserva` — ver §6), não em grafos de consulta arbitrários. Cada tela do app já sabe exatamente o que precisa buscar (`GetGameUseCase`, `GetArenaDetailUseCase` etc. são 1:1 com "GET de um recurso"). GraphQL resolveria um problema de over-fetching que não existe aqui — o 06/09/10 já especificam cada tela e cada card. RPC genérico (ex.: um único endpoint `/executarEvento`) esconderia a forma dos dados e tornaria o contrato mais difícil de validar com OpenAPI.

## 3. Convenção de nomes: português, igual ao Kotlin — de propósito

**Decisão: todo campo JSON usa o mesmo nome (e `camelCase`) da propriedade Kotlin correspondente.** `valorCota`, não `shareAmount`; `numeroVagas`, não `spotCount`; `codigoConvite`, não `inviteCode`.

Isso não é uma preferência estética — é a decisão que mais reduz "erros de API" neste projeto especificamente: as 4 plataformas já desserializam essas formas com `kotlinx.serialization`, que por padrão usa o nome exato da propriedade Kotlin como chave JSON. Nomear os campos da API em português/`camelCase` idêntico ao modelo significa **zero camada de tradução** entre o DTO do backend e o `data class` que o app já tem — sem `@SerialName`, sem mapeador, sem risco de um campo renomeado silenciosamente virar `null`. Caminhos de URL seguem o mesmo princípio: recursos em português (`/jogos`, `/arenas`, `/cotas`, `/pagamentos`), ações como sub-recursos ou verbos claros (`/jogos/{id}/pagar`, `/jogos/{id}/cancelar`).

Isso é uma convenção deste projeto, não uma regra geral de mercado — produtos brasileiros variam entre inglês e português nas APIs; aqui a escolha segue a linguagem ubíqua que os 13 documentos anteriores e o código inteiro já usam.

## 4. Autenticação e sessão

**Hoje (`FakeAuthRepository.kt`):** não existe token nenhum. `currentUser: StateFlow<Usuario?>` é a sessão inteira, persistida localmente em `AppPreferences.session` (id/nome/celular/email em texto puro). O código OTP é fixo (`"123456"`) e login social (`entrarComProvedor`) nem valida o `idToken` de verdade.

**Decisão para o backend real:**
- **JWT de acesso (curto, ~15 min) + refresh token (longo, rotativo)**, `Authorization: Bearer <access>`. Refresh dedicado em `POST /auth/token/renovar`.
- OTP via provedor real de SMS/WhatsApp (12.2 do [03](03-product-design-prd.md)), mas **mantendo exatamente os números já validados em `AuthRules`** (`core/domain/repository/AuthRepository.kt`): validade de **10 min**, **5 tentativas**, bloqueio de **15 min**. Esses não são números arbitrários do fake — são requisito de produto (US-002), só precisam de um provedor de verdade por trás.
- Login social (Google/Apple): o servidor **valida a assinatura, emissor e expiração do `idToken`** junto ao provedor antes de emitir sessão — o fake pula essa etapa inteira (`entrarComProvedor` deriva um id a partir de `idToken.hashCode()`, só serve de placeholder visual).
- `entrarComo(usuarioId)` (troca de perfil "demo") **não existe no contrato real** — é um atalho de demonstração, não aparece no [15](15-contrato-api.md).
- Armazenamento do token no cliente, por plataforma (nenhuma mudança de código acontece agora — é orientação para quando o cliente real for escrito):
  - **Android:** `EncryptedSharedPreferences`/Keystore.
  - **iOS:** Keychain (`kSecClassGenericPassword`).
  - **Desktop:** chaveiro do SO (macOS Keychain / Windows Credential Manager) atrás de `multiplatform-settings`, nunca texto puro em disco.
  - **Web (Wasm):** token de acesso em memória; refresh token em cookie `httpOnly; Secure; SameSite=Strict` emitido pelo próprio backend (evita expor o refresh a XSS via `localStorage` — o Compose Web não tem como marcar `localStorage` como protegido).

## 5. Envelope de erro e catálogo de códigos

**Decisão:** toda resposta de erro usa o mesmo envelope, e o campo `codigo` é **literalmente** a string que já existe em `RegraNegocioException`/`AuthFailure` no cliente (`core/domain/usecase/Errors.kt`), nunca um novo texto inventado no servidor:

```json
{
  "erro": {
    "codigo": "SEM_VAGAS",
    "mensagem": "As vagas acabaram. Entre na lista de espera."
  }
}
```

Por quê: o app já tem, hoje, telas inteiras mapeadas por esses códigos exatos — por exemplo `PaymentScreen.ErrorContent` decide a ilustração e a ação primária olhando `phase.code == "SEM_VAGAS"`. Se o backend inventar `"NO_SPOTS_LEFT"` em vez de `"SEM_VAGAS"`, essas telas caem no estado de erro genérico silenciosamente — exatamente o tipo de erro de API que este documento existe para evitar. O catálogo completo, código por código, está no [15 §Erros](15-contrato-api.md#erros).

## 6. Máquina de estados: transições, não "salvar o objeto inteiro"

**Hoje:** `GameRepository.save(jogo)` / `.update(id) { transform }` deixam toda a lógica de transição no cliente, que chama `ReservationStateMachine.tentarAplicar(jogo, evento)` localmente. Isso existe só porque não há servidor — **não deve ser portado assim**.

**Decisão:** o servidor é o único lugar que aplica `ReservationStateMachine` (`core/domain/ReservationStateMachine.kt`, já testado e pronto para portar quase linha a linha). O cliente nunca envia "o jogo inteiro atualizado" — ele dispara a **intenção** (pagar, cancelar, sair, oferecer vaga) e o servidor decide a transição, grava o resultado e distribui o novo estado. Cada subtipo de `EventoReserva` é um endpoint ou um job agendado — a tabela completa está no [15 §Jogos](15-contrato-api.md#jogos). As `RegistroTransicao` que a máquina já produz (`de`, `para`, `em`, `ator`, `motivo`) são o log de auditoria da RN-34 — tabela própria no [16](16-modelo-dados.md#jogos_historico).

## 7. Concorrência: de `Mutex` para controle otimista real

`ReservationService.kt` serializa toda mutação com um `Mutex` de processo único e o próprio código comenta: *"no backend isso será controle otimista via `Reserva.version`"*. O campo `version: Long` já existe no modelo (incrementado a cada transição) e hoje é decorativo — nada o confere.

**Decisão:** toda escrita sobre um `Jogo`/`Reserva` exige o `version` atual (corpo da requisição ou `If-Match`); o servidor rejeita com **409** se não bater, e o cliente reage buscando o estado atual e tentando de novo. RNF-04/05 do [03](03-product-design-prd.md#112-concorrência-e-integridade-da-reserva) — zero double-booking — passa a ser garantido por **restrição no banco** (`EXCLUDE USING gist` sobre o intervalo do slot, ver [16](16-modelo-dados.md#slots)), não por lock em memória.

## 8. Tempo real: WebSocket com fallback de polling

RNF-01 do [03](03-product-design-prd.md#111-tempo-real) já pede isso; a decisão aqui é tornar concreto:

- **Canal:** WebSocket por jogo/arena (`wss://api.../ws/jogos/{id}`, `wss://api.../ws/arenas/{id}/agenda`). Cliente sem WebSocket disponível (ou rede restritiva) cai para **polling a cada 10 s** no mesmo endpoint REST do recurso — o app já modela cada tela como um `Flow` reativo (`observeGame`, `observeSlots`, `observeRepasses`), então os dois transportes alimentam o mesmo `StateFlow` do lado do cliente sem mudar a UI.
- **Latência-alvo:** mudança de estado propagada em **≤ 2 s (p95)** após o webhook do gateway de pagamento ser processado (RNF-01).

## 9. Jobs agendados

`ProcessDeadlinesUseCase`/`ReservationService.processarDeadlines` hoje correm a cada **30 s** em memória (`ReservationService.start(intervalo = 30.seconds)`), varrendo todo jogo não-terminal e aplicando o próximo evento temporal que já venceu (`PrazoCotasExpirou`, `PrazoResolucaoExpirou`, `IniciarJogo`, `Concluir`).

**Decisão:** worker dedicado (cron de 30 s ou fila com *delay* por jogo — equivalente em efeito), **idempotente** (reexecutar sem efeito duplicado é requisito, RNF-03), rodando a mesma lógica pura de `ReservationStateMachine`. 30 s é o SLA já assumido pelo produto para "o jogo vira `EXPIRADO`/`CONFIRMADO` sozinho" — não é um detalhe de implementação, é latência visível ao usuário.

## 10. Gateway de pagamento

A interface `PaymentGateway` (`core/domain/repository/PaymentGateway.kt`) já é a porta/adaptador que o [03 §12.1](03-product-design-prd.md#121-gateway-de-pagamento-split--pix) pede. **Decisão: Asaas** é o PSP real contra o qual essa porta será implementada — razão principal: é o único dos três candidatos com custódia programada do split self-service via API (Conta Escrow), que o modelo de repasse D+1 pós-jogo exige. Avaliação completa, trade-offs aceitos e lacunas ainda pendentes de verificação empírica (captura parcial de cartão, custo do Escrow, confirmação jurídica da Q-01) no [ADR — 17](17-adr-gateway-pagamento.md). Dois comportamentos do fake são **requisitos de produto, não acidente de implementação**, e têm que sobreviver à troca para o PSP real:

1. **Pagamento tardio é sempre estornado integralmente** (`ReservationService.onWebhook`): se o webhook de confirmação chega para uma cota que já não é mais `PENDENTE` (prazo passou, jogo cancelado), o dinheiro é devolvido na hora, não fica "preso". RN-18.
2. **Estorno pode falhar e pedir uma chave Pix alternativa** (`StatusPagamento.ESTORNO_FALHOU`, adicionado nesta sessão/T6): quando o cartão de origem recusa a devolução, o backend precisa de um caminho para receber uma chave Pix e tentar de novo (`informarChavePixReembolso`) — isso é sobre o **gateway real**, que pode mesmo recusar estornos de cartão; não é um caso só do protótipo.

O endpoint de webhook do gateway (`POST /webhooks/pagamentos`) e o formato de `WebhookPagamento` estão no [15 §Webhooks](15-contrato-api.md#webhooks).

## 11. Armazenamento de arquivos

`ArenaPhotoStore`/`ProfilePhotoStore` hoje guardam bytes JPEG comprimidos localmente por `Settings`. **Decisão:** objeto storage (S3-compatível) + CDN; a API devolve URL, nunca bytes inline (ver `Arena.fotos: List<String>` — já é uma lista de URLs no modelo, só que hoje apontando para nada real). Upload via `POST` multipart ou URL pré-assinada — detalhe de implementação, não compromete o contrato (`fotos` continua sendo `List<String>` de URLs do ponto de vista do cliente).

## 12. Analytics

`AnalyticsTracker`/`AnalyticsEvents` (60 eventos nomeados, `core/domain/repository/AnalyticsTracker.kt`) é *fire-and-forget* — não precisa de resposta síncrona nem de modelagem relacional. **Decisão:** não entra no banco operacional (§16); vai para um serviço de eventos (ingestão em lote, ex. endpoint `POST /eventos` que empilha e repassa a um provedor como Amplitude/Segment, ou direto para o provedor do lado do cliente). Mantido fora do [16](16-modelo-dados.md) de propósito.

## 13. Dívidas a resolver antes de desenhar a API (achadas nesta auditoria)

Estas quatro inconsistências existem **no cliente hoje** e foram encontradas relendo o código inteiro para este documento. Resolvidas aqui — a decisão de qual caminho é o "real" — para o [15](15-contrato-api.md) não documentar dois contratos concorrentes para a mesma coisa:

1. **Duas listas de espera paralelas.** `SocialStore.queues` (em memória, o que a `GameDetailScreen` realmente usa) e `GameRepository.observeWaitlist`/`saveWaitlist` + `JoinWaitlistUseCase` (registrado no DI, nunca chamado por nenhuma tela). **Decisão: o contrato real é o segundo** — fila de espera é estado do servidor, não do cliente. `SocialStore.queues` deve ser removido do cliente quando a API existir; o [15](15-contrato-api.md#lista-de-espera) documenta só o caminho correto.
2. **Dois modelos de avaliação pós-jogo.** `core/model/Social.kt` define `Avaliacao` (marcada "Fase 2", nunca referenciada em código nenhum) enquanto a avaliação real (T7, usada por `GameDetailScreen`/`SocialStore`) é `GameRating(attendance: Map<String, Attendance>, courtStars, comment)` + `enum Attendance`, e nem é `@Serializable`. **Decisão: o contrato real segue o formato de `GameRating`/`Attendance`** — mapa de presença por usuário + nota da quadra + comentário. `Avaliacao` em `Social.kt` deve ser removida do cliente; o [16](16-modelo-dados.md#avaliacoes) desenha a tabela a partir de `GameRating`, não de `Avaliacao`.
3. **`courtStars`/`comment` nunca são persistidos.** Mesmo no caminho "real" (2), `SocialStore.submitRating` hoje descarta a nota da quadra e o comentário — só guarda a presença. Quando o backend existir, isso deixa de ser aceitável: o [16](16-modelo-dados.md#avaliacoes) já reserva as colunas.
4. **Código de convite não é aleatório.** `CreateGameUseCase.gerarCodigo` deriva o código de 6 caracteres de um hash determinístico do id do jogo — previsível, não adequado para produção (alguém poderia adivinhar códigos de convite). **Decisão: o backend gera o código com um gerador aleatório de verdade** (ex. `ULID`/`nanoid` truncado ao alfabeto já definido em `CreateGameUseCase` — sem os caracteres ambíguos `0,1,I,O`), mantendo o mesmo formato visual.

## 14. Plataformas cliente — o que a API precisa respeitar

| Plataforma | Implicação para a API |
|---|---|
| **Android** | Deep links `https://sportconnect.app/j/{codigo}` precisam resolver com HTTP normal (App Links) antes de qualquer chamada autenticada — convite é sempre público/sem sessão. |
| **iOS** | Mesma exigência via Universal Links; `idToken` do Apple Sign In tem formato próprio (`identityToken` JWT), o servidor precisa validar contra a chave pública da Apple, não da Google. |
| **Web (Wasm)** | RNF-25: página de convite precisa responder rápido mesmo sem o bundle Compose/Wasm carregado — o endpoint de convite público (`GET /convites/{codigo}`) deve ser leve e **não exigir autenticação**, para servir uma pré-visualização mesmo antes do app carregar (meta tags Open Graph geradas no servidor, não no cliente). |
| **Desktop** | Sem deep link de sistema universal — o app resolve o código de convite por colar/digitar; a API não precisa de nada especial além do endpoint público já citado. |
| **Todas** | Fuso fixo `America/Sao_Paulo` (UTC−03:00, sem horário de verão desde 2019 — `TimeZones.kt`). A API **nunca envia fuso**: todo timestamp é UTC (`Instant` ISO-8601, ex. `"2026-11-18T22:00:00Z"`); a formatação local é sempre client-side. |

---

*Depende de: [03](03-product-design-prd.md) §8–§12 (regras de negócio, máquinas de estado, NFRs, integrações). Alimenta: [15 — Contrato de API](15-contrato-api.md), [16 — Modelo de dados](16-modelo-dados.md).*
