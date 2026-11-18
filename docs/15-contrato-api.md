# 15 — Contrato de API · SportConnect

> Revisão de 18/11/2026. Todo endpoint abaixo foi **extraído diretamente** das interfaces (`core/domain/repository/`), dos casos de uso (`core/domain/usecase/`) e da máquina de estados (`core/domain/ReservationStateMachine.kt`) que o app já implementa contra um "backend fake" — não foi desenhado do zero. Convenções gerais (nomes de campo, erro, auth, tempo real, concorrência) estão em [14 — Arquitetura técnica](14-arquitetura-backend.md); regras de negócio (RN-xx) estão em [03 §8](03-product-design-prd.md#8-regras-de-negócio-rn). Este documento referencia RN/arquivo-fonte em cada endpoint para quem for implementar poder confirmar contra o código.
>
> **Convenção de campo monetário:** todo valor é um inteiro em centavos (`Long`), nunca ponto flutuante (RN-01). Nos exemplos JSON abaixo, `"valorCota": 2000` significa R$ 20,00.
> **Convenção de data/hora:** sempre UTC, ISO-8601 (`"2026-11-21T22:00:00Z"`). Sem campo de fuso — a conversão para `America/Sao_Paulo` é sempre do cliente.
> **Autenticação:** `Authorization: Bearer <token de acesso>` em todo endpoint marcado 🔒. Endpoints marcados 🌐 são públicos (link de convite) — nunca exigem sessão.
> **Concorrência otimista:** todo `PATCH`/ação que muta um `Jogo` espera o campo `version` do estado lido mais recentemente; resposta **409** se desatualizado (ver [14 §7](14-arquitetura-backend.md#7-concorrência-de-mutex-para-controle-otimista-real)).
> **Idempotência:** toda ação que gera cobrança (`pagar`, `cobrir-diferenca`, `reivindicar`) exige um cabeçalho `Idempotency-Key` (UUID gerado pelo cliente); reenviar a mesma chave devolve a mesma `Cobranca`, nunca cobra duas vezes (RNF-06, `Pagamento.idempotencyKey` já existe no modelo).

---

## Sumário

1. [Autenticação](#1-autenticação) — `AuthRepository.kt`
2. [Perfil (`/me`)](#2-perfil-me) — `AccountUseCases.kt`, `AthleteUseCases.kt`
3. [Arenas e quadras](#3-arenas-e-quadras) — `ArenaRepository.kt`, `ArenaUseCases.kt`
4. [Administração da arena](#4-administração-da-arena) — `ArenaRepository.kt`, `ArenaDashboardUseCase.kt`, `ArenaReportsUseCase.kt`
5. [Jogos](#5-jogos) — `GameRepository.kt`, `GameUseCases.kt`
6. [Lista de espera e substituição](#6-lista-de-espera-e-substituição) — `WaitlistManager.kt`, `GameUseCases.kt`
7. [Avaliação pós-jogo e presença](#7-avaliação-pós-jogo-e-presença) — `SocialStore.kt` (T7)
8. [Pagamentos](#8-pagamentos) — `PaymentGateway.kt`
9. [Webhooks (gateway → backend)](#9-webhooks-gateway--backend)
10. [Tempo real (backend → cliente)](#10-tempo-real-backend--cliente)
11. [Eventos de analytics](#11-eventos-de-analytics)
12. [Catálogo de erros](#12-catálogo-de-erros)
13. [Apêndice — eventos da máquina de estados → endpoint/job](#13-apêndice--eventos-da-máquina-de-estados--endpointjob)

---

## 1. Autenticação

Números de regra (validade do código, tentativas, bloqueio) vêm de `AuthRules` em `AuthRepository.kt` — não são escolha livre do backend, são requisito de produto (US-002).

### `POST /auth/otp` 🌐
Solicita um código de uso único por SMS ou WhatsApp.
- **Request:** `{ "celular": "+5511988887777", "canal": "SMS" | "WHATSAPP" }`
- **Resposta:** `204 No Content`
- **Erros:** `PHONE_INVALID` (400) — celular fora do formato E.164 BR.
- Rate limit: um pedido novo reinicia a janela de tentativas se o celular mudou (`FakeAuthRepository`).

### `POST /auth/otp/confirmar` 🌐
- **Request:** `{ "celular": "+5511988887777", "codigo": "123456" }`
- **Resposta `200`:** `{ "usuario": Usuario, "tokens": { "acesso": "<jwt>", "renovacao": "<token>", "expiraEm": "2026-11-18T23:00:00Z" } }`
- **Erros:** `CODE_EXPIRED` (410, janela de **10 min**) · `CODE_INVALID` (400) · `TOO_MANY_ATTEMPTS` (429, corpo inclui `"tentarApos": "<instant>"`, bloqueio de **15 min** após **5** tentativas erradas).
- Se nenhum usuário tiver esse celular, o backend **cria um convidado** (`nome: "Convidado"`) — mesmo comportamento do fake, é intencional (US-001: pagar sem conta prévia).

### `POST /auth/social` 🌐
Login via Google/Apple. O backend valida o `idToken` contra o provedor (assinatura, emissor, expiração) — isso é o que o fake **não** faz e o real precisa fazer.
- **Request:** `{ "provedor": "google" | "apple", "idToken": "<jwt do provedor>", "email": "...", "nome": "..." }`
- **Resposta `200`:** igual a `/auth/otp/confirmar`.
- **Erros:** `PROVIDER_TOKEN_INVALID` (401).

### `POST /auth/token/renovar`
- **Request:** `{ "renovacao": "<token>" }`
- **Resposta `200`:** novo par de tokens. Refresh é rotativo: o token antigo é invalidado.
- **Erros:** `REFRESH_TOKEN_INVALID` (401).

### `POST /auth/logout` 🔒
Invalida o refresh token atual. `204`.

### `POST /me/celular/solicitar` 🔒
Primeiro passo de `trocarCelular` (RN está em US-002): pede OTP para um número novo, reaproveitando a mesma janela/tentativas/bloqueio de `/auth/otp`.
- **Request:** `{ "celular": "+5511999990000" }` → `204`.

### `POST /me/celular/confirmar` 🔒
- **Request:** `{ "celular": "+5511999990000", "codigo": "123456" }` → `200` com `Usuario` atualizado.
- **Erros:** mesmos de `/auth/otp/confirmar`.

---

## 2. Perfil (`/me`)

### `GET /me` 🔒 → `Usuario` (ver [16 §usuarios](16-modelo-dados.md#usuarios)).

### `PATCH /me` 🔒
- **Request:** `{ "nome": "Ana Clara", "email": "ana@exemplo.com" }` (nome ≥ 2 caracteres após `trim`; email valida formato simples se enviado).
- **Resposta `200`:** `Usuario`. **Erros:** `VALIDATION_ERROR` (422).

### `POST /me/consentimentos` 🔒
Registra aceite de termos/privacidade e opt-in de marketing (LGPD, US-003).
- **Request:** `{ "versaoTermos": "2026-11", "marketing": true }` → `200` com `Usuario`.

### `DELETE /me` 🔒
Solicita exclusão de conta (LGPD art. 18). Se o usuário tiver cota `PAGA` em jogo não-terminal, a exclusão é **adiada** para o fim do último jogo pendente, não imediata.
- **Request (opcional):** `{ "aposJogosAte": "<instant>" }`
- **Resposta `202 Accepted`:** `{ "agendadaApos": "<instant>" | null }` — `null` = exclusão imediata (anonimização em até 15 dias, RNF-11).

### `GET /me/exportar` 🔒
Portabilidade de dados (LGPD). Devolve o JSON completo — formato já fixado no cliente (`ExportMyDataUseCase`), **não inventar um novo**:
```json
{
  "geradoEm": "...",
  "perfil": { "id": "...", "nome": "...", "celular": "...", "email": "...", "criadoEm": "...", "papeis": ["ATLETA"] },
  "consentimentos": [{ "tipo": "...", "concedido": true, "em": "..." }],
  "jogos": [{ "id": "...", "titulo": "...", "arena": "...", "quadra": "...", "inicio": "...", "status": "...", "organizador": true }],
  "pagamentos": [{ "jogoId": "...", "valorCentavos": 2150, "status": "PAGA", "pagoEm": "..." }]
}
```

### `GET /me/jogos` 🔒 → `{ "proximos": [Jogo], "historico": [Jogo] }`
Jogos onde o usuário organiza ou tem cota não-`PENDENTE`, divididos por `!status.terminal`.

### `GET /me/pagamentos` 🔒 → `[MeuPagamento]`
```json
{ "pagamentoId": "...", "jogo": { "...": "..." }, "valorCota": 2000, "taxa": 150, "metodo": "PIX", "pagoEm": "...", "reembolsado": false, "complemento": false }
```
> Hoje isso é derivado na hora a partir das cotas do jogo (não existe tabela própria no cliente) — o backend **deve materializar** uma tabela real de pagamentos (ver [16 §pagamentos](16-modelo-dados.md#pagamentos)), só a *forma* da resposta é a mesma.

### `GET /me/notificacoes` 🔒 → `[Aviso]`
```json
{ "id": "...", "tipo": "CONFIRMADO" | "PAGAMENTO_RECEBIDO" | "PROGRESSO" | "PRAZO" | "CANCELADO", "jogo": {"...": "..."}, "em": "...", "lido": false, "pagas": 8, "total": 12, "valor": 24000 }
```
### `POST /me/notificacoes/marcar-lidas` 🔒 — `{ "ids": ["..."] }` → `204`.

### Cartões salvos
- `GET /me/cartoes` 🔒 → `[{ "id": "...", "bandeira": "VISA", "final": "4242", "validade": "12/29" }]` — **nunca** número completo/CVV; o que trafega é um token do gateway, não dado de cartão (RNF-12, PCI-DSS SAQ-A).
- `DELETE /me/cartoes/{id}` 🔒 → `204`.

---

## 3. Arenas e quadras

### `GET /arenas` 🌐
Busca por proximidade (US-020). Sem sessão — a vitrine é pública.
- **Query:** `lat`, `lng` (origem; default = Av. Paulista/MASP se omitido), `esporte` (opcional), `raioKm` (opcional).
- **Resposta `200`:** `[ArenaProxima]`, ordenado por distância:
```json
{ "arena": Arena, "distanciaKm": 2.3, "precoMinimoHora": 12000, "esportes": ["PADEL"], "quantidadeQuadras": 3 }
```
- **Regra:** só arena com `status == "ATIVA"` aparece (T9 — antes disso era um bug: qualquer status aparecia).

### `GET /arenas/{id}` 🌐 → `ArenaDetalhe`
```json
{ "arena": Arena, "quadras": [Quadra], "esportes": ["PADEL", "BEACH_TENNIS"], "precoMinimoHora": 12000, "precoMaximoHora": 25000 }
```
- **Erros:** `404` se não existe, **ou** se existe mas `status != "ATIVA"` e quem pergunta não é o gestor dela (não expor arena em cadastro ao público).

### `GET /arenas/{id}/quadras` 🔒🌐 → `[Quadra]`

### `GET /arenas/{id}/quadras/{quadraId}/slots` 🌐
- **Query:** `data` (`YYYY-MM-DD`, fuso São Paulo), `esporte` (opcional), `incluirIndisponiveis` (opcional, default `false`).
- **Resposta `200`:** `[SlotDisponivel]` — exclui quadra inativa, slot no passado, esporte incompatível com a quadra; `BLOQUEADO` nunca é oferecido (RN-33).
```json
{ "slot": Slot, "quadra": Quadra, "disponivel": true }
```

---

## 4. Administração da arena

Tudo abaixo exige papel `GESTOR_ARENA` ou `OPERADOR_ARENA` com `Usuario.arenaId` igual ao `{id}` do caminho — `SEM_PERMISSAO` (403) caso contrário.

### `PATCH /arenas/{id}` 🔒
Cadastro/edição (T9, US-012). Qualquer subconjunto dos campos pode vir no corpo; o backend aplica só o que foi enviado.
- **Request (exemplo):** `{ "nome": "...", "telefone": "...", "endereco": { "logradouro": "...", "numero": "...", "bairro": "...", "cep": "..." }, "comodidades": ["Vestiário", "Wi-Fi"], "horarioFuncionamento": "Seg a sáb, 7h às 23h", "chavePix": "pagamentos@arena.com", "duracaoHoldHoras": 4, "janelaCancelamentoHoras": 24 }`
- **Resposta `200`:** `Arena` atualizada.
- **Erros:** `VALIDATION_ERROR` (422) — ex. `duracaoHoldHoras` fora de 1–48 (RN-10), `janelaCancelamentoHoras` fora de 0–72 (RN-23), `chavePix` que falha `isValidPixKey` (CPF/CNPJ/e-mail/celular/chave aleatória — `core/domain/PixKey.kt`).
- **Nota de modelagem:** o CNPJ da arena (`documento`) **não é o CPF do responsável** — são dados diferentes; se o formulário de onboarding também coletar o CPF de quem está cadastrando, ele não tem campo nesta entidade (ver [14 §13](14-arquitetura-backend.md#13-dívidas-a-resolver-antes-de-desenhar-a-api) se isso precisar de modelagem própria no futuro).

### `POST /arenas/{id}/publicar` 🔒
Muda `status` para `ATIVA` — a partir daqui a arena aparece em `GET /arenas` (T9). Sem corpo. **Erros:** `CADASTRO_INCOMPLETO` (422) se o backend decidir exigir campos mínimos antes de publicar (nome, endereço, ao menos uma quadra) — a UI já faz esse checklist no cliente, mas o servidor deve revalidar.

### `POST /arenas/{id}/quadras` 🔒
- **Request:** `{ "nome": "...", "tipo": "SOCIETY_SINTETICO", "piso": "...", "coberta": true, "esportes": [{ "esporte": "FUTEBOL_SOCIETY", "capacidadeMax": 14 }], "precoHora": 20000, "precoHoraPico": 25000 }`
- **Resposta `201`:** `Quadra`. **Erros:** `VALIDATION_ERROR` (422) se `capacidadeMax` > o máximo do esporte (RN-05).

### `PATCH /arenas/{id}/quadras/{quadraId}` 🔒 — mesmos campos, parciais. `PATCH /arenas/{id}/quadras/{quadraId}/slots/{slotId}` 🔒 — bloqueio manual: `{ "status": "BLOQUEADO", "motivoBloqueio": "Manutenção" }`. **Erros:** `SLOT_OCUPADO` (409) se o slot já estiver `EM_CHECKOUT`/`PRE_RESERVADO`/`RESERVADO` — a arena cancela a reserva (§5) em vez de bloquear por cima (RN-33).

### `GET /arenas/{id}/agenda` 🔒 → `PainelArena`
- **Query:** `data` (`YYYY-MM-DD`).
```json
{ "data": "2026-11-19", "agenda": [{ "slot": Slot, "quadra": Quadra, "jogo": Jogo | null }], "ocupacaoPct": 72.5, "confirmados": 5, "preReservas": 2, "livres": 3, "bloqueados": 1, "receitaPrevistaDia": 84000, "repasseAgendado": 12000, "repasseLiquidado": 45000 }
```

### `GET /arenas/{id}/relatorio` 🔒 → `ArenaReport`
- **Query:** `dias` (janela rolante terminando hoje).
```json
{ "dias": 30, "courts": [{ "courtId": "...", "courtName": "...", "offered": 120, "booked": 86 }], "netRevenue": 450000, "cancelled": 3, "expired": 7 }
```

### `GET /arenas/{id}/repasses` 🔒 → `[Repasse]` (ver [16 §repasses](16-modelo-dados.md#repasses)).

### `POST /arenas/{id}/fotos` 🔒 — multipart, até 5 fotos (`ArenaPhotoStore.MAX`), comprimidas a 1600px no servidor. **Resposta `201`:** `{ "fotos": ["https://.../1.jpg", ...] }`. `DELETE /arenas/{id}/fotos/{index}` 🔒 → `204`.

---

## 5. Jogos

### `POST /jogos` 🔒
Cria um jogo em `RASCUNHO` e trava o slot por 10 min (RN-08) — `CreateGameUseCase`.
- **Request:** `{ "slotId": "...", "esporte": "FUTEBOL_SOCIETY", "numeroVagas": 14, "politica": "ORGANIZADOR_COBRE", "garantia": "PIX", "tetoComplementoPct": 50, "visibilidade": "PRIVADO", "titulo": null, "observacoes": null }`
- **Resposta `201`:** `Jogo` (com `codigoConvite` já gerado — aleatório no backend real, ver [14 §13.4](14-arquitetura-backend.md#13-dívidas-a-resolver-antes-de-desenhar-a-api)).
- **Erros:** `SLOT_INEXISTENTE`, `SLOT_INDISPONIVEL`, `QUADRA_INEXISTENTE`, `ARENA_INEXISTENTE`, `ESPORTE_NAO_SUPORTADO`, `VAGAS_INVALIDAS`, `COTA_MINIMA`, `POLITICA_INVIAVEL` — ver [§12](#12-catálogo-de-erros).

### `GET /jogos/{id}` 🔒 → `Jogo`. Assina tempo real em `wss://.../ws/jogos/{id}` (ver [§10](#10-tempo-real-backend--cliente)).

### `GET /jogos/convite/{codigo}` 🌐
Página pública do convite (US-030) — **sem sessão**, precisa ser leve (RNF-25).
- **Resposta `200`:** `Jogo` (campos de atletas reduzidos a primeiro nome + inicial, RNF-13). **Erros:** `CONVITE_INVALIDO` (404).
- `codigo` aceita tanto o código puro quanto uma URL completa terminando nele (o cliente já normaliza assim hoje).

### `GET /jogos/meus` 🔒 → `{ "proximos": [Jogo], "historico": [Jogo] }` (alias de `/me/jogos`, mantido por simetria com o caso de uso `GetMyGamesUseCase`).

### `GET /jogos/abertos` 🔒 → `[Jogo]`
- **Query:** `esporte` (opcional). Filtra `visibilidade == "VAGAS_ABERTAS" && status == "AGUARDANDO_PAGAMENTOS" && etapaPrazo == "COLETANDO_COTAS" && vagasRestantes > 0 && inicio > agora`.

### `POST /jogos/{id}/pagar` 🔒 — exige `Idempotency-Key`
Paga a cota do organizador (jogo em `RASCUNHO`), uma cota padrão (jogo em `AGUARDANDO_PAGAMENTOS`) ou a cota complemento própria (`JANELA_RESOLUCAO`) — a seleção é automática por estado, igual a `PayShareUseCase`.
- **Request:** `{ "metodo": "PIX" | "CARTAO", "cotaId": "..." }` (`cotaId` opcional — se omitido, o servidor escolhe a cota livre).
- **Resposta `201`:** `Cobranca`:
```json
{ "pagamentoId": "...", "jogoId": "...", "cotaId": "...", "metodo": "PIX", "valorCota": 2000, "taxaConveniencia": 150, "pixCopiaECola": "000201...6304A1B2", "expiraEm": "..." }
```
- **Erros:** `JOGO_EM_RASCUNHO`, `JA_PAGOU`, `PRAZO_ENCERRADO`, `SEM_VAGAS`, `JOGO_FECHADO`, `JOGO_INEXISTENTE`, `NAO_AUTENTICADO`.

### `POST /jogos/{id}/cobrir-diferenca` 🔒 — exige `Idempotency-Key`
Só o organizador, só em `JANELA_RESOLUCAO`, sem taxa de conveniência (RN-14). `CoverDifferenceUseCase`.
- **Resposta `201`:** `Cobranca`. **Erros:** `SOMENTE_ORGANIZADOR`, `FORA_DA_JANELA`, `JOGO_INEXISTENTE`.

### `POST /jogos/{id}/cancelar` 🔒
- **Request:** `{ "motivo": "Chuva forte" }` (obrigatório se quem cancela é a arena — RN-24; opcional para o organizador).
- **Resposta `200`:** `Jogo` cancelado. **Erros:** `SEM_PERMISSAO` (nem organizador nem arena vinculada), e a própria máquina de estados pode rejeitar fora da janela de cancelamento gratuito (RN-23) — ver [§13](#13-apêndice--eventos-da-máquina-de-estados--endpointjob).

### `POST /jogos/{id}/saida` 🔒
Atleta (não organizador) sai antes da confirmação (RN-21). `LeaveGameUseCase`.
- **Resposta `200`:** `Jogo`. **Erros:** `SEM_COTA`.

---

## 6. Lista de espera e substituição

> Resolve a dívida #1 do [14 §13](14-arquitetura-backend.md#13-dívidas-a-resolver-antes-de-desenhar-a-api): este é o **único** contrato de lista de espera — o estado vive no servidor, nunca só no cliente.

### `POST /jogos/{id}/lista-espera` 🔒 (RN-27)
- **Resposta `201`:** `[ListaEspera]` (fila completa, para o cliente saber a posição). **Erros:** `LISTA_ESPERA` com a mensagem específica (jogo não aceita fila agora / ainda há vaga livre / fila cheia / já está na fila / já está no jogo — ver [§12](#12-catálogo-de-erros)).
### `DELETE /jogos/{id}/lista-espera` 🔒 → `204`.
### `GET /jogos/{id}/lista-espera` 🔒 → `[ListaEspera]` (tempo real, mesma assinatura do jogo).

### `POST /jogos/{id}/cotas/{cotaId}/oferecer` 🔒 (T8, RN-26)
Atleta pago oferece/retira a própria vaga para a fila. `ToggleSpotOfferUseCase`.
- **Request:** `{ "oferecer": true }`
- **Resposta `200`:** `Jogo`. **Erros:** `SUBSTITUICAO` (mensagem específica: fora da janela CONFIRMADO→T-1h / cota não existe / cota não é sua / cota não está PAGA / organizador não pode oferecer).

### `POST /jogos/{id}/cotas/{cotaId}/reivindicar` 🔒 — exige `Idempotency-Key`
Quem está na lista de espera paga a vaga oferecida. `ClaimOfferedSpotUseCase`.
- **Request:** `{ "metodo": "PIX" }`
- **Resposta `201`:** `Cobranca` (a cota original só vira `REEMBOLSADA` quando o pagamento deste confirmar — ver webhook, [§9](#9-webhooks-gateway--backend)).
- **Erros:** `SUBSTITUICAO_INDISPONIVEL` (janela fechada / vaga não existe mais / vaga não está oferecida), `JA_PAGOU`, `JOGO_INEXISTENTE`.

---

## 7. Avaliação pós-jogo e presença

> Resolve as dívidas #2 e #3 do [14 §13](14-arquitetura-backend.md#13-dívidas-a-resolver-antes-de-desenhar-a-api): o contrato segue o formato **realmente usado** pelo cliente (`GameRating`/`Attendance`), não a classe `Avaliacao` morta.

### `POST /jogos/{id}/avaliacao` 🔒 (T7, E15/US-130)
Só quem participou e ainda não avaliou (`jogo.status == "CONCLUIDO"`).
- **Request:**
```json
{
  "attendance": { "u-atleta-1": "ATTENDED", "u-atleta-2": "NO_SHOW", "u-atleta-3": "LATE" },
  "courtStars": 4,
  "comment": "Quadra boa, iluminação fraca."
}
```
- **Resposta `201`:** `204`. **Erros:** `JA_AVALIADO` (409), `JOGO_NAO_CONCLUIDO` (409).
- **Nota:** `courtStars`/`comment` **devem** ser persistidos pelo backend real — o cliente hoje descarta os dois (ver [14 §13.3](14-arquitetura-backend.md#13-dívidas-a-resolver-antes-de-desenhar-a-api)).

### `GET /me/presenca` 🔒 → `{ "presente": 9, "total": 10 }` ou `204` se nunca foi avaliado (T7 — perfil mostra "Presença: 9/10 dos jogos" só quando isto não é vazio).

---

## 8. Pagamentos

### `GET /pagamentos/{id}` 🔒 → `Pagamento` (ver [16 §pagamentos](16-modelo-dados.md#pagamentos)). Usado pelo cliente como *fallback* de polling quando o WebSocket de `/jogos/{id}` não está disponível — o status muda via o mesmo webhook do [§9](#9-webhooks-gateway--backend).

### `POST /pagamentos/{id}/informar-chave-pix` 🔒 (T6)
Depois de um estorno recusado pelo cartão (`status == "ESTORNO_FALHOU"`), reenvia o reembolso para uma chave Pix.
- **Request:** `{ "chave": "pagamentos@exemplo.com" }` — validada por `isValidPixKey` (CPF/CNPJ/e-mail/celular/chave aleatória).
- **Resposta `200`:** `Pagamento` (volta para `ESTORNO_EM_PROCESSAMENTO`). **Erros:** `CHAVE_PIX_INVALIDA` (422), `ESTORNO_NAO_FALHOU` (409) se o pagamento não estiver em `ESTORNO_FALHOU`.

---

## 9. Webhooks (gateway → backend)

O backend assina webhooks do PSP (Asaas/Pagar.me/Mercado Pago — [03 Q-02](03-product-design-prd.md#15-questões-em-aberto), ainda em aberto) e os traduz para o formato interno que `ReservationService.onWebhook` já consome:

```json
{
  "pagamentoId": "...", "jogoId": "...", "cotaId": "...", "pagadorId": "...", "pagadorNome": "...",
  "metodo": "PIX", "status": "CONFIRMADO" | "FALHOU" | "EXPIRADO" | "ESTORNADO" | "ESTORNO_FALHOU",
  "em": "...",
  "cobertura": false,
  "substituicaoCotaId": null
}
```

**Comportamentos que o backend precisa replicar exatamente** (não são detalhe, são regra de negócio já implementada e testada em `ReservationService.onWebhook`):
- `CONFIRMADO` + `substituicaoCotaId` preenchido → aplica a substituição (RN-26): cota original vira `REEMBOLSADA` (sem taxa) e o estorno correspondente é **disparado para o pagamento original**, não para o novo.
- `CONFIRMADO` + `cobertura: true` → cobertura do organizador (RN-14).
- `CONFIRMADO` chegando depois do prazo (cota já não está mais `PENDENTE`, ou jogo já `CANCELADO`/`EXPIRADO`) → **estorna o pagamento tardio integralmente**, nunca fica com o dinheiro (RN-18).
- `FALHOU`/`EXPIRADO` → libera a trava de checkout da cota (ela volta a ficar `PENDENTE` e disponível).

---

## 10. Tempo real (backend → cliente)

Canal | Assunto | Payload
---|---|---
`wss://.../ws/jogos/{id}` | Mudança de estado do jogo/reserva/cotas | `Jogo` completo (o cliente já trata como substituição total do estado local)
`wss://.../ws/jogos/{id}/lista-espera` | Fila mudou | `[ListaEspera]`
`wss://.../ws/arenas/{id}/agenda?data=` | Agenda do dia mudou (reserva nova, cancelamento) | `PainelArena`
`wss://.../ws/arenas/{id}/repasses` | Repasse mudou de status | `[Repasse]`

Sem WebSocket disponível, o cliente cai para **polling do GET equivalente a cada 10 s** (RNF-01) — por isso todo canal acima tem um GET REST espelhado nas seções 3–6.

---

## 11. Eventos de analytics

### `POST /eventos` 🔒 — aceita lote
- **Request:** `[{ "nome": "COBRANCA_GERADA", "propriedades": { "pagamento_id": "...", "metodo": "pix" }, "em": "..." }]`
- **Resposta:** `202 Accepted`. *Fire-and-forget* — nunca bloqueia a ação que gerou o evento (ver [14 §12](14-arquitetura-backend.md#12-analytics)). Lista completa dos 60 nomes de evento já usados pelo cliente está em `core/domain/repository/AnalyticsTracker.kt` — o backend não precisa validar o nome contra uma lista fechada, só armazenar/repassar.

---

## 12. Catálogo de erros

Todo código abaixo é **texto literal** já esperado pelo cliente (`RegraNegocioException`/`AuthFailure`, `core/domain/usecase/Errors.kt` + `core/domain/repository/AuthRepository.kt`). O backend não cria códigos novos para estas situações — reaproveita exatamente estes.

| Código | HTTP | Onde é lançado | Gatilho |
|---|---|---|---|
| `NAO_AUTENTICADO` | 401 | qualquer ação 🔒 | sessão ausente/expirada — é o "401" universal do domínio (`AuthRepository.exigirUsuario`) |
| `PHONE_INVALID` | 400 | `/auth/otp` | celular fora do formato |
| `CODE_INVALID` | 400 | `/auth/otp/confirmar` | código não bate |
| `CODE_EXPIRED` | 410 | `/auth/otp/confirmar` | passou de 10 min |
| `TOO_MANY_ATTEMPTS` | 429 | `/auth/otp/confirmar` | 5ª tentativa errada; corpo traz `tentarApos` |
| `PROVIDER_TOKEN_INVALID` | 401 | `/auth/social` | assinatura/emissor/expiração do idToken não valida |
| `SEM_PERMISSAO` | 403 | `/jogos/{id}/cancelar`, endpoints de admin de arena | quem chama não é organizador/arena/gestor vinculado |
| `SOMENTE_ORGANIZADOR` | 403 | `/jogos/{id}/cobrir-diferenca` | quem chama não é o organizador |
| `JOGO_INEXISTENTE` | 404 | várias ações sobre `/jogos/{id}` | id não existe |
| `SLOT_INEXISTENTE` | 404 | `POST /jogos` | `slotId` não existe |
| `QUADRA_INEXISTENTE` | 404 | `POST /jogos` | quadra do slot não existe |
| `ARENA_INEXISTENTE` | 404 | `POST /jogos` | arena da quadra não existe |
| `CONVITE_INVALIDO` | 404 | `GET /jogos/convite/{codigo}` | código não resolve a nenhum convite ativo |
| `SLOT_INDISPONIVEL` | 409 | `POST /jogos` | slot acabou de ser reservado por outro checkout |
| `SLOT_OCUPADO` | 409 | bloqueio manual de slot (admin) | slot já está `EM_CHECKOUT`/`PRE_RESERVADO`/`RESERVADO` — arena precisa cancelar, não bloquear (RN-33) |
| `ESPORTE_NAO_SUPORTADO` | 422 | `POST /jogos` | quadra não suporta o esporte escolhido |
| `VAGAS_INVALIDAS` | 422 | `POST /jogos` | `numeroVagas` fora de `[mín,máx]` do esporte/quadra (RN-03/05) |
| `COTA_MINIMA` | 422 | `POST /jogos` | `V / numeroVagas` cairia abaixo de R$ 5,00 (RN-06) |
| `POLITICA_INVIAVEL` | 422 | `POST /jogos` | janela de coleta resultante < 30 min para a política/garantia escolhida (RN-12) |
| `JOGO_EM_RASCUNHO` | 409 | `POST /jogos/{id}/pagar` | organizador tentando pagar de novo, ou outra pessoa pagando antes da ativação |
| `JA_PAGOU` | 409 | `/pagar`, `/reivindicar` | já existe cota `PAGA` minha nesse jogo |
| `PRAZO_ENCERRADO` | 409 | `/pagar` | passou de **P** (RN-11) |
| `SEM_VAGAS` | 409 | `/pagar` | nenhuma cota `PENDENTE` livre — dispara `CHECKOUT_CONFLITO` no analytics antes de responder |
| `JOGO_FECHADO` | 409 | `/pagar` | status do jogo não aceita mais pagamento (mensagem inclui o status atual) |
| `FORA_DA_JANELA` | 409 | `/cobrir-diferenca` | fora de `JANELA_RESOLUCAO` |
| `SEM_COTA` | 422 | `/saida` | não tem cota `PAGA` nesse jogo |
| `LISTA_ESPERA` | 409 | `/lista-espera` (POST) | mensagem específica: jogo não aceita fila agora / ainda há vaga livre / fila cheia (limite 10) / já está na fila / já está no jogo |
| `SUBSTITUICAO` | 409 | `/cotas/{id}/oferecer` | mensagem específica: fora de CONFIRMADO→T-1h / cota inexistente / cota não é sua / cota não está PAGA / organizador não pode oferecer |
| `SUBSTITUICAO_INDISPONIVEL` | 409 | `/cotas/{id}/reivindicar` | janela fechada, vaga não existe mais, ou vaga não foi oferecida/não está PAGA |
| `JA_AVALIADO` | 409 | `/jogos/{id}/avaliacao` | usuário já enviou avaliação deste jogo |
| `JOGO_NAO_CONCLUIDO` | 409 | `/jogos/{id}/avaliacao` | jogo ainda não está `CONCLUIDO` |
| `CHAVE_PIX_INVALIDA` | 422 | `/pagamentos/{id}/informar-chave-pix`, `PATCH /arenas/{id}` | falha em `isValidPixKey` |
| `ESTORNO_NAO_FALHOU` | 409 | `/informar-chave-pix` | pagamento não está em `ESTORNO_FALHOU` |
| `CADASTRO_INCOMPLETO` | 422 | `/arenas/{id}/publicar` | campos mínimos de cadastro ausentes |
| `VALIDATION_ERROR` | 422 | vários `PATCH` | campo fora da faixa/formato esperado (corpo inclui o(s) campo(s) inválido(s)) |
| `CONFLITO_DE_VERSAO` | 409 | qualquer mutação com `version` desatualizado | ver [14 §7](14-arquitetura-backend.md#7-concorrência-de-mutex-para-controle-otimista-real) — não existe no fake (que usa um `Mutex`), é novo no backend real |

---

## 13. Apêndice — eventos da máquina de estados → endpoint/job

Cada linha é um `EventoReserva` (`core/domain/ReservationStateMachine.kt`) que o backend precisa aplicar com a **mesma lógica pura** já testada no cliente. "Origem" diz se o evento nasce de uma chamada HTTP, do webhook do gateway, ou de um job agendado (ver [14 §9](14-arquitetura-backend.md#9-jobs-agendados)).

| Evento | Origem | Efeito |
|---|---|---|
| `Ativar` | implícito em `CotaPaga` quando o jogo está `RASCUNHO` (o webhook aplica os dois em sequência) | `RASCUNHO` → `CONFIRMADO` (se 1 vaga ou pagamento integral) ou `AGUARDANDO_PAGAMENTOS` (calcula H/P) |
| `CotaPaga` | webhook do gateway (`§9`) | marca uma cota `PAGA`; confirma o jogo se essa era a última pendente |
| `PrazoCotasExpirou` | job agendado, `agora >= P` | aplica a política de não-pagamento (A/B/C) |
| `CoberturaPaga` | webhook (`cobertura: true`) | `JANELA_RESOLUCAO` → `CONFIRMADO` |
| `PrazoResolucaoExpirou` | job agendado, `agora >= H` | janela de resolução terminou sem cobrir/re-ratear → `EXPIRADO` |
| `CancelarPor` | `POST /jogos/{id}/cancelar` | → `CANCELADO`, reembolsa todas as cotas pagas |
| `AtletaSaiu` | `POST /jogos/{id}/saida` | cota → `REEMBOLSADA` (sem taxa), nova cota `PENDENTE` abre a vaga |
| `IniciarJogo` | job agendado, `agora >= T` | `CONFIRMADO` → `EM_ANDAMENTO` |
| `Concluir` | job agendado, `agora >= fim` | `EM_ANDAMENTO` → `CONCLUIDO`, gera o `Repasse` (RN-30) |

Efeitos colaterais que cada transição pode disparar (`EfeitoReserva`) e para onde vão: `Reembolsar` → chamada ao gateway (`estornar`); `VagaLiberada` → recalcula a fila de espera e notifica o próximo (push); `CobrarCobertura`/`PropostaRerrateio` → notificação push ao organizador/pagantes; os demais (`Ativada`, `Confirmada`, `Expirada`, `Cancelada`, `PoliticaAplicada`, `SlotAlterado`) só mudam estado/registram no `core/domain/repository/AnalyticsTracker.kt`.

---

*Depende de: [14 — Arquitetura técnica](14-arquitetura-backend.md), [03](03-product-design-prd.md) §7–§8 (US/RN). Alimenta: [16 — Modelo de dados](16-modelo-dados.md).*
