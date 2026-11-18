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
| T. Telas e fluxos | — | 6 | 4 | T1–T10 |
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
| M1 | ✅ P0 | G | **Componente `SCMap`**: projeção Web Mercator, cache de tiles em memória (LRU), arrastar, zoom por pinça, duplo toque e roda do mouse, botões +/− (alvos de 48 dp), pinos com preço (`SCMapPin`), pino selecionado, atribuição "© OpenStreetMap" sempre visível, estado sem conexão. Testes da projeção e do enquadramento. | `core/designsystem/component/SCMap.kt` | Base de M2 e M3. Precisa ser igual nas 4 plataformas. |
| M2 | ✅ P1 | M | **Busca com mapa**: no compacto, botão "Ver no mapa" (`search_map_view`) alterna lista ↔ mapa, e tocar no pino mostra o card da quadra por cima; no expandido, lista à esquerda e mapa à direita, sincronizados (selecionar card destaca o pino e vice-versa). Os filtros valem para os dois. | `feature/explore/` | US-200 e o 09 ficha 4. No expandido a busca hoje mostra só 2 cards por tela (ver T4). |
| M3 | ✅ P1 | P | **Mini-mapa na página da arena** com o pino e "Como chegar"; tocar abre o app de mapas. | `feature/arena/ArenaDetailScreen.kt` | O endereço hoje é só texto. |

## W. Web

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| W1 | ✅ P0 | M | **URL e histórico do navegador**: a rota atual aparece na barra de endereço, "voltar/avançar" do navegador funcionam, e abrir `/j/{código}`, `/g/{id}` ou `/a/{id}` direto na web leva à tela certa (hoje sempre abre o onboarding/Home). | `wasmJsMain/main.kt`, `navigation/` | O fluxo principal da web (US-001: convidado abre o link e paga sem instalar) **não funciona** pela URL. |
| W2 | ✅ P1 | P | **"Usar minha localização" na web** via `navigator.geolocation` (hoje a web só aceita cidade digitada). | `core/platform/DeviceLocation.wasmJs.kt` | Paridade com Android/iOS; a busca por distância depende disso. |
| W3 | ✅ P1 | P | **PWA e cabeçalho**: `manifest.webmanifest` (nome, ícones, cores), `apple-touch-icon`, `theme-color` claro/escuro e descrição. | `wasmJsMain/resources/` | "Adicionar à tela inicial" e aparência correta no celular. |

## X. Plataformas

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| X1 | ✅ P1 | M | **Folha nativa de compartilhar** ("Mais opções", `invite_share_other`): Android `Intent.ACTION_SEND`, iOS `UIActivityViewController`, web `navigator.share` (cai para copiar), desktop copia. | `core/platform/Share*.kt`, convite | Prevista no 09 §6.5; hoje só existe WhatsApp e copiar. |
| X2 | ✅ P1 | P | **Salvar arquivo de verdade**: extrato CSV (arena) e "Baixar meus dados" (JSON) com o seletor de salvar do FileKit — download na web. | repasses, privacidade | Hoje os dois vão para a área de transferência, o que confunde (LGPD: "baixar meus dados"). |
| X3 | ✅ P1 | P | **Janela do desktop**: ícone, tamanho mínimo (360×640), lembrar tamanho e posição; pacote com versão do `gradle.properties` e ícones `.icns/.ico/.png`. | `desktopMain/main.kt`, `build.gradle.kts` | Hoje a janela não tem ícone e o instalador sai sempre como 1.0.0. |
| X4 | ✅ P2 | P | **iOS**: gesto de voltar pela borda em todas as telas de pilha e teclado com "Concluído" nos campos numéricos; conferir no simulador. | `iosMain/`, `MainViewController` | Expectativa básica de quem usa iPhone. |

## A. Acessibilidade (09 §8)

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| A1 | ✅ P1 | P | **Card de jogo lido por inteiro**: hoje o leitor diz "Esporte: Futebol society, Status: …" **sem o nome nem a data** do jogo. Incluir título, dia e hora, local e prazo, na ordem do 09 §8 (home). Saudação "E aí, Rafael!" é lida por último — corrigir a ordem. | `SCGameCard`, `HomeScreen` | O usuário de leitor de tela não sabe qual jogo é qual. |
| A2 | ✅ P1 | P | **Seleção com estado**: esportes do onboarding/Meus esportes e chips de filtro como caixa de seleção com "marcado/não marcado"; abas (barra e rail) e segmentos com "selecionado". | onboarding, explore, `SCNavigation` | Na web aparecem como "button" sem estado; o 09 pede `Role.Checkbox`/`Role.Tab`. |
| A3 | ✅ P1 | P | **Links e ícones sem nome**: "Termos de Uso" e "Política de Privacidade" no login aparecem como botões sem nome; aplicar os `cd_*` que já existem no 07 e não são usados (`cd_copy_pix`, `cd_share_whatsapp`, `cd_split_progress`, `cd_status_paid/pending`, `cd_calendar_add`, `cd_directions`, `cd_favorite_*`). | auth, pay, game, arena | 17 descrições prontas no 07 sem uso. |
| A4 | ✅ P1 | P | **FAB da rail** (tela larga) só com "+": rótulo visível "Criar jogo" quando houver espaço, ou descrição acessível. | `SCNavigation` | 09 §8 home: "FAB com rótulo visível, não só +". |

## T. Telas e fluxos

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| T1 | ✅ P1 | P | **Máscara do celular** mostra "(11) 9 8765-4321" (espaço a mais); padrão do 07: "(11) 98765-4321". | `PhoneMask` / `SCTextField` | Aparece no login, na edição de perfil e na exclusão de conta. |
| T2 | ✅ P1 | P | **Campo de cidade** do onboarding marcado como multilinha (Enter quebra linha) → linha única com ação "Buscar"; chip de data mostra "hoje" em minúscula → "Hoje". | onboarding, explore | Detalhes vistos na web. |
| T3 | ✅ P1 | M | **Jogos com vaga** (US-100/101, textos já no 07): seção na Home (`home_section_open_games`, `empty_open_games_*`) e aba "Jogos com vaga" na busca; ao criar jogo, opção "Abrir vagas para avulsos". | home, explore, create | Os jogos com `VAGAS_ABERTAS` da demo não aparecem em lugar nenhum; no expandido a Home fica vazia. |
| T4 | ✅ P1 | P | **Busca no expandido**: título desalinhado do conteúdo e cards com imagem enorme (cabem 2 por tela) → imagem com altura máxima e título na mesma margem. | `ExploreScreen`, `SCCourtCard` | Visto na web a 1280 px. |
| T5 | ✅ P1 | P | **Fotos da arena nos cards da busca**: quando a arena tem fotos (E9), o card usa a primeira no lugar do desenho. | `SCCourtCard` | Fecha o ciclo do E9. |
| T6 | ✅ P2 | P | **Reembolso que falhou** (`error_refund_failed_*`): estado no comprovante com ação de ajuda. | `ReceiptScreen` | Estado previsto no 07 §4 sem tela. |
| T7 | ✅ P2 | P | **Presença no perfil** (US-131, `profile_attendance`): "Presença: 9 de 10 jogos" a partir das avaliações (E15). | perfil | Fecha o ciclo do E15. |
| T8 | ✅ P2 | M | **Passar minha vaga** (US-111): atleta pago em jogo confirmado oferece a vaga à lista de espera; quando alguém paga, ele é reembolsado sem taxa (`WaitlistManager.substituir` já existe). | game detail | Regra pronta no domínio, sem tela. |
| T10 | ✅ P1 | P | **Saudação do visitante** mostra "E aí, !" (nome vazio) → "E aí!" / saudação de visitante. | `HomeScreen` | Visto na web sem login. |
| T9 | ✅ P2 | M | **Cadastro da arena completo** (US-010/012): endereço com CEP, telefone, comodidades e horário de funcionamento; ao publicar, a arena aparece na busca. Hoje o cadastro não salva nada. | `arenaadmin/onboarding` | Fluxo de entrada da arena. |

## Q. Qualidade

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| Q1 | ✅ P1 | P | **Checagem das 4 plataformas num comando** (`./gradlew checkAll`): testes, build Android, framework iOS, desktop e web. | `build.gradle.kts` | Evita que uma plataforma quebre sem ninguém ver (a E9 subiu com um teste falhando). |
| Q2 | ⏸ P2 | P | Trocar `LocalClipboardManager` (obsoleto) por `LocalClipboard`. | `FeatureUi.kt` e telas | Aviso de depreciação em todas as plataformas. **Investigado, não é um simples swap** — ver Andamento. |

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
- **W1** ✅ URL e histórico na web (`WebLinks`, `bindToBrowserNavigation`); links entram numa fila até o `NavHost` ter grafo — corrige também a abertura a frio em qualquer plataforma. Hospedagem: 12 §13.
- **M1** ✅ `SCMap` em `core/designsystem/map/` (projeção, `MapTileCache` com LRU e 6 downloads simultâneos, gestos, pinos, zoom, atribuição). Testes em `MapMathTest`.
- **M2** ✅ Busca com mapa: "Ver no mapa"/"Ver em lista" no celular (pino → card compacto da quadra mais barata da arena) e lista + mapa lado a lado no expandido (pino rola a lista). Verificado na web e no Android (no emulador, o relógio atrasado quebrava o TLS — não é defeito do app).
- **M3** ✅ Mini-mapa estático na página da arena (`SCMap(interactive = false)`), toque abre a rota.
- **W2** ✅ "Usar minha localização" na web (`navigator.geolocation`; negado → cidade digitada).
- **W3** ✅ `manifest.webmanifest`, ícones 192/512 e `apple-touch-icon`, `theme-color` claro/escuro, descrição; loader no tema do sistema.
- **X1** ✅ `rememberNativeShare()` (Android `ACTION_SEND`, iOS `UIActivityViewController`, web `navigator.share`; desktop copia). Usado em "Outros apps" do convite e no compartilhar da arena.
- **X2** ✅ `rememberFileExport` (diálogo "Salvar como" do FileKit; web baixa). Extrato `.csv` com BOM (acentos no Excel) e "Baixar meus dados" em `.json`. Verificado no Android (arquivo em Downloads).
- **X3** ✅ Janela do desktop com ícone, tamanho mínimo 360×600 e tamanho/posição lembrados; pacotes com a versão do `gradle.properties` (macOS: `1.0.<build>` enquanto o app for 0.x, exigência do `.dmg`) e ícones `.icns/.ico/.png`.
- **X4** ✅ Tocar fora fecha o teclado em todas as plataformas (no iPhone os teclados numéricos não têm tecla de fechar). O gesto de voltar pela borda no iOS já vem do Compose 1.12 (`UIKitBackGestureRecognizer`), sem código nosso.
- **A1** ✅ Cards de jogo e de quadra lidos numa frase completa (nome, data, local, esporte, estado, rateio, prazo; arena, quadra, distância) e horários como "Reservar das 21h às 22h, R$ 240,00". Barra de topo lida primeiro (`TopBarFirst`) no TalkBack/VoiceOver — **limitação:** a web do Compose ignora `traversalIndex` e mantém a ordem da árvore.
- **A2** ✅ Android/iOS já tinham `Role.Tab`/`Role.Checkbox` com estado; a web do Compose só gera `role=button` sem `aria-selected/checked`. `webSelectedLabel`/`webCheckedLabel` põem o estado no nome só na web: abas (barra, rail, menu), "Meus jogos", 6 segmentos, chips de esporte e filtro "Coberta".
- **A4** ✅ FAB "Criar jogo" do celular com nome acessível explícito (a web o expunha sem nome). Na rail, o FAB só com ícone segue o padrão M3 e já tem a descrição "Criar jogo".
- **A3** ✅ `SCInlineLinks` no lugar do `LinkAnnotation` (Termos/Política com nome e papel de botão; frase lida inteira); campo de busca usa o placeholder como nome. Os `cd_*` restantes do 07 descrevem botões que já têm texto visível equivalente — aplicá-los duplicaria a leitura, então ficaram de fora. **Limitação do Material 3:** o `ModalBottomSheet` tem o rótulo "Fechar planilha" na tradução do Compose (texto da biblioteca, sem parâmetro).
- **T1** ✅ Máscara do celular `(##) #####-####` (07): login, edição de perfil e exclusão de conta. Teste em `PhoneMaskTest`.
- **T2** ✅ "Hoje"/"Amanhã" com maiúscula no chip de data, no menu de datas e nos títulos dos avisos (`relativeDayLabel`); em frases continua "hoje". O "multiline" do campo de cidade era falso alarme: o campo já é de linha única, e a web do Compose expõe todo campo como multilinha.
- **T10** ✅ Visitante vê "E aí!" (`home_greeting_guest`).
- **T3** ✅ Jogos com vaga: `OpenGamesViewModel` (só jogos de outras pessoas), seção "Jogos com vaga perto de você" na Home (inclusive no estado vazio), aba "Jogos com vaga" na busca com filtro de esporte, e "Abrir vagas pra quem quiser jogar" na revisão do criar jogo. Verificado no Android até a tela de pagamento.
- **T4** ✅ `SCCourtCard`: imagem com altura máxima de 180dp (`BoxWithConstraints` + `maxWidth / 2.4f`) em vez de seguir a proporção 2.4:1 sem limite, o que passava de 250dp em telas largas. `ExploreScreen`: título da busca centralizado na mesma largura/margem do conteúdo (antes ficava colado à esquerda no `topBar` do `Scaffold`).
- **T5** ✅ Card da busca (`SCCourtCard`, variantes List e Map) usa a primeira foto da arena (`ArenaPhotoStore`, mesmo armazenamento do E9) no lugar do desenho da quadra, quando houver; sem fotos, mantém o desenho. Decorativo — o card já lê a frase completa (A1).
- **T6** ✅ Novo status `ESTORNO_FALHOU`: no `FakePaymentGateway`, o estorno no cartão (não no Pix) termina recusado pela conta de origem, como no real. O comprovante mostra `error_refund_failed_*` (fundo `errorContainer`) com a ação "Informar chave Pix", que abre um diálogo (`SCDialog` + `SCTextField`) validado pelo mesmo `isValidPixKey` do cadastro da arena (extraído para `core/domain/PixKey.kt`); ao confirmar, `PaymentGateway.informarChavePixReembolso` reencaminha o reembolso e a linha do tempo volta a "Em andamento" até concluir. Strings novas em `strings.xml` + `docs/07-ux-writing.md` (`refund_pix_key_field/submit`).
- **T7** ✅ `SocialStore.submitRating` agora guarda a presença marcada (antes era descartada, só o "já avaliou" ficava); `attendanceOf(userId)` conta ATTENDED/LATE sobre o total de marcações do usuário em todos os jogos. Perfil mostra "Presença: 9/10 dos jogos" abaixo do nome — some para quem nunca foi avaliado (sem dado falso). Removida a marca "Fase 2" de `profile_attendance` no 07/09: a E15 já existia, faltava só ler o dado.
- **T8** ✅ Fluxo completo de "Passar minha vaga" (RN-26), que só existia como função pura (`WaitlistManager.substituir`) sem nenhuma tela: `Cota.offeredForSubstitution` + `WaitlistManager.oferecer/cancelarOferta` (dono da cota, janela até T-1h); `ToggleSpotOfferUseCase` liga/desliga a oferta; `ClaimOfferedSpotUseCase` gera a cobrança do substituto (`SolicitacaoCobranca.substituicaoCotaId` novo, propagado até o webhook); `ReservationService.onWebhook` aplica `substituir` na confirmação e chama `gateway.estornar` sem taxa pro atleta original. No detalhe do jogo: barra "Passar minha vaga"/"Cancelar oferta" para quem já pagou, e "Vaga disponível, pagar e entrar" pra quem está na fila — reaproveita a tela de pagamento existente (`Route.GamePay.substituicaoCotaId`). Testes: `WaitlistManagerTest` (oferta exigida, só o dono oferece) e um teste de integração ponta a ponta em `FakeDataIntegrationTest` (oferece → paga → webhook → substituição + reembolso).
- **T9** ✅ O onboarding era puramente local (nenhum campo ia para a `Arena` de verdade); agora `ArenaOnboardingViewModel` carrega a arena do gestor (`observeArenaId`) e cada etapa salva nela: Info grava nome, telefone e endereço completo (rua/número/bairro/CEP) além de comodidades (chips com os mesmos rótulos do `amenityOf` da E9) e horário de funcionamento (texto livre); Bank grava a chave Pix (`Arena.chavePix`, novo). "Publicar" muda `Arena.status` para `ATIVA`, e `GetArenasNearbyUseCase` passou a **filtrar por status** — antes qualquer arena aparecia na busca, mesmo `EM_CADASTRO`. O campo "CPF do responsável" não tem onde persistir (a `Arena.documento` é o CNPJ dela, outro dado) e por isso nunca vem pré-preenchido. Teste novo: `ArenaOnboardingViewModelTest` (carrega dados reais, salva etapa, publica e reaparece na busca).
- **Q2** ⏸ Investigado (fontes do Compose Multiplatform 1.12.2 decompiladas): `LocalClipboard`/`Clipboard.setClipEntry` são `suspend` e `ClipEntry` é uma classe `expect/actual` cujo construtor recebe o objeto nativo do clipboard de cada plataforma (`Transferable` no desktop, `ClipData` no Android, outro tipo no iOS, `JsArray<ClipboardItem>` no wasmJs) — **não existe fábrica comum para "texto simples"**: o `ClipEntry.withPlainText(String)` só existe na actual do wasmJs, fora do contrato `expect`, então não compila a partir do código comum. Trocar exigiria uma função `expect`/`actual` própria (`core/platform/`, no padrão do X1) com 4 implementações — e eu não consigo rodar/testar iOS nem Web neste ambiente para validar essa troca sem risco de quebrar "copiar" (Pix, convite) silenciosamente em alguma plataforma. Fica para uma sessão com acesso a simulador iOS e ao build Web.
