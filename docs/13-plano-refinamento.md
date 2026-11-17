# 13 · Plano de refinamento: mapa, paridade entre plataformas e o que ficou para trás

> Revisão de 17/11/2026, feita depois das seções A–E do [11](11-backlog-auditoria.md). Fontes: 00 a 12, o código
> em `composeApp/` + `androidApp/` + `iosApp/`, e uma passada real em cada plataforma — Android (emulador),
> iOS (simulador iPhone 17 Pro), web (build de produção no Chrome, 412 px e 1280 px) e desktop.
> Objetivo: deixar o app **redondo** enquanto não há servidor nem contas de loja.
> Prioridade: **P0** = quebra um fluxo ou uma plataforma; **P1** = usabilidade importante; **P2** = refinamento.
> Tamanho: **P** (≤ 2h), **M** (≤ 1 dia), **G** (> 1 dia). Cada item vira **um commit**.

---

## Visão geral

| Área | P0 | P1 | P2 | Itens |
|---|---|---|---|---|
| M. Mapa (US-200) | 1 | 2 | — | M1–M3 |
| W. Web | 1 | 2 | — | W1–W3 |
| X. Plataformas (Android, iOS, desktop) | — | 3 | 1 | X1–X4 |
| A. Acessibilidade | — | 4 | — | A1–A4 |
| T. Telas e fluxos | — | 5 | 4 | T1–T9 |
| Q. Qualidade | — | 1 | 1 | Q1–Q2 |

**Como a revisão foi feita:** 101 dos 909 textos do 07 ainda não aparecem no código — cada grupo deles aponta
uma lacuna (ex.: `home_section_open_games`, `cd_*`, `profile_attendance`, `error_refund_failed_*`). As US do
PRD (03 §6) foram cruzadas com as telas; as fichas e o checklist de acessibilidade do 09 (§3.2, §6, §8) foram
conferidos na árvore de acessibilidade da web (a mesma semântica que o TalkBack e o VoiceOver leem).

---

## M. Mapa (US-200)

**Decisão:** o Google Maps exige chave de API com conta de faturamento, e o projeto ainda não tem essa
estrutura. O MapLibre Compose não tem alvo Wasm (a nossa web é Wasm). Então o mapa é um componente próprio,
**`SCMap`**, em Compose puro no `commonMain`: tiles raster baixados pelo Ktor (que já existe nas 4
plataformas), arrastar, pinça/roda do mouse para zoom e pinos desenhados pelo design system. Resultado: **a
mesma tela em Android, iOS, desktop e web**, sem chave e sem SDK nativo. O endereço dos tiles fica em
`FeatureFlags.MAP_TILE_URL`; antes de publicar, troca-se o OpenStreetMap (uso leve, com atribuição) por um
provedor com chave — Google Map Tiles API, MapTiler ou Stadia (passo a passo no 12 §13).

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| M1 | P0 | G | **Componente `SCMap`**: projeção Web Mercator, cache de tiles em memória (LRU), arrastar, zoom por pinça, duplo toque e roda do mouse, botões +/− (alvos de 48 dp), pinos com preço (`SCMapPin`), pino selecionado, atribuição "© OpenStreetMap" sempre visível, estado sem conexão. Testes da projeção e do enquadramento. | `core/designsystem/component/SCMap.kt` | Base de M2 e M3. Precisa ser igual nas 4 plataformas. |
| M2 | P1 | M | **Busca com mapa**: no compacto, botão "Ver no mapa" (`search_map_view`) alterna lista ↔ mapa, e tocar no pino mostra o card da quadra por cima; no expandido, lista à esquerda e mapa à direita, sincronizados (selecionar card destaca o pino e vice-versa). Os filtros valem para os dois. | `feature/explore/` | US-200 e o 09 ficha 4. No expandido a busca hoje mostra só 2 cards por tela (ver T4). |
| M3 | P1 | P | **Mini-mapa na página da arena** com o pino e "Como chegar"; tocar abre o app de mapas. | `feature/arena/ArenaDetailScreen.kt` | O endereço hoje é só texto. |

## W. Web

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| W1 | P0 | M | **URL e histórico do navegador**: a rota atual aparece na barra de endereço, "voltar/avançar" do navegador funcionam, e abrir `/j/{código}`, `/g/{id}` ou `/a/{id}` direto na web leva à tela certa (hoje sempre abre o onboarding/Home). | `wasmJsMain/main.kt`, `navigation/` | O fluxo principal da web (US-001: convidado abre o link e paga sem instalar) **não funciona** pela URL. |
| W2 | P1 | P | **"Usar minha localização" na web** via `navigator.geolocation` (hoje a web só aceita cidade digitada). | `core/platform/DeviceLocation.wasmJs.kt` | Paridade com Android/iOS; a busca por distância depende disso. |
| W3 | P1 | P | **PWA e cabeçalho**: `manifest.webmanifest` (nome, ícones, cores), `apple-touch-icon`, `theme-color` claro/escuro e descrição. | `wasmJsMain/resources/` | "Adicionar à tela inicial" e aparência correta no celular. |

## X. Plataformas

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| X1 | P1 | M | **Folha nativa de compartilhar** ("Mais opções", `invite_share_other`): Android `Intent.ACTION_SEND`, iOS `UIActivityViewController`, web `navigator.share` (cai para copiar), desktop copia. | `core/platform/Share*.kt`, convite | Prevista no 09 §6.5; hoje só existe WhatsApp e copiar. |
| X2 | P1 | P | **Salvar arquivo de verdade**: extrato CSV (arena) e "Baixar meus dados" (JSON) com o seletor de salvar do FileKit — download na web. | repasses, privacidade | Hoje os dois vão para a área de transferência, o que confunde (LGPD: "baixar meus dados"). |
| X3 | P1 | P | **Janela do desktop**: ícone, tamanho mínimo (360×640), lembrar tamanho e posição; pacote com versão do `gradle.properties` e ícones `.icns/.ico/.png`. | `desktopMain/main.kt`, `build.gradle.kts` | Hoje a janela não tem ícone e o instalador sai sempre como 1.0.0. |
| X4 | P2 | P | **iOS**: gesto de voltar pela borda em todas as telas de pilha e teclado com "Concluído" nos campos numéricos; conferir no simulador. | `iosMain/`, `MainViewController` | Expectativa básica de quem usa iPhone. |

## A. Acessibilidade (09 §8)

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| A1 | P1 | P | **Card de jogo lido por inteiro**: hoje o leitor diz "Esporte: Futebol society, Status: …" **sem o nome nem a data** do jogo. Incluir título, dia e hora, local e prazo, na ordem do 09 §8 (home). Saudação "E aí, Rafael!" é lida por último — corrigir a ordem. | `SCGameCard`, `HomeScreen` | O usuário de leitor de tela não sabe qual jogo é qual. |
| A2 | P1 | P | **Seleção com estado**: esportes do onboarding/Meus esportes e chips de filtro como caixa de seleção com "marcado/não marcado"; abas (barra e rail) e segmentos com "selecionado". | onboarding, explore, `SCNavigation` | Na web aparecem como "button" sem estado; o 09 pede `Role.Checkbox`/`Role.Tab`. |
| A3 | P1 | P | **Links e ícones sem nome**: "Termos de Uso" e "Política de Privacidade" no login aparecem como botões sem nome; aplicar os `cd_*` que já existem no 07 e não são usados (`cd_copy_pix`, `cd_share_whatsapp`, `cd_split_progress`, `cd_status_paid/pending`, `cd_calendar_add`, `cd_directions`, `cd_favorite_*`). | auth, pay, game, arena | 17 descrições prontas no 07 sem uso. |
| A4 | P1 | P | **FAB da rail** (tela larga) só com "+": rótulo visível "Criar jogo" quando houver espaço, ou descrição acessível. | `SCNavigation` | 09 §8 home: "FAB com rótulo visível, não só +". |

## T. Telas e fluxos

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| T1 | P1 | P | **Máscara do celular** mostra "(11) 9 8765-4321" (espaço a mais); padrão do 07: "(11) 98765-4321". | `PhoneMask` / `SCTextField` | Aparece no login, na edição de perfil e na exclusão de conta. |
| T2 | P1 | P | **Campo de cidade** do onboarding marcado como multilinha (Enter quebra linha) → linha única com ação "Buscar"; chip de data mostra "hoje" em minúscula → "Hoje". | onboarding, explore | Detalhes vistos na web. |
| T3 | P1 | M | **Jogos com vaga** (US-100/101, textos já no 07): seção na Home (`home_section_open_games`, `empty_open_games_*`) e aba "Jogos com vaga" na busca; ao criar jogo, opção "Abrir vagas para avulsos". | home, explore, create | Os jogos com `VAGAS_ABERTAS` da demo não aparecem em lugar nenhum; no expandido a Home fica vazia. |
| T4 | P1 | P | **Busca no expandido**: título desalinhado do conteúdo e cards com imagem enorme (cabem 2 por tela) → imagem com altura máxima e título na mesma margem. | `ExploreScreen`, `SCCourtCard` | Visto na web a 1280 px. |
| T5 | P1 | P | **Fotos da arena nos cards da busca**: quando a arena tem fotos (E9), o card usa a primeira no lugar do desenho. | `SCCourtCard` | Fecha o ciclo do E9. |
| T6 | P2 | P | **Reembolso que falhou** (`error_refund_failed_*`): estado no comprovante com ação de ajuda. | `ReceiptScreen` | Estado previsto no 07 §4 sem tela. |
| T7 | P2 | P | **Presença no perfil** (US-131, `profile_attendance`): "Presença: 9 de 10 jogos" a partir das avaliações (E15). | perfil | Fecha o ciclo do E15. |
| T8 | P2 | M | **Passar minha vaga** (US-111): atleta pago em jogo confirmado oferece a vaga à lista de espera; quando alguém paga, ele é reembolsado sem taxa (`WaitlistManager.substituir` já existe). | game detail | Regra pronta no domínio, sem tela. |
| T9 | P2 | M | **Cadastro da arena completo** (US-010/012): endereço com CEP, telefone, comodidades e horário de funcionamento; ao publicar, a arena aparece na busca. Hoje o cadastro não salva nada. | `arenaadmin/onboarding` | Fluxo de entrada da arena. |

## Q. Qualidade

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| Q1 | ✅ P1 | P | **Checagem das 4 plataformas num comando** (`./gradlew checkAll`): testes, build Android, framework iOS, desktop e web. | `build.gradle.kts` | Evita que uma plataforma quebre sem ninguém ver (a E9 subiu com um teste falhando). |
| Q2 | P2 | P | Trocar `LocalClipboardManager` (obsoleto) por `LocalClipboard`. | `FeatureUi.kt` e telas | Aviso de depreciação em todas as plataformas. |

---

## Fora deste plano (dependem de servidor, contas ou decisão de produto)

| Item | Origem | Motivo |
|---|---|---|
| Push remoto (servidor → aparelho) e "abriu uma vaga pra você" | US-046, US-110 | Precisa de FCM/APNs e servidor (12 §11). Os lembretes locais já funcionam. |
| Aprovar avulsos antes do pagamento | US-102 | Depende de T3 em produção e de regra de produto. |
| Jogos recorrentes e mensalistas | US-120, US-121 | Precisa de cobrança recorrente no gateway. |
| Check-in na chegada, reagendamento por chuva, transferir organização | US-140–142 | Fase 2 de operação; sem servidor não há sincronização. |
| WhatsApp oficial e "racha depois" | US-150, US-151 | Precisa da API do WhatsApp Business e do gateway. |
| Equipe/operadores da arena, conta de recebimento com KYC | US-014, 09 ficha 25 | Contas de arena no servidor e gateway de pagamentos. |
| Pré-visualização Open Graph do convite | 09 §6.4 | Gerada no servidor, por convite. |
| Torneios, nível, matchmaking, preço dinâmico | US-210–232 | Fase 3. |

## Andamento

- **Q1** ✅ `./gradlew checkAll`: testes + Android + iOS (simulador) + desktop + web (~3 min).
