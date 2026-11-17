# 11 · Backlog da auditoria de usabilidade, fluidez e telas faltantes

> Auditoria de 17/11/2026 sobre o código em `composeApp/` + `androidApp/`, as capturas em `build-screens/`
> e as especificações 07 (UX Writing), 09 (UX Design) e 10 (UI Design).
> Prioridade: **P0** = bloqueia publicação nas lojas ou deixa o app visivelmente lento; **P1** = usabilidade
> importante; **P2** = refinamento. Tamanho: **P** (≤ 2h), **M** (≤ 1 dia), **G** (> 1 dia).

---

## Visão geral

| Área | P0 | P1 | P2 |
|---|---|---|---|
| A. Fluidez e desempenho | 6 | 4 | 2 |
| B. Telas e fluxos que faltam | 7 | 4 | 2 |
| C. Ajustes de tela (layout, tamanho, bugs visuais) | 3 | 14 | 6 |
| D. Plataforma (Android / iOS / Web) | 3 | 3 | 1 |

---

## A. Fluidez e desempenho ("o app não está fluido")

As causas mais prováveis da sensação de lentidão, da mais para a menos impactante:

| # | P | Tam | Tarefa | Onde | Por quê |
|---|---|---|---|---|---|
| A1 | ✅ P0 | P | **Definir transições do `NavHost`.** Hoje não há `enterTransition`/`exitTransition`, e o padrão do navigation-compose é um *fade* de **700 ms** a cada troca de tela. Usar *slide + fade* de 250–300 ms para telas em pilha (`SCMotion.Medium2`), *fade-through* de ~200 ms entre abas e *slide-up* para fluxos modais (pagar, criar jogo, convite). | `navigation/AppNavHost.kt` (`NavHost(...)`) | A navegação inteira parece "arrastada". É a mudança de maior ganho com menor esforço. |
| A2 | ✅ P0 | P | **Testar desempenho só em build release.** No `debug` o Compose roda sem otimização e chega a ser 2–5× mais lento. Ligar `isMinifyEnabled = true` + `isShrinkResources = true` no `release`, adicionar `proguard-rules.pro` (kotlinx.serialization, Ktor, Koin) e criar o build type `benchmark` (release assinado com debug) para testar no aparelho. | `androidApp/build.gradle.kts` | Se você avaliou a fluidez no botão ▶ do Android Studio (debug), boa parte da lentidão vem daí. |
| A3 | P0 | M | **Baseline Profile + Startup Profile.** Criar o módulo `:baselineprofile` (plugin `androidx.baselineprofile`) com a jornada Splash → Home → Jogo → Pagar e gerar perfis para `:androidApp`. | novo módulo | Reduz o *jank* da primeira abertura de cada tela em 20–40% e acelera o cold start. |
| A4 | ✅ P0 | P | **Animar a entrada e saída da barra de navegação.** Hoje `showNavigation` liga e desliga a bottom bar de uma vez quando se entra em pagar/criar jogo, e o conteúdo "pula" 80 dp. Usar `AnimatedVisibility(slideInVertically + fadeIn)`. | `SCNavigation.kt` (`SCAdaptiveNavigation`) | Salto visual em toda entrada e saída de fluxo transacional. |
| A5 | ✅ P0 | P | **Listras animadas do rateio recompõem a cada frame.** `stripePhase` lê o `animateFloat(...).value` durante a composição. Mover a leitura para a fase de desenho (`drawWithCache`/`Modifier.drawBehind { phase() }` recebendo um lambda). | `SCSplitProgress.kt:112-117` | Com uma cota "em análise", o card inteiro recompõe a 60/120 fps. Isso aparece na Home, no Jogo e no Convite. |
| A6 | ✅ P0 | P | **Eliminar esperas artificiais do fake.** `FakePaymentGateway` confirma em 3 s e `checkNow()` espera mais 600 ms. Para a demo, use 1 s e mostre progresso; para testes manuais, use 0. | `FakePaymentGateway.kt:34`, `PaymentViewModel.kt:167` | O usuário de teste lê isso como "app lento". |
| A7 | P1 | P | **Feedback tátil (haptics)** ao: selecionar horário, copiar Pix, confirmar pagamento, "Fechou!" e trocar aba. Hoje há 0 usos de `LocalHapticFeedback`. | componentes SC* | Dá sensação de resposta imediata. |
| A8 | P1 | M | **Pull-to-refresh** (`PullToRefreshBox`) na Home, Meus jogos, Explorar, Avisos, Agenda e Repasses. Hoje não existe em nenhuma tela. | telas de lista | Padrão esperado. Sem ele, a pessoa não tem como "forçar" atualização. |
| A9 | P1 | M | **Transições compartilhadas** (`SharedTransitionLayout`): card do jogo → detalhe (título, badge e barra de rateio) e card da quadra → detalhe da arena (imagem). | Home/MyGames → GameDetail; Explore → Arena | Continuidade visual; é o que faz o app "parecer nativo e fluido". |
| A10 | P1 | P | **Animar mudanças de conteúdo**: `animateItem()` nos itens das `Lazy*` (Home, Agenda, Avisos) e `AnimatedContent` nos estados loading → conteúdo (hoje só há troca seca em 6 arquivos). | telas de lista | Itens aparecem e somem sem transição. |
| A11 | P2 | P | **Proteção contra toque duplo na navegação** (`nav.go` dispara duas telas iguais com toque rápido em rotas com argumentos diferentes). Ignorar `navigate` se o `lifecycle` da entrada atual não estiver `RESUMED`. | `AppNavigator.go` | Duplicação de telas na pilha. |
| A12 | P2 | P | Usar `LocalSCClock` em vez de `Clock.System.now()` dentro de composables. | `PaymentScreen.kt:581` | Consistência e testabilidade. |

---

## B. Telas e fluxos que faltam

### B.1 Splash e abertura

| # | P | Tam | Tarefa | Detalhe |
|---|---|---|---|---|
| B1 | ✅ P0 | M | **Splash Android (SplashScreen API).** | `androidx.core:core-splashscreen`. Tema `Theme.SportConnect.Starting` com `windowSplashScreenBackground` = `primary` dos tokens, ícone = símbolo do `logo.svg` convertido em *vector drawable* animado (AVD ≤ 1 s), `postSplashScreenTheme` com tema que respeite claro/escuro (hoje o tema é `@android:style/Theme.Material.Light.NoActionBar`, que pisca branco no modo escuro). `installSplashScreen().setKeepOnScreenCondition { !sessionReady }` enquanto lê sessão/onboarding do disco. |
| B2 | ✅ P0 | P | **Launch screen iOS.** | `Info.plist` tem `UILaunchScreen` vazio, então abre tela branca. Definir `UIColorName` (cor de fundo nos Assets, com variante escura) e `UIImageName` (símbolo do logo). |
| B3 | ✅ P0 | M | **Gate de sessão ("roteador de abertura").** | Hoje `onboardingDone` e a sessão vivem só em memória (`UiSessionStore`, `FakeAuthRepository` já inicia logado). Resultado: o onboarding aparece em **toda** abertura. Persistir com `multiplatform-settings` ou DataStore KMP e decidir a rota inicial: 1ª vez → Onboarding; com sessão → Home (ou Painel, se o último modo foi Arena); visitante → Home como visitante (09 §3, ficha 1: "visitante pode explorar sem login"). |
| B4 | ✅ P1 | P | **Loader na Web.** | O `index.html` fica em branco enquanto o Wasm baixa. Colocar logo + spinner em CSS no `<body>` e removê-lo no primeiro frame. |

### B.2 Login e cadastro

| # | P | Tam | Tarefa | Detalhe |
|---|---|---|---|---|
| B5 | ✅ P0 | M | **Completar a tela de login (`auth`)** conforme 09 ficha 2. | (a) "Receber pelo WhatsApp" chama o mesmo `sendCode` que SMS: passar o canal. (b) Estados de erro especificados e não implementados: `error_code_expired_*`, `error_too_many_attempts_*` (bloqueio de 15 min após 5 tentativas), `error_no_connection_*`; as strings já existem. (c) Campos bloqueados durante o loading. (d) **Autofill do código**: `ContentType.SmsOtpCode` (Android, SMS Retriever / User Consent API) e `textContentType = .oneTimeCode` (iOS). (e) Foco automático no OTP e `ImeAction.Done` no celular (hoje há **0** `ImeAction` no app inteiro). (f) ~~Máscara visual~~ — correção da auditoria: o `SCTextField` tipo `Phone` já aplicava a máscara. (g) Remover o `contentDescription` fixo "Código de 6 números" e usar string resource. |
| B6 | ✅ P0 | M | **Cadastro: termos e consentimentos (LGPD, CT-05, US-003).** | Hoje `auth_terms` é texto solto, sem link e sem aceite registrado. Transformar em texto com links ("Termos de uso" e "Política de privacidade") abrindo uma tela/WebView, e registrar `Consentimento` no `Usuario` ao concluir o cadastro. Opt-in separado e desmarcado para marketing. |
| B7 | P1 | M | **Cadastro: passo de perfil.** | Depois do nome: foto opcional (câmera/galeria) e esportes preferidos. O passo do nome hoje não tem voltar nem validação de tamanho mínimo (aceita 1 caractere). |
| B8 | P1 | M | **Login social (Google / Apple) atrás de flag.** | Previsto em 09 (`auth_google`, `auth_apple`; as strings já existem). Se Google entrar no iOS, **Sign in with Apple passa a ser obrigatório** (App Store 4.8). |
| B9 | P1 | P | **Login como sheet** no convite e no pagamento (09: "Também embutido como sheet"). | Hoje `GuestInvite → login` navega para a tela cheia `Route.Auth`. |

### B.3 Onboarding

| # | P | Tam | Tarefa | Detalhe |
|---|---|---|---|---|
| B10 | P1 | M | **Passos de esportes e localização** do onboarding (09 ficha 1). | Hoje o onboarding tem só o carrossel de 3 telas, e as 3 usam a **mesma ilustração** (`SCIllustration.Court`). Faltam a seleção de esportes (chips; strings `onboarding_sports_*` já existem) e a permissão de localização com finalidade explícita (`onboarding_location_*`), com *fallback* "Digitar minha cidade". Criar uma ilustração por passo. Permitir swipe (`HorizontalPager`): hoje só avança pelo botão. |

### B.4 Conta, privacidade e exclusão

| # | P | Tam | Tarefa | Detalhe |
|---|---|---|---|---|
| B11 | P0 | G | **Excluir conta** (obrigatório: App Store 5.1.1(v) e política do Google Play; LGPD art. 18). | Fluxo em `profile/privacy`: botão `profile_delete_account` → tela de consequências → diálogo `profile_delete_dialog_*` (strings já existem) → reautenticação por OTP → regra US-003 "com jogo ativo, a exclusão ocorre após conclusão/reembolso" → estado "exclusão agendada" → logout. Adicionar `AuthRepository.solicitarExclusao()` e o evento `conta_exclusao_solicitada`. Publicar também a **URL web de exclusão**, que o Play Console exige. |
| B12 | P0 | M | **Tela `profile/privacy` — Privacidade e dados** (09 ficha 16; hoje o item no Perfil não faz nada). | Consentimentos com switch (marketing, localização, push) disparando `consentimento_atualizado`; "Baixar meus dados" (`profile_download_data`, RNF-11: JSON/CSV); links para Termos e Política; "Excluir conta". |
| B13 | P0 | M | **Editar perfil.** | Não existe: o nome só é definido no 1º login. Tela com foto, nome/apelido, e-mail opcional e troca de celular (com novo OTP). Tocar no avatar do Perfil abre essa tela. |
| B14 | ✅ P0 | P | **Confirmar "Sair".** | Hoje o logout é imediato com um toque num texto vermelho. Usar um `SCDialog` e mover o botão para o fim, separado. |
| B15 | P1 | M | **Itens mortos do Perfil.** | "Meus esportes", "Cartões salvos", "Privacidade e dados" e "Falar com o suporte" têm `onClick = {}`. Criar as telas ou esconder os itens até existirem: item que não responde passa a impressão de app quebrado. |
| B16 | P2 | M | **Preferências**: tema (sistema/claro/escuro) e preferências de notificação por tipo. | `profile_notifications` hoje leva à lista de avisos, não às preferências. |
| B17 | P2 | P | **Comprovante**: "Compartilhar" hoje só copia texto. Usar o *share sheet* nativo (expect/actual) e, depois, PDF. | `ReceiptScreen` |

---

## C. Ajustes de tela (usabilidade, tamanho, bugs visuais)

| # | P | Tam | Tela | Problema → Ajuste |
|---|---|---|---|---|
| C1 | ✅ P0 | P | Pix pago | **Texto duplicado**: "6 de 12 cotas pagas" aparece 2× (título manual + rótulo interno do `SCSplitProgress`). Além disso, o título soma `paid + covered` e o componente usa só `paid`, então os números podem divergir. Remover o `Text` em `PaymentScreen.kt:582`. |
| C2 | ✅ P0 | P | Convite / "Fechou!" | **A arte decorativa (círculo de quadra) cruza o título e o corpo do texto** ("Rafael te chamou pra jogar", "a quadra está garantida pra"). Limitar a arte à metade direita **atrás** de uma área sem texto, ou reduzir a opacidade para ≤ 12% e cortar com `clipToBounds`. |
| C3 | ✅ P0 | P | Home expandida (tablet/desktop) | **O FAB da rail encosta no topo da janela** (sem respiro de status bar/insets). Aplicar `windowInsetsPadding(WindowInsets.safeDrawing)` + 8–12 dp na rail; usar o `ExtendedFloatingActionButton` "Criar jogo" com rótulo na rail. |
| C4 | P1 | M | Home (compacta) | **Cards muito altos (~270 dp)**: só cabem 2 por tela. Juntar o chip de esporte, o "Você organiza" e o status numa única linha (ícone do esporte no lugar do chip com texto); a barra de rateio já repete "8 de 12", então tirar do badge. Meta: ~180 dp por card. |
| C5 | P1 | P | Home | O botão "Buscar quadra" e o FAB "Criar jogo" competem no rodapé; o FAB cobre o fim da lista. Mover "Buscar quadra" para um atalho no topo (ou retirá-lo, já que existe a aba "Buscar"). |
| C6 | P1 | M | Explorar | **Imagem do card ocupa ~50% da altura**: só cabe 1 resultado por tela. Reduzir a mídia para 16:9 com altura máx. de 140 dp, ou layout horizontal (miniatura 96 dp à esquerda) como opção "lista compacta". Os cards também repetem a mesma ilustração e a mesma distância. |
| C7 | P1 | P | Explorar | O filtro "Esporte" deveria vir pré-preenchido com os esportes do onboarding; "Mais perto" parece link e deveria ser um seletor de ordenação (chip com menu). |
| C8 | P1 | P | Arena | O CTA fixo "Ver horários" é ambíguo (de qual quadra?). Cada quadra já tem horários e "Ver todos": trocar o CTA fixo por "Escolher quadra e horário" com rolagem até a lista, ou remover. A bottom nav + CTA fixo consomem ~180 dp. |
| C9 | P1 | P | Arena / Convite | "Ver todos" e "Ver regras" (botões de texto) ficam **recuados ~16 dp** em relação ao conteúdo (padding interno do TextButton). Compensar com `Modifier.offset(x = -12.dp)` ou usar a variante sem padding horizontal. |
| C10 | P1 | P | **Global: cor de seleção inconsistente** | Seleções aparecem em **pêssego** (`secondaryContainer`: duração no slot picker, "Dia/Semana" na agenda, período nos repasses, "hoje" no Explorar) e em **verde** (data, horário, aba). Escolher uma só para estado selecionado (recomendo `primaryContainer` verde) e deixar o pêssego para destaque/"Recomendado". |
| C11 | P1 | P | Escolher horário | A legenda ocupa 2 linhas; colocar em 1 linha rolável ou num ícone "?" (tooltip). Rolar automaticamente até o 1º horário disponível ≥ agora (hoje começa às 7h). |
| C12 | P1 | P | Criar jogo — passo 3 | Valor quebrado entre linhas ("R$ ⏎ 46,66"): usar espaço não separável (` `) em `formatBrl()`. Rótulos do stepper quebram em 2 linhas: mostrar só o rótulo do passo atual no compacto. |
| C13 | P1 | P | Jogo (detalhe) | Dois botões primários seguidos ("Lembrar quem falta" verde-escuro + "Convidar mais gente" verde-neon): manter 1 primário e o outro como `Tonal`. Status e esporte empilhados em 2 linhas: juntar em 1. |
| C14 | P1 | P | Pagar (Pix) | A tela abre um *bottom sheet* sobre uma tela cinza (scrim) sem conteúdo útil atrás: renderizar como tela normal. Em tablet/desktop, **mostrar o QR Code por padrão** (a pessoa paga com o celular). "Já paguei" (outlined) e "Pagar com cartão" (texto) ficam lado a lado com pesos visuais confusos: empilhar. |
| C15 | P1 | P | Pix pago | O conteúdo fica no topo com 50% de tela vazia: centralizar verticalmente e animar o check (escala + haptic). |
| C16 | P1 | P | Painel da arena | O nome da arena trunca ("Arena Vila M…"): usar `titleLarge` em até 2 linhas ou mostrar só o nome curto. Três cards "Atenção" quase idênticos: agrupar ("3 quadras sem horários a partir de 26/11 · Publicar"). "Publicar horários" sublinhado tem área de toque pequena: virar botão de 48 dp. |
| C17 | P1 | P | Agenda da arena | Os horários "Livre" não parecem tocáveis: dar uma ação explícita ("+ Bloquear" / "Reservar") ou um *affordance* de toque. Rolar até a hora atual ao abrir. |
| C18 | P2 | P | Cadastro da arena | CPF e CEP sem máscara, placeholder nem teclado numérico; CEP deveria preencher o endereço (ViaCEP). |
| C19 | P2 | P | Repasses | Os valores usam a fonte mono (Atkinson Mono), diferente do resto do app (Saira). Padronizar com `numericPrice`. |
| C20 | P2 | P | Avatares | O empilhamento corta as iniciais ("BL(", "CM("): aumentar o *overlap* negativo ou o diâmetro para 32 dp. |
| C21 | P2 | P | Global | Nenhum campo define `ImeAction`/`KeyboardActions` nem `imePadding()`: o teclado cobre o botão fixo do rodapé em Auth, Criar jogo e Cadastro da arena. |
| C22 | P2 | P | Global | Telas carregando mostram `Scaffold` vazio (ex.: `ReceiptScreen` com `p == null` dá `return`). Usar `SCSkeleton`. |
| C23 | P2 | P | Global | Perfil: "Versão 0.1.0" fixo no código; ler `versionName` via expect/actual. |

---

## D. Plataforma

| # | P | Tam | Tarefa |
|---|---|---|---|
| D1 | ✅ P0 | P | **Ícone do app Android**: `androidApp/src/main/res` só tem `values/`, então o app usa o ícone padrão. Gerar *adaptive icon* (foreground/background + monochrome para Android 13+) a partir de `docs/assets/app-icon.svg` e declarar `android:icon`/`android:roundIcon`. |
| D2 | ✅ P0 | P | **Ícone do app iOS**: `AppIcon.appiconset` só tem o `Contents.json` (sem imagem). Exportar 1024×1024 (variantes clara, escura e tinted no iOS 18+). |
| D3 | ✅ P0 | P | **Tema Android próprio** (`Theme.SportConnect`, DayNight, sem ActionBar, barras transparentes) no lugar de `@android:style/Theme.Material.Light.NoActionBar`. Evita o flash branco no modo escuro e prepara o splash (B1). |
| D4 | P1 | P | **Predictive back** (Android 14+): `android:enableOnBackInvokedCallback="true"` no manifest; com A1 feito, o navigation-compose já anima o gesto. |
| D5 | P1 | P | **Deep links reais**: as rotas declaram `https://sportconnect.app/{a,g,j}`, mas o `AndroidManifest` não tem `intent-filter` com `autoVerify` e o iOS não tem Associated Domains. O convite por link (fluxo principal do produto) não abre o app. |
| D6 | P1 | P | `android:localeConfig` + `values-pt-rBR` e `supportsRtl` revisado; `android:allowBackup` ligado guarda a sessão em backup: definir `dataExtractionRules` excluindo tokens. |
| D7 | P2 | P | Trocar `material-icons-extended` (biblioteca enorme e sem manutenção) pelos ícones usados copiados para `SCIcons`, ou Material Symbols via Compose Resources: reduz build e APK. |

---

## E. Especificado nos documentos e ainda não implementado

Levantado comparando 03 (PRD), 04 (métricas), 07 (textos) e 09 (inventário de telas) com o código.
Os **52 eventos** do 04 estão todos no código; **191 dos 791 textos** do 07 nunca são usados, e cada
grupo deles corresponde a uma funcionalidade que falta.

| # | Fase | Origem | O que falta |
|---|---|---|---|
| E1 | MVP | 09 ficha 16, US-003 | Tela Privacidade e dados (consentimentos, baixar dados, excluir conta) → B11/B12 |
| E2 | MVP | 09 ficha 1 | Onboarding com esportes e localização → B10 |
| E3 | MVP | 09 ficha 2 | Login social e erros de OTP → B5/B8 |
| E4 | MVP | 07 §4 | ~25 estados de erro sem tela (`error_payment_declined_*`, `error_pix_*`, `error_checkout_expired_*`, `error_hold_expired_*`, `error_card_expired_*`, `error_price_changed_*`, `error_session_expired_*`, `error_refund_failed_*`, `error_upload_failed_*`, `error_location_denied_*`, `error_payment_slow_*`) |
| E5 | P1 | US-032 | Formulário de cartão (`card_number/expiry/cvv/name_label`, `card_save_toggle`) |
| E6 | MVP | US-053 | Linha do tempo do reembolso (`refund_requested`, `refund_status_processing`, `refund_card_eta`, `refund_help`) |
| E7 | MVP | RNF-22, 09 §6.6 | Modo offline: `offline_banner`/`online_banner` + cache dos confirmados e comprovantes |
| E8 | MVP | US-046, US-061 | Push e WhatsApp: 16 textos `push_*` e 4 `wa_*` (lembretes 2h/30min, jogo amanhã, reembolso, nova reserva, repasse) |
| E9 | MVP | US-010, CT-01 | Arena: fotos e comodidades (`arena_detail_amenity_*`) |
| E10 | MVP/F2 | US-020, US-200 | Busca: filtros de preço/horário, ordenar por preço, mapa (`search_filter_*`, `search_sort_price`, `search_map_view`) |
| E11 | P1 | 09 fichas 24–25, US-015, US-063 | `arena-admin/reports` e `arena-admin/settings` (rotas não existem) |
| E12 | MVP | US-001 | Convite web: "Abrir no app" (`guest_open_in_app`) e oferta do app após pagar (`payment_app_offer_*`) |
| E13 | MVP | 07 | Cancelamento com motivo (`cancel_reason_label`, `cancel_done_body`) |
| E14 | F2 | US-110 | Lista de espera: domínio pronto (`WaitlistManager`), sem UI (`waitlist_*`) |
| E15 | F2 | US-130 | Avaliação pós-jogo (`rating_*`) |

---

## Andamento

**Sprint "Fluidez" — concluído em 17/11/2026:** A1 (transições do NavHost em `navigation/NavTransitions.kt`),
A2 (R8 no release + build type `benchmark`), A4 (barra/rail animadas), A5 (listras do rateio na fase de
desenho), A6 (fake de pagamento 3 s → 1 s), C1, C2, C3, D3 (tema `Theme.SportConnect` claro/escuro).
Extra encontrado no emulador: a Home e o Painel da arena desenhavam o título por baixo da status bar
(barras de topo próprias sem `WindowInsets.statusBars`) — corrigido.

**Sprint "Abertura e conta" — concluído em 17/11/2026:** B1 (SplashScreen API + saída animada), B2
(launch screen iOS: `LaunchBackground` + `LaunchLogo`), B3 (`AppPreferences` com multiplatform-settings:
onboarding e sessão salvos; instalação nova começa deslogada; criar jogo/pagar pedem login e voltam ao
destino), B4 (loader da Web), B5 (canal WhatsApp, código expira em 10 min, bloqueio de 15 min após 5 erros,
sem conexão, autofill `SmsOtpCode`/`PhoneNumber`, foco e ações do teclado, haptic no erro), B6 (links para
Termos/Política em `legal/{doc}`, aceite e opt-in de marketing registrados no usuário), B14 (diálogo ao sair;
Perfil de visitante com "Entrar com celular"), D1 (ícone adaptativo + monocromático + legado), D2 (AppIcon
1024 sem alfa). Testes novos em `AuthRepositoryTest`.
Pendentes do sprint: B9 (login como sheet) e a confirmação do autofill de SMS num aparelho real.

## Ordem sugerida (sprints)

1. ~~**Sprint "Fluidez"**~~ ✅ (2–3 dias): A1, A2, A4, A5, A6, C1, C2, C3, D3 → o app já "parece outro".
2. ~~**Sprint "Abertura e conta"**~~ ✅ (1 semana): B1, B2, B3, D1, D2, B5, B6, B14.
3. **Sprint "LGPD e lojas"** (1 semana): B11, B12, B13, B15, D5. **Sem B11 o app é rejeitado nas duas lojas.**
4. **Sprint "Polimento"**: A3, A7–A10, C4–C17, B7–B10.
5. Backlog: P2 restantes.
