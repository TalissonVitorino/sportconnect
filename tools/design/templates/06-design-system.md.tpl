# 06 — Design System · SportConnect ("Placar DS")

> Sistema de design do SportConnect para **Compose Multiplatform + Material 3** (Android, iOS, Web/Wasm, Desktop).
> Versão **1.0.0** · Prefixo de componentes: `SC` · Pacote: `br.com.sportconnect.designsystem`
> Identidade e racional visual: [`05-visual-design.md`](05-visual-design.md) · Tokens: [`design-tokens.json`](design-tokens.json) · Vitrine: [`style-guide.html`](style-guide.html)
> Os HEX deste documento são gerados por `tools/design/gen_tokens.py` (não editar à mão).

---

## 1. Arquitetura

```
primitivos (o que existe)          semânticos (para que serve)          componentes (onde é usado)
verde-quadra.40  #006D31   ──►     color.primary (light)        ──►     button.primary.container
grafite.98       #F5FBF6   ──►     color.surface (light)        ──►     card.container (via surfaceContainerLowest)
space.4          16dp      ──►     spacing.cardPadding          ──►     gameCard.padding
```

**Regras de referência**
1. Componentes **só** referenciam tokens semânticos (nunca primitivos, nunca HEX).
2. Tokens semânticos referenciam primitivos e variam por tema (light/dark).
3. Primitivos não variam por tema e não são expostos ao código de feature.

**No código (Compose):**

```kotlin
@Composable
fun SCTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) SCDarkColorScheme else SCLightColorScheme
    val ext = if (darkTheme) SCDarkExtendedColors else SCLightExtendedColors  // success, warning, sport, booking, timeSlot
    CompositionLocalProvider(
        LocalSCExtendedColors provides ext,
        LocalSCSpacing provides SCSpacing(),
        LocalSCTypographyExt provides SCTypographyExt(),   // numericScore, numericPrice, pixCode
    ) {
        MaterialTheme(colorScheme = colors, typography = SCTypography, shapes = SCShapes, content = content)
    }
}
object SCTheme {
    val colors @Composable get() = MaterialTheme.colorScheme
    val ext @Composable get() = LocalSCExtendedColors.current
    val spacing @Composable get() = LocalSCSpacing.current
}
```

---

## 2. Tokens de cor

### 2.1 Camada 1 — Primitivos

{{KEYS}}

{{TONAL}}

Escala neutra completa (0–100, incluindo tons de *surface container*):

{{NEUTRAL_FULL}}

### 2.2 Camada 2 — Semânticos: mapeamento exato para `ColorScheme` do Material 3

{{SCHEME}}

```kotlin
val SCLightColorScheme = lightColorScheme(
    primary = Color(0xFF006D31), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF27F37A), onPrimaryContainer = Color(0xFF002107),
    // … demais papéis conforme a tabela acima (gerar com tools/design/gen_tokens.py)
)
```

### 2.3 Camada 2 — Semânticos estendidos (fora do `ColorScheme`)

Expostos via `LocalSCExtendedColors` (padrão "custom colors" do M3).

{{EXT}}

**Esportes** (`SCTheme.ext.sport(SportType)` → `SportColors(container, onContainer, accent)`):

{{SPORTS}}

**Estados de reserva** (`SCTheme.ext.booking(BookingStatus)`):

{{BOOKING}}

**Estados de horário** (`SCTheme.ext.timeSlot(SlotState)`):

{{SLOTS}}

### 2.4 Camada 3 — Tokens de componente (amostra normativa)

| Token de componente | Referência semântica | Light | Dark |
|---|---|---|---|
| `button.primary.container` | `color.primary` | #006D31 | #00E46E |
| `button.primary.content` | `color.onPrimary` | #FFFFFF | #003916 |
| `button.tonal.container` | `color.primaryContainer` | #27F37A | #005323 |
| `button.tonal.content` | `color.onPrimaryContainer` | #002107 | #64FF93 |
| `button.secondary.border` | `color.outline` | #6F7A71 | #88948A |
| `button.destructive.container` | `color.error` | #BF0025 | #FFB4AB |
| `button.disabled.container` | `color.onSurface` @ 12% | — | — |
| `button.disabled.content` | `color.onSurface` @ 38% | — | — |
| `card.container` | `color.surfaceContainerLowest` | #FFFFFF | #0A100B |
| `card.border` | `color.outlineVariant` | #BDCAC0 | #3F4941 |
| `textField.container` | `color.surfaceContainerLowest` | #FFFFFF | #0A100B |
| `textField.border` | `color.outline` | #6F7A71 | #88948A |
| `textField.border.focused` | `color.primary` (2dp) | #006D31 | #00E46E |
| `splitProgress.paid` | `color.primary` | #006D31 | #00E46E |
| `splitProgress.pending` | `color.surfaceContainerHighest` + borda `outline` | #DEE4DF | #323632 |
| `splitProgress.covered` | `color.tertiary` (cota coberta pelo organizador) | #0058CA | #BDC2FF |
| `pixSheet.container` | `color.surfaceContainerLow` | #EFF5F0 | #191C19 |
| `pixSheet.qr.container` | `#FFFFFF` fixo (leitura do QR em qualquer tema) | #FFFFFF | #FFFFFF |
| `countdown.critical` | `color.error` (< 5 min) | #BF0025 | #FFB4AB |
| `bottomNav.indicator` | `color.primaryContainer` | #27F37A | #005323 |
| `snackbar.container` | `color.inverseSurface` | #2D312E | #DEE4DF |
| `focusRing` | `color.primary`, 2dp + 2dp offset | #006D31 | #00E46E |
| `scrim.modal` | `color.scrim` @ 32% | — | — |

A lista completa por componente está em [`design-tokens.json`](design-tokens.json) → `component`.

### 2.5 Contraste

Todos os pares acima foram verificados (WCAG 2.1 AA): light {{CONTRAST_LIGHT_COUNT}}, dark {{CONTRAST_DARK_COUNT}}. Tabela completa em [`05-visual-design.md` §4](05-visual-design.md#4-contraste-wcag-21).

---

## 3. Tipografia

Ver escala completa em [`05-visual-design.md` §5](05-visual-design.md#5-tipografia). Mapeamento Compose:

```kotlin
val SairaCondensed @Composable get() = FontFamily(
    Font(Res.font.saira_condensed_medium, FontWeight.Medium),
    Font(Res.font.saira_condensed_semibold, FontWeight.SemiBold),
    Font(Res.font.saira_condensed_bold, FontWeight.Bold),
    Font(Res.font.saira_condensed_extrabold, FontWeight.ExtraBold),
)
val Atkinson @Composable get() = FontFamily(
    Font(Res.font.atkinson_next_regular, FontWeight.Normal),
    Font(Res.font.atkinson_next_semibold, FontWeight.SemiBold),
    Font(Res.font.atkinson_next_bold, FontWeight.Bold),
)
val SCTypography @Composable get() = Typography(
    displayLarge  = TextStyle(fontFamily = SairaCondensed, fontWeight = FontWeight.Bold, fontSize = 60.sp, lineHeight = 64.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = SairaCondensed, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-0.25).sp),
    displaySmall  = TextStyle(fontFamily = SairaCondensed, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = SairaCondensed, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontFamily = SairaCondensed, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = SairaCondensed, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall    = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium    = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall     = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge    = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium   = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
```

Estilos estendidos: `numericScore` (Saira Condensed 700, 44/48, `fontFeatureSettings = "tnum"`), `numericPrice` (Saira Condensed 700, 28/32, `tnum`), `pixCode` (Atkinson Hyperlegible Mono 500, 13/20).

---

## 4. Espaçamento, forma, elevação, toque

### 4.1 Espaçamento (base 4dp)

| Primitivo | dp | Semântico | Uso |
|---|---|---|---|
| `space.1` | 4 | `spacing.xxs` | Ícone ↔ texto em badge |
| `space.2` | 8 | `spacing.xs` | Entre chips; ícone ↔ texto em botão |
| `space.3` | 12 | `spacing.sm` | Padding vertical de lista densa; gap da grade de horários |
| `space.4` | 16 | `spacing.md` | **Margem de tela (compact)**, padding de card |
| `space.5` | 20 | `spacing.mdPlus` | Padding de sheet |
| `space.6` | 24 | `spacing.lg` | Margem (medium+), padding horizontal de botão, entre seções |
| `space.8` | 32 | `spacing.xl` | Entre grupos de conteúdo |
| `space.10` | 40 | `spacing.xxl` | Topo de estado vazio |
| `space.12` | 48 | `spacing.touch` | Alvo de toque mínimo |
| `space.16` | 64 | `spacing.xxxl` | Respiro de hero |
| `space.20` | 80 | `spacing.bottomSafe` | Espaço para FAB/botão fixo inferior |

### 4.2 Raios

| Token | dp | Componentes |
|---|---|---|
| `radius.xs` | 4 | SCStatusBadge |
| `radius.sm` | 8 | SCSportChip |
| `radius.md` | 12 | SCButton, SCTextField, slot do SCTimeSlotPicker, SCSnackbar |
| `radius.lg` | 16 | Container do QR no SCPixPaymentSheet, SCSkeleton de card |
| `radius.xl` | 24 | SCGameCard, SCCourtCard |
| `radius.xxl` | 28 | SCPixPaymentSheet (topo), SCDialog |
| `radius.full` | — | Avatares, FAB, indicador da SCBottomNav |

```kotlin
val SCShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), small = RoundedCornerShape(8.dp), medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp),
)
```

### 4.3 Elevação

| Token | dp | Superfície tonal | Componentes |
|---|---|---|---|
| `elevation.level0` | 0 | surface | Tela, SCGameCard (usa borda) |
| `elevation.level1` | 1 | surfaceContainerLow | SCCourtCard em mapa |
| `elevation.level2` | 3 | surfaceContainer | SCTopBar rolada, SCBottomNav |
| `elevation.level3` | 6 | surfaceContainerHigh | FAB, SCSnackbar |
| `elevation.level4` | 8 | surfaceContainerHigh | Estado pressionado do nível 3 |
| `elevation.level5` | 12 | surfaceContainerHighest | SCPixPaymentSheet, SCDialog |

### 4.4 Toque e foco

- **Alvo mínimo 48 × 48dp** em todo elemento interativo (`Modifier.minimumInteractiveComponentSize()`); se o visual é menor (chip de 32dp, badge), a área de toque é expandida invisivelmente.
- 8dp mínimos entre alvos adjacentes.
- **Anel de foco** (teclado — Web/Desktop): 2dp `primary` com 2dp de afastamento, raio = raio do componente + 2dp. Nunca remover.
- Estados de interação M3 (state layers): hover 8%, foco 10%, pressionado 10%, arrastado 16% de `onSurface`/`on<Cor>`.

### 4.5 Motion

Durações e easings em [`05-visual-design.md` §12](05-visual-design.md#12-motion); objeto Compose `SCMotion` com `tween(durationMillis = SCMotion.Medium2, easing = SCMotion.Standard)`.

---

## 5. Catálogo de componentes

Convenções de todas as assinaturas: `modifier: Modifier = Modifier` é sempre o primeiro parâmetro opcional; textos visíveis vêm de `stringResource` na feature (o DS não contém copy fixa, exceto padrões acessíveis); callbacks com prefixo `on`.

Índice: [SCButton](#51-scbutton) · [SCTextField](#52-sctextfield) · [SCSportChip](#53-scsportchip) · [SCStatusBadge](#54-scstatusbadge) · [SCGameCard](#55-scgamecard) · [SCCourtCard](#56-sccourtcard) · [SCTimeSlotPicker](#57-sctimeslotpicker) · [SCSplitProgress](#58-scsplitprogress) · [SCParticipantRow](#59-scparticipantrow) · [SCPriceBreakdown](#510-scpricebreakdown) · [SCPixPaymentSheet](#511-scpixpaymentsheet) · [SCCountdownTimer](#512-sccountdowntimer) · [SCEmptyState](#513-scemptystate) · [SCTopBar](#514-sctopbar) · [SCBottomNav](#515-scbottomnav) · [SCAvatarStack](#516-scavatarstack) · [SCSnackbar](#517-scsnackbar) · [SCSkeleton](#518-scskeleton) · [SCDialog](#519-scdialog)

Tipos de domínio compartilhados:

```kotlin
enum class SportType { FutebolSociety, Futsal, Volei, VoleiPraia, Futevolei, BeachTennis, Padel, Tenis, Basquete, Handebol }
enum class BookingStatus { AguardandoPagamento, Confirmado, Cancelado, Expirado, Concluido }
enum class SlotState { Disponivel, Ocupado, PreReservado, Selecionado }
enum class ShareStatus { Paga, Pendente, Processando, CobertaOrganizador, Reembolsada }
@Immutable data class Money(val cents: Long)            // formatação pt-BR centralizada em Money.format()
```

---

### 5.1 SCButton

**Anatomia:** container (raio 12) · ícone opcional à esquerda (20dp) · rótulo (labelLarge) · indicador de carregamento (substitui o ícone; rótulo permanece).

| Variante | Container / Conteúdo | Quando usar |
|---|---|---|
| `Primary` | primary / onPrimary | **Uma** ação principal por tela: "Pagar minha cota", "Criar jogo", "Confirmar horário" |
| `Secondary` | transparente, borda 1dp outline / primary | Alternativa à principal: "Pagar com cartão" |
| `Tonal` | primaryContainer / onPrimaryContainer | Ação de destaque energético não-crítica: "Convidar galera" |
| `Text` | transparente / primary | Ações terciárias, em diálogos e cards: "Ver regras" |
| `Destructive` | error / onError | Ações irreversíveis: "Cancelar jogo e reembolsar" (sempre após SCDialog) |

| Tamanho | Altura | Padding H | Ícone | Uso |
|---|---|---|---|---|
| `Small` | 40dp (área de toque 48) | 16 | 18 | Em cards e linhas |
| `Medium` | 48dp | 24 | 20 | Padrão |
| `Large` | 56dp | 24 | 24 | CTA fixo inferior de telas de pagamento (largura total) |

**Estados:** padrão · hover (state layer 8%) · foco (anel 2dp) · pressionado (10%) · carregando (spinner 20dp, desabilita clique, mantém largura, `contentDescription` "Carregando") · desabilitado (container onSurface 12%, conteúdo 38%).

```kotlin
enum class SCButtonVariant { Primary, Secondary, Tonal, Text, Destructive }
enum class SCButtonSize { Small, Medium, Large }

@Composable
fun SCButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SCButtonVariant = SCButtonVariant.Primary,
    size: SCButtonSize = SCButtonSize.Medium,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
)
```

**Acessibilidade:** `Role.Button`; rótulo é o nome acessível; em `loading`, `stateDescription = "Carregando"` e foco preservado; nunca botão só com ícone (use IconButton com `contentDescription`).

| Faça | Não faça |
|---|---|
| Verbo + objeto: "Pagar R$ 20,00" | "OK", "Enviar", "Continuar" genéricos em pagamento |
| Um único Primary por tela | Dois Primary lado a lado |
| Destructive só após confirmação | Destructive como ação padrão de diálogo |

---

### 5.2 SCTextField

**Anatomia:** rótulo (sempre visível, acima do campo, labelLarge) · container 56dp (raio 12, borda 1dp outline) · ícone à esquerda opcional · texto (bodyLarge) · ícone/ação à direita (limpar, mostrar senha) · texto de apoio ou erro (bodySmall) · contador opcional.

**Variantes:** `Text`, `Money` (prefixo "R$", teclado numérico, máscara pt-BR), `Phone` (máscara (11) 9 9999-9999), `Cpf`, `Search` (raio full, ícone de busca), `Multiline`.

**Estados:** vazio · preenchido · foco (borda 2dp primary) · erro (borda 2dp error + ícone `error` + mensagem) · desabilitado (38%) · somente leitura (sem borda, container surfaceContainer).

```kotlin
@Composable
fun SCTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    type: SCTextFieldType = SCTextFieldType.Text,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,          // não-nulo => estado de erro
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
)
```

**Acessibilidade:** rótulo visível é o nome acessível (não usar só placeholder); erro anunciado via `semantics { error(errorText) }` e `liveRegion = Polite`; máscara não pode bloquear colar; `Money` lê "vinte reais" (valor formatado em `stateDescription`).

| Faça | Não faça |
|---|---|
| Erro que diz como corrigir: "Informe um valor a partir de R$ 5,00" | "Valor inválido" |
| Validar ao sair do campo | Mostrar erro enquanto o usuário ainda digita |

---

### 5.3 SCSportChip

**Anatomia:** container 32dp (raio 8) · ícone do esporte 20dp · rótulo (labelMedium) · check (quando selecionado em modo filtro).

| Variante | Visual |
|---|---|
| `Tag` (estático) | `sport.container` / `sport.onContainer` |
| `Filter` não selecionado | surfaceContainerLowest, borda 1dp outline, ícone em `sport.accent`, texto onSurfaceVariant |
| `Filter` selecionado | `sport.container`, borda 2dp `sport.accent`, ícone `check` + ícone do esporte |
| `Compact` | só ícone 24dp em círculo `sport.container` (com `contentDescription`) — para avatares de arena/mapa |

**Estados:** padrão · hover · foco · pressionado · selecionado · desabilitado.

```kotlin
@Composable
fun SCSportChip(
    sport: SportType,
    modifier: Modifier = Modifier,
    variant: SCSportChipVariant = SCSportChipVariant.Tag,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,       // null => Tag não interativo
    label: String = sport.displayName(), // "Vôlei de praia"
)
```

**Acessibilidade:** `Role.Checkbox` em `Filter` com `toggleableState`; área de toque 48dp; o nome do esporte é sempre texto (cor e ícone são reforço).

| Faça | Não faça |
|---|---|
| Usar as cores de esporte só para identificar esporte | Usar cor de esporte para status ("vermelho" de beach tennis ≠ erro) |
| Rolar horizontalmente a lista de filtros | Quebrar a lista de 10 esportes em várias linhas no compact |

---

### 5.4 SCStatusBadge

**Anatomia:** container 24dp (raio 4) · ícone 16dp · rótulo (labelMedium). Variante `Large` (32dp, ícone 20) para o topo do detalhe do jogo.

| Status | Cores | Ícone | Rótulo |
|---|---|---|---|
| AguardandoPagamento | warningContainer / onWarningContainer | `hourglass_top` | "Aguardando pagamento" (+ "· 8 de 12" opcional) |
| Confirmado | successContainer / onSuccessContainer | `check_circle` | "Confirmado" |
| Cancelado | errorContainer / onErrorContainer | `cancel` | "Cancelado" |
| Expirado | surfaceContainerHighest / onSurfaceVariant | `timer_off` | "Expirado" |
| Concluido | tertiaryContainer / onTertiaryContainer | `sports_score` | "Concluído" |

```kotlin
@Composable
fun SCStatusBadge(
    status: BookingStatus,
    modifier: Modifier = Modifier,
    size: SCBadgeSize = SCBadgeSize.Medium,
    detail: String? = null,       // ex.: "8 de 12 pagos"
)
```

> **Copy:** o DS fixa cor, ícone e nome técnico do estado; o rótulo exibido vem de `07-ux-writing.md` (ex.: em cards o estado `AguardandoPagamento` aparece como "Pré-reserva · 8 de 12 pagas", chave `home_card_status_pending`). Termos proibidos lá ("garantido", "hold", "split") também valem aqui.

**Acessibilidade:** não interativo; `contentDescription = "Status: Aguardando pagamento, 8 de 12 pagos"`; mudança de status anunciada com `liveRegion = Polite` quando visível.

| Faça | Não faça |
|---|---|
| Sempre ícone + texto | Bolinha colorida sem texto |
| Mesmo rótulo em app, push e e-mail | Sinônimos ("Pendente", "Em aberto") para o mesmo estado |

---

### 5.5 SCGameCard

**Anatomia (de cima para baixo):** faixa lateral 4dp em `sport.accent` · linha 1: SCSportChip (Tag) + SCStatusBadge · título do jogo (titleMedium) · arena + quadra (bodyMedium, ícone `stadium`) · data/hora (bodyMedium, ícone `schedule`, "Sáb, 23/11 · 19h–20h") · SCSplitProgress (variante `Compact`) · rodapé: SCAvatarStack + valor da cota (numericPrice menor, 20sp) + SCCountdownTimer (se aguardando).

**Variantes:** `Organizer` (mostra quem falta pagar + ação "Cobrar galera"), `Participant` (mostra "Sua cota: Paga/Pendente" + ação "Pagar R$ 20,00"), `Open` (vaga aberta, Fase 2: "3 vagas · R$ 20,00").

**Estados:** padrão · pressionado · foco · carregando (SCSkeleton) · passado (opacidade de conteúdo secundário 72%, status Concluído).

Container: `surfaceContainerLowest`, borda 1dp `outlineVariant`, raio 24, padding 16.

```kotlin
@Composable
fun SCGameCard(
    game: GameCardUi,                 // sport, title, arenaName, courtName, start, end, status, paidCount, totalShares, shareValue, participants, holdExpiresAt
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SCGameCardVariant = SCGameCardVariant.Participant,
    primaryAction: SCCardAction? = null,   // ex.: Pagar minha cota
)
```

**Acessibilidade:** card inteiro clicável com `mergeDescendants`; leitura em ordem: "Futebol society. Racha de sábado. Arena Vila Madalena, Quadra 2. Sábado, 23 de novembro, 19 às 20 horas. Aguardando pagamento, 8 de 12 pagos. Sua cota: 20 reais, pendente." Ação primária é alvo separado e focável.

| Faça | Não faça |
|---|---|
| Valor da cota e status sempre visíveis sem expandir | Esconder o prazo em "ver detalhes" |
| Faixa de esporte + chip | Fundo inteiro do card na cor do esporte |

---

### 5.6 SCCourtCard

**Anatomia:** foto 16:9 (raio 24 superior; *placeholder* com linhas de quadra do esporte) · selo de distância sobre a foto ("2,4 km", scrim) · nome da arena (titleMedium) · quadra/tipo de piso (bodyMedium: "Quadra 2 · Grama sintética · Coberta") · linha de SCSportChip `Compact` (esportes aceitos) · preço "a partir de R$ 180/h" (numericPrice 20sp) · próximos horários livres (3 mini-slots).

**Variantes:** `List` (vertical completo), `Horizontal` (thumbnail 96dp à esquerda — buscas), `Map` (compacto sobre o mapa, elevação 1), `Owner` (painel da arena: ocupação do dia em barra + ação "Editar agenda").

**Estados:** padrão · pressionado · foco · indisponível (sem horários: texto "Sem horários livres hoje") · carregando.

```kotlin
@Composable
fun SCCourtCard(
    court: CourtCardUi,               // arenaName, courtName, surface, covered, sports, photoUrl, priceFrom, distanceKm, nextSlots
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SCCourtCardVariant = SCCourtCardVariant.List,
    onSlotClick: ((SlotUi) -> Unit)? = null,
)
```

**Acessibilidade:** foto decorativa (`contentDescription = null`) se o nome está no card; mini-slots são alvos de 48dp próprios; preço lido como "a partir de 180 reais por hora".

| Faça | Não faça |
|---|---|
| Foto real da arena | Foto de banco com atletas profissionais |
| Mostrar esportes aceitos com ícone + nome no detalhe | Listar só ícones sem nome no detalhe |

---

### 5.7 SCTimeSlotPicker

**Anatomia:** seletor de data (faixa horizontal de dias, "Sáb 23") · legenda dos 4 estados · grade de slots (2 colunas compact / 4 medium / 6 expanded; gap 12) · cada slot 56dp: hora início–fim (titleSmall, "19:00–20:00"), preço (bodySmall) e ícone de estado · resumo da seleção fixo no rodapé.

| Estado do slot | Visual | Interação |
|---|---|---|
| Disponível | surfaceContainerLowest, borda 1dp outline, texto onSurface | Toque seleciona |
| Ocupado | surfaceContainerHigh + hachura 45°, texto onSurfaceVariant, rótulo "Ocupado" | Não selecionável (anunciado como indisponível) |
| Pré-reservado | warningContainer, borda 1dp warning, ícone `hourglass_top`, "Libera às 18:40" | Não selecionável; mostra quando libera |
| Selecionado | primary, texto onPrimary, ícone `check` | Toque desseleciona; permite selecionar slots contíguos (1h–3h) |

```kotlin
@Composable
fun SCTimeSlotPicker(
    days: List<LocalDate>,
    selectedDay: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    slots: List<SlotUi>,              // start, end, price, state, holdReleasesAt
    selection: Set<SlotId>,
    onSelectionChange: (Set<SlotId>) -> Unit,
    modifier: Modifier = Modifier,
    maxContiguous: Int = 3,
    columns: Int = SCTimeSlotPickerDefaults.columns(windowSizeClass),
)
```

**Acessibilidade:** grade com `collectionInfo`/`collectionItemInfo`; cada slot `Role.Checkbox` com `stateDescription` ("Disponível, 180 reais", "Ocupado", "Pré-reservado, libera às 18 e 40"); slots ocupados permanecem focáveis por teclado para leitura (`disabled()`), mas não acionáveis; navegação por setas na Web/Desktop.

| Faça | Não faça |
|---|---|
| Estado com cor + ícone/hachura + texto | Diferenciar ocupado/livre só por cor |
| Seleção contígua com feedback de erro claro | Permitir seleção de horários não contíguos |

---

### 5.8 SCSplitProgress

**O componente-assinatura.** Representa o rateio como **segmentos — uma cota por segmento** (como as cadeiras de um banco de reservas), não uma barra contínua genérica.

**Anatomia:** rótulo numérico grande "8 de 12 pagos" (numericScore 44sp em `Full`; titleSmall em `Compact`) · valor arrecadado "R$ 160 de R$ 240" · trilho de N segmentos (10dp de altura, gap 3dp, raio 2dp) · legenda opcional (Pagas · Cobertas · Pendentes).

| Segmento | Cor |
|---|---|
| Paga | primary |
| Processando (Pix enviado, aguardando confirmação) | primary @ 40% + listras animadas (sem animação com reduzir movimento) |
| Coberta pelo organizador | tertiary |
| Pendente | surfaceContainerHighest + borda 1dp outline |
| Reembolsada | transparente, borda tracejada outline |

**Variantes:** `Full` (detalhe do jogo), `Compact` (cards: sem legenda, 6dp), `Continuous` (automático se N > 30 — barra contínua com marcadores a cada 10).

**Estados:** em andamento · completa (todos os segmentos `primary` → coreografia "Fechou!" e troca de badge) · expirada (segmentos pendentes ficam com hachura e opacidade 38%).

```kotlin
@Composable
fun SCSplitProgress(
    totalShares: Int,
    paid: Int,
    modifier: Modifier = Modifier,
    processing: Int = 0,
    covered: Int = 0,
    collected: Money? = null,
    total: Money? = null,
    variant: SCSplitProgressVariant = SCSplitProgressVariant.Full,
    showLegend: Boolean = variant == SCSplitProgressVariant.Full,
    onCompleteAnimationEnd: (() -> Unit)? = null,
)
```

**Acessibilidade:** `progressBarRangeInfo(paid/total)`; `contentDescription = "8 de 12 cotas pagas, 160 de 240 reais arrecadados"`; o texto numérico é sempre visível; segmento processando tem padrão, não só cor.

| Faça | Não faça |
|---|---|
| Número absoluto ("8 de 12") ao lado da barra | Só porcentagem ("67%") |
| Diferenciar coberta pelo organizador de paga | Contar cota coberta como paga pelo atleta |

---

### 5.9 SCParticipantRow

**Anatomia:** avatar 40dp (foto ou iniciais sobre `sport.container`) · nome (bodyLarge) + papel ("Organizador") · status da cota (ícone + texto bodySmall, strings de `07-ux-writing.md`: "Pagou" + hora, "Falta pagar", "Pagamento em análise", "Coberta pelo organizador", "Reembolsada") · ação à direita (organizador: "Lembrar" via `notifications`; atleta: nada) · altura 64dp.

**Variantes:** `Default`, `Self` ("Você", fundo surfaceContainerLow, ação "Pagar"), `Invite` (vaga vazia: avatar tracejado + "Vaga aberta" + "Convidar").

```kotlin
@Composable
fun SCParticipantRow(
    name: String,
    shareStatus: ShareStatus,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    role: String? = null,
    paidAt: Instant? = null,
    isSelf: Boolean = false,
    action: SCRowAction? = null,      // Lembrar / Pagar / Convidar
)
```

**Acessibilidade:** linha mesclada para leitura ("Ana Souza, cota paga às 14 e 32"); ação é alvo separado de 48dp com rótulo explícito ("Lembrar Ana de pagar").

| Faça | Não faça |
|---|---|
| Ordenar: você → pendentes → pagos | Ordem alfabética escondendo pendências |
| Lembrete com limite (1 a cada 2h) | Botão de "cobrar" sem limite (expõe o atleta) |

---

### 5.10 SCPriceBreakdown

**Anatomia:** linhas chave-valor (bodyMedium, valores tabulares alinhados à direita): "Quadra (1h)" R$ 240,00 · "Dividido por 12" · "Sua cota" R$ 20,00 · "Taxa de conveniência" R$ 1,50 (com ícone `info` → explicação) · divisor linha de quadra 2dp · **Total a pagar** (titleMedium + numericPrice 28sp).

**Variantes:** `Payer` (visão do atleta), `Organizer` (inclui regra de não pagamento escolhida e cenário "Se faltar alguém, você cobre até R$ 80,00"), `Arena` (valor bruto, taxa da plataforma, repasse líquido e data prevista).

```kotlin
@Composable
fun SCPriceBreakdown(
    lines: List<PriceLine>,           // label, value: Money, hint: String?, emphasis
    total: PriceLine,
    modifier: Modifier = Modifier,
    variant: SCPriceBreakdownVariant = SCPriceBreakdownVariant.Payer,
    footnote: String? = null,
)
```

**Acessibilidade:** cada linha lida como "Taxa de conveniência: 1 real e 50 centavos"; dica `info` abre tooltip/bottom sheet acessível por teclado; total em `heading()`.

| Faça | Não faça |
|---|---|
| Mostrar taxa antes do pagamento | Revelar taxa só na tela do Pix |
| Centavos sempre (R$ 20,00) | Arredondar visualmente ("R$ 20") em telas de pagamento |

---

### 5.11 SCPixPaymentSheet

**Anatomia (bottom sheet modal, raio 28 no topo, `surfaceContainerLow`):** alça · título "Pague com Pix" (titleLarge) · valor (displaySmall, "R$ 21,50") · SCCountdownTimer ("O código expira em 14:59") · QR code 200dp sobre **fundo branco fixo** com padding 16 e raio 16 (em qualquer tema) · botão Primary Large "Copiar código Pix" (ícone `content_copy`) · código copia-e-cola truncado em `pixCode` (2 linhas, expandível) · passos curtos ("1. Abra o app do seu banco · 2. Escolha Pix copia e cola · 3. Cole e confirme") · status ao vivo: "Aguardando pagamento…" → "Pagamento recebido" · ação Text "Pagar com cartão".

**Estados:** aguardando (padrão) · copiado (botão vira "Código copiado" com ícone `check` por 2s + snackbar) · processando (spinner + "Confirmando com o banco…") · pago (ícone `check_circle` success, "Pagamento recebido", fecha em 2s ou ação "Ver jogo") · expirado (QR com opacidade 12% + "Código expirado" + Primary "Gerar novo código") · erro (mensagem + "Tentar de novo").

```kotlin
@Composable
fun SCPixPaymentSheet(
    amount: Money,
    pixCopyPaste: String,
    qrBitmap: ImageBitmap,
    expiresAt: Instant,
    state: PixPaymentState,           // Waiting, Processing, Paid, Expired, Error(message)
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onPayWithCard: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
)
```

**Acessibilidade:** QR com `contentDescription = "QR code Pix de 21 reais e 50 centavos"`; botão de copiar é o **primeiro foco** (no celular, o QR não é útil para quem está no mesmo aparelho); mudança de estado anunciada (`liveRegion = Assertive` só em "Pagamento recebido" e "Código expirado"); contador anuncia apenas nos marcos 5 min e 1 min (não a cada segundo); prazo pode ser renovado (WCAG 2.2.1 — "Gerar novo código").

| Faça | Não faça |
|---|---|
| "Copiar código Pix" como ação principal no celular | QR como única opção |
| QR em fundo branco também no tema escuro | QR invertido (leitores de banco falham) |

---

### 5.12 SCCountdownTimer

**Anatomia:** ícone `timer` · tempo restante em `numericScore` (variante Large) ou labelLarge tabular (Inline) · rótulo de contexto ("para fechar o jogo", "para o código expirar").

| Faixa | Cor | Formato |
|---|---|---|
| > 1h | onSurfaceVariant | "3h 20min" |
| 5 min – 1h | warning (texto) / warningContainer (Chip) | "42:10" |
| < 5 min | error | "04:59" |
| Esgotado | onSurfaceVariant | "Prazo encerrado" |

**Variantes:** `Inline` (texto em cards), `Chip` (container, ex.: no topo do detalhe), `Large` (tela do Pix e do organizador).

```kotlin
@Composable
fun SCCountdownTimer(
    deadline: Instant,
    modifier: Modifier = Modifier,
    variant: SCCountdownVariant = SCCountdownVariant.Inline,
    label: String? = null,
    clock: Clock = Clock.System,
    onExpired: () -> Unit = {},
)
```

**Acessibilidade:** `contentDescription` em linguagem natural ("Faltam 42 minutos para fechar o jogo"), atualizado por minuto (não por segundo) para leitores de tela; dígitos tabulares evitam "tremida" visual; cor nunca é o único sinal (formato e ícone mudam).

| Faça | Não faça |
|---|---|
| Mostrar horário absoluto junto em prazos longos ("até 18:40") | Contagem regressiva de dias em segundos |
| Pausar atualizações fora da tela | Recompor a tela inteira a cada segundo |

---

### 5.13 SCEmptyState

**Anatomia (centralizado):** ilustração "linha de cal" 160dp · título (headlineSmall) · descrição (bodyMedium, ≤ 2 linhas) · SCButton Primary (ação) · ação Text secundária opcional.

**Variantes:** `FirstUse` ("Nenhum jogo ainda" → "Criar meu primeiro jogo"), `NoResults` (busca/filtro → "Limpar filtros"), `Error` (falha de rede → "Tentar de novo"; ilustração de rede caída), `Offline`.

```kotlin
@Composable
fun SCEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    illustration: SCIllustration = SCIllustration.Court,
    primaryAction: SCAction? = null,
    secondaryAction: SCAction? = null,
)
```

**Acessibilidade:** ilustração decorativa; título com `heading()`; foco inicial no título quando a tela carrega vazia.

| Faça | Não faça |
|---|---|
| Convidar à ação: "Crie um jogo e mande o link no grupo" | "Ops! Nada por aqui :(" |

---

### 5.14 SCTopBar

**Anatomia:** navegação (voltar `arrow_back` / fechar `close`) · título (titleLarge; em `Large`, headlineMedium Saira) · até 2 ações (IconButton 48dp) + menu de overflow.

**Variantes:** `Small` (padrão), `CenterAligned` (raiz de abas), `Large` (detalhe do jogo/arena — colapsa ao rolar), `Contextual` (seleção na agenda da arena: "3 horários selecionados").

**Estados:** no topo (surface, sem elevação) · rolado (surfaceContainer, nível 2).

```kotlin
@Composable
fun SCTopBar(
    title: String,
    modifier: Modifier = Modifier,
    variant: SCTopBarVariant = SCTopBarVariant.Small,
    navigation: SCTopBarNavigation? = SCTopBarNavigation.Back,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
)
```

**Acessibilidade:** título com `heading()`; ícones com `contentDescription` ("Voltar", "Compartilhar convite"); ordem de foco: navegação → título → ações.

---

### 5.15 SCBottomNav

**Anatomia:** container 80dp surfaceContainer · 3–5 destinos: ícone 24dp (FILL 1 quando ativo) dentro do indicador `primaryContainer` (64×32, raio full) + rótulo labelSmall sempre visível · badge numérico opcional (error).

**Destinos padrão (atleta/organizador):** Início (`home`), Jogos (`sports_score`), Buscar (`search`), Perfil (`person`). **Arena:** Agenda (`calendar_month`), Quadras (`stadium`), Financeiro (`payments`), Perfil.
Em medium/expanded vira Navigation Rail; em large, Navigation Drawer.

```kotlin
@Composable
fun SCBottomNav(
    items: List<SCNavItem>,           // route, label, icon, badgeCount
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
)
```

**Acessibilidade:** `Role.Tab` com `selected`; rótulos sempre visíveis (não só ícone); badge lido ("Jogos, 2 pendências").

| Faça | Não faça |
|---|---|
| Máximo 4 destinos no app do atleta | Colocar "Pagar" como destino de navegação |

---

### 5.16 SCAvatarStack

**Anatomia:** até N avatares (padrão 4) 32dp sobrepostos −8dp, borda 2dp `surfaceContainerLowest` (separa do vizinho) · contador final "+8" em círculo surfaceContainerHighest (labelMedium) · opcional: anel `primary` 2dp em avatares que já pagaram (modo `PaymentAware`).

```kotlin
@Composable
fun SCAvatarStack(
    people: List<AvatarUi>,           // name, photoUrl, paid
    modifier: Modifier = Modifier,
    max: Int = 4,
    size: Dp = 32.dp,
    paymentAware: Boolean = false,
    onClick: (() -> Unit)? = null,
)
```

**Acessibilidade:** um único nó: "Ana, Bruno, Carla, Diego e mais 8 participantes"; em `paymentAware`, o anel é reforço — o número de pagos está no SCSplitProgress ao lado.

---

### 5.17 SCSnackbar / Toast

**Anatomia:** container inverseSurface (raio 12, elevação 3) · ícone opcional (success/error em tom inverso) · mensagem (bodyMedium, inverseOnSurface, até 2 linhas) · ação opcional (inversePrimary, labelLarge) · fechar opcional.

**Variantes:** `Info`, `Success` ("Código Pix copiado"), `Error` ("Não foi possível copiar. Toque e segure o código para copiar."), `Undo` ("Convite removido · Desfazer").

**Duração:** 4s (curta), 10s (com ação), indefinida para erros que exigem ação. Sobe acima do botão fixo inferior.

```kotlin
@Composable
fun SCSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier)

suspend fun SnackbarHostState.showSC(
    message: String,
    kind: SCSnackbarKind = SCSnackbarKind.Info,
    actionLabel: String? = null,
    duration: SnackbarDuration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short,
): SnackbarResult
```

**Acessibilidade:** `liveRegion = Polite`; com ação, duração estendida e ação alcançável por teclado; nunca a única forma de informar sucesso de pagamento (a tela também muda).

---

### 5.18 SCSkeleton

**Anatomia:** blocos com a forma exata do conteúdo final (`SCSkeleton.Line`, `.Circle`, `.Box`) em surfaceContainerHigh, com shimmer de 1.200ms (gradiente para surfaceContainerHighest). Presets: `GameCardSkeleton`, `CourtCardSkeleton`, `ParticipantRowSkeleton`, `TimeSlotGridSkeleton`.

```kotlin
@Composable
fun SCSkeleton(
    shape: SCSkeletonShape,           // Line(widthFraction), Circle(size), Box(height, radius)
    modifier: Modifier = Modifier,
    animate: Boolean = !LocalReducedMotion.current,
)
```

**Acessibilidade:** container pai recebe `contentDescription = "Carregando jogos"` e os blocos são invisíveis para leitores (`clearAndSetSemantics`); shimmer desativado com reduzir movimento; após 10s sem dados, trocar por SCEmptyState `Error`.

| Faça | Não faça |
|---|---|
| Skeleton para listas e cards | Skeleton para valores de pagamento (use spinner + texto "Calculando…") |

---

### 5.19 SCDialog

**Anatomia:** scrim 32% · container surfaceContainerHigh (raio 28, nível 5, largura 280–560dp) · ícone opcional 24dp (centralizado, cor do tipo) · título (titleLarge) · corpo (bodyMedium) · conteúdo opcional (ex.: SCPriceBreakdown do reembolso) · ações à direita: Text (dispensar) + Primary/Destructive (confirmar).

**Variantes:** `Confirm`, `Destructive` ("Cancelar o jogo? Os 8 pagamentos serão reembolsados em até 1 dia útil." → "Manter jogo" / "Cancelar e reembolsar"), `Info`, `FullScreen` (compact, para formulários longos).

```kotlin
@Composable
fun SCDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirm: SCAction,
    modifier: Modifier = Modifier,
    dismiss: SCAction? = null,
    icon: ImageVector? = null,
    variant: SCDialogVariant = SCDialogVariant.Confirm,
    body: String? = null,
    content: (@Composable () -> Unit)? = null,
)
```

**Acessibilidade:** `Role.Dialog` / `paneTitle`; foco inicial na ação **segura** (dispensar) em `Destructive`; Esc/voltar fecha; foco preso no diálogo e devolvido ao gatilho ao fechar.

| Faça | Não faça |
|---|---|
| Botões com o verbo exato da consequência | "Sim" / "Não" |
| Explicar o que acontece com o dinheiro | Diálogo destrutivo sem citar valores |

---

## 6. Padrões compostos (referência rápida)

| Padrão | Composição |
|---|---|
| Detalhe do jogo (organizador) | SCTopBar Large + SCStatusBadge Large + SCCountdownTimer Chip + SCSplitProgress Full + lista SCParticipantRow + SCButton Tonal "Convidar galera" |
| Pagar minha cota | SCPriceBreakdown Payer + SCButton Primary Large "Pagar R$ 21,50 com Pix" → SCPixPaymentSheet |
| Escolher horário | SCCourtCard (cabeçalho) + SCTimeSlotPicker + rodapé fixo com resumo + SCButton Primary "Pré-reservar 19h–20h" |
| Lista de jogos | SCSportChip Filter (rolagem) + SCGameCard × n + SCEmptyState + SCSkeleton |

---

## 7. Governança

### 7.1 Versionamento (SemVer)

| Mudança | Versão | Exemplo |
|---|---|---|
| **MAJOR** | Remove/renomeia token ou parâmetro público; muda significado de papel | `secondary` deixa de ser laranja |
| **MINOR** | Novo componente, variante, token ou parâmetro com default | `SCSplitProgress.Continuous` |
| **PATCH** | Ajuste de valor sem mudança de API que mantém contraste AA | Tom de `outline` ajustado |

- Tokens e componentes vivem no módulo KMP `:designsystem`; `design-tokens.json` é a fonte, versionada no mesmo repositório.
- Depreciação: `@Deprecated(level = WARNING)` por **1 MINOR**, depois `ERROR` por 1 MINOR, remoção no próximo MAJOR — sempre com `ReplaceWith`.
- CHANGELOG em `designsystem/CHANGELOG.md` com seções Adicionado / Alterado / Depreciado / Removido / Corrigido.

### 7.2 Fluxo de contribuição

1. **Proposta** (issue com template "DS: novo componente"): problema, telas afetadas, por que componentes existentes não bastam.
2. **Revisão de design** (DS owner + 1 designer de produto): anatomia, estados, tokens usados, acessibilidade.
3. **Implementação** em `:designsystem` com `@Preview` light/dark + fontScale 2.0, e entrada no catálogo (app de catálogo/`style-guide.html`).
4. **Verificações obrigatórias de CI:** `python3 tools/design/gen_tokens.py` sem reprovações de contraste · *screenshot tests* (Roborazzi/Paparazzi) light/dark · lint que proíbe `Color(0x…)` e `.dp` literais fora de `:designsystem`.
5. **Revisão de código** por 1 dev mobile + DS owner → merge → release com CHANGELOG.

Critério de entrada de um componente no DS: usado (ou previsto) em **≥ 2 features**. Caso contrário, nasce na feature e é promovido depois.

### 7.3 Nomenclatura

| Item | Padrão | Exemplo |
|---|---|---|
| Componente | `SC` + nome em inglês, PascalCase | `SCSplitProgress` |
| Variante | enum `SC<Componente>Variant` | `SCButtonVariant.Tonal` |
| Token primitivo | `<família>.<tom>` kebab | `verde-quadra.40`, `space.4` |
| Token semântico | papel M3 em camelCase | `primaryContainer`, `onWarningContainer` |
| Token de componente | `<componente>.<parte>.<propriedade>` | `button.primary.container` |
| Domínio | português sem acento em enums (fiel ao negócio) | `BookingStatus.AguardandoPagamento` |
| Arquivos de recurso | snake_case | `saira_condensed_bold.ttf`, `ic_sport_padel.xml` |
| Strings visíveis | `strings.xml` em pt-BR, chave `ds_<componente>_<uso>` | `ds_pix_copy_code` |

### 7.4 Responsáveis

| Papel | Responsabilidade |
|---|---|
| DS owner (Visual Designer) | Tokens, aprovação de componentes, contraste, CHANGELOG |
| DS engineer (mobile) | Implementação KMP, testes de screenshot, performance |
| Product designers | Propor componentes, usar só o catálogo, reportar lacunas |
| Acessibilidade (rotativo) | Revisão trimestral com TalkBack/VoiceOver/NVDA |
