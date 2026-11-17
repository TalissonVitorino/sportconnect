package br.com.sportconnect.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.AvatarUi
import br.com.sportconnect.core.designsystem.component.CourtCardSkeleton
import br.com.sportconnect.core.designsystem.component.CourtCardUi
import br.com.sportconnect.core.designsystem.component.GameCardSkeleton
import br.com.sportconnect.core.designsystem.component.GameCardUi
import br.com.sportconnect.core.designsystem.component.ParticipantRowSkeleton
import br.com.sportconnect.core.designsystem.component.PriceLine
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCAvatarStack
import br.com.sportconnect.core.designsystem.component.SCBadgeSize
import br.com.sportconnect.core.designsystem.component.SCBottomNav
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCCountdownTimer
import br.com.sportconnect.core.designsystem.component.SCCountdownVariant
import br.com.sportconnect.core.designsystem.component.SCCourtCard
import br.com.sportconnect.core.designsystem.component.SCCourtCardVariant
import br.com.sportconnect.core.designsystem.component.SCDialogContent
import br.com.sportconnect.core.designsystem.component.SCDialogVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCGameCard
import br.com.sportconnect.core.designsystem.component.SCGameCardVariant
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.component.SCIllustrationImage
import br.com.sportconnect.core.designsystem.component.SCNavItem
import br.com.sportconnect.core.designsystem.component.SCNavigationRail
import br.com.sportconnect.core.designsystem.component.SCOpenSpotRow
import br.com.sportconnect.core.designsystem.component.SCParticipantRow
import br.com.sportconnect.core.designsystem.component.SCPixPaymentContent
import br.com.sportconnect.core.designsystem.component.SCPixState
import br.com.sportconnect.core.designsystem.component.SCPriceBreakdown
import br.com.sportconnect.core.designsystem.component.SCPriceEmphasis
import br.com.sportconnect.core.designsystem.component.SCSkeleton
import br.com.sportconnect.core.designsystem.component.SCSkeletonShape
import br.com.sportconnect.core.designsystem.component.SCSnackbar
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.SCSplitProgress
import br.com.sportconnect.core.designsystem.component.SCSplitProgressVariant
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.component.SCSportChipVariant
import br.com.sportconnect.core.designsystem.component.SCStatusBadge
import br.com.sportconnect.core.designsystem.component.SCTextField
import br.com.sportconnect.core.designsystem.component.SCTextFieldType
import br.com.sportconnect.core.designsystem.component.SCTimeSlotPicker
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.component.SCTopBarVariant
import br.com.sportconnect.core.designsystem.component.SlotUi
import br.com.sportconnect.core.designsystem.component.TimeSlotGridSkeleton
import br.com.sportconnect.core.designsystem.icon.SCLogo
import br.com.sportconnect.core.designsystem.icon.icon
import br.com.sportconnect.core.designsystem.theme.LocalSCClock
import br.com.sportconnect.core.designsystem.theme.SCReservationTone
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SCShareState
import br.com.sportconnect.core.designsystem.theme.SCSlotState
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Sport
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Instante fixo do catalogo: sabado, 21/11/2026 17:00 BRT. Tudo que tem prazo deriva dele. */
@OptIn(ExperimentalTime::class)
private val CatalogNow: Instant = Instant.fromEpochSeconds(1_795_291_200)

@OptIn(ExperimentalTime::class)
private object CatalogClock : Clock {
    override fun now(): Instant = CatalogNow
}

/**
 * Vitrine do "Placar DS": todos os componentes SC* em seus estados principais, organizados em
 * colunas responsivas (1 coluna compact; ate 3 em telas largas). Usa um relogio fixo para que
 * contadores e screenshots sejam deterministicos.
 *
 * Deve ser chamada dentro de [SportConnectTheme]. Use [DesignSystemCatalogApp] para a versao com
 * alternancia claro/escuro.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun DesignSystemCatalog(
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    darkTheme: Boolean = false,
    onToggleTheme: (() -> Unit)? = null,
) {
    val c = SportConnectTheme.colors
    CompositionLocalProvider(LocalSCClock provides CatalogClock) {
        Surface(modifier, color = c.surface) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val columns = when {
                    maxWidth >= 1240.dp -> 3
                    maxWidth >= 820.dp -> 2
                    else -> 1
                }
                val sections = catalogSections()
                Column(
                    Modifier.fillMaxWidth()
                        .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                        .padding(horizontal = if (columns == 1) 16.dp else 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    CatalogHeader(darkTheme, onToggleTheme)
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        val perColumn = distribute(sections, columns)
                        perColumn.forEach { col ->
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                col.forEach { s -> CatalogSection(s.title) { s.content() } }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Catalogo com estado de tema proprio (claro/escuro) — pronto para virar rota de debug. */
@Composable
fun DesignSystemCatalogApp(initialDark: Boolean = false) {
    var dark by remember { mutableStateOf(initialDark) }
    SportConnectTheme(darkTheme = dark) {
        DesignSystemCatalog(darkTheme = dark, onToggleTheme = { dark = !dark })
    }
}

private class Section(val title: String, val weight: Int, val content: @Composable () -> Unit)

/** Distribui as secoes em colunas equilibrando o "peso" (altura estimada). */
private fun distribute(sections: List<Section>, columns: Int): List<List<Section>> {
    val cols = List(columns) { mutableListOf<Section>() }
    val heights = IntArray(columns)
    sections.forEach { s ->
        val i = heights.indices.minBy { heights[it] }
        cols[i] += s
        heights[i] += s.weight
    }
    return cols
}

@Composable
private fun CatalogHeader(dark: Boolean, onToggleTheme: (() -> Unit)?) {
    val c = SportConnectTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SCLogo(Modifier.height(44.dp), onDark = dark)
            Text(
                "Placar DS 1.0 · catálogo de componentes · tema ${if (dark) "escuro" else "claro"}",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
            )
        }
        if (onToggleTheme != null) {
            IconButton(onClick = onToggleTheme) {
                Icon(if (dark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, if (dark) "Usar tema claro" else "Usar tema escuro")
            }
        }
    }
}

@Composable
private fun CatalogSection(title: String, content: @Composable () -> Unit) {
    val c = SportConnectTheme.colors
    Column(
        Modifier.fillMaxWidth()
            .background(c.surfaceContainerLow, SCShape.card)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = c.onSurface, modifier = Modifier.semantics { heading() })
        content()
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = SportConnectTheme.colors.onSurfaceVariant)
}

private val people = listOf(
    AvatarUi("Ana Souza", paid = true), AvatarUi("Bruno Lima", paid = true), AvatarUi("Carla Dias"),
    AvatarUi("Diego Rocha", paid = true), AvatarUi("Eduarda Melo"), AvatarUi("Felipe Nunes", paid = true),
    AvatarUi("Gabi Torres"), AvatarUi("Hugo Alves"), AvatarUi("Iris Prado", paid = true),
    AvatarUi("João Reis"), AvatarUi("Kátia Luz"), AvatarUi("Léo Mota"),
)

@OptIn(ExperimentalTime::class)
@Composable
private fun catalogSections(): List<Section> = listOf(
    Section("Cores", 5) { ColorsSection() },
    Section("Tipografia", 6) { TypographySection() },
    Section("SCButton", 5) { ButtonsSection() },
    Section("SCSportChip", 4) { SportChipsSection() },
    Section("SCStatusBadge", 3) { StatusSection() },
    Section("SCSplitProgress", 5) { SplitSection() },
    Section("SCCountdownTimer", 4) { CountdownSection() },
    Section("SCTextField", 8) { TextFieldsSection() },
    Section("SCGameCard", 9) { GameCardsSection() },
    Section("SCCourtCard", 8) { CourtCardsSection() },
    Section("SCTimeSlotPicker", 6) { TimeSlotSection() },
    Section("SCParticipantRow", 6) { ParticipantSection() },
    Section("SCPriceBreakdown", 4) { PriceSection() },
    Section("SCPixPaymentSheet", 10) { PixSection() },
    Section("SCAvatarStack", 2) { AvatarSection() },
    Section("SCTopBar", 4) { TopBarSection() },
    Section("SCBottomNav · rail", 6) { NavSection() },
    Section("SCSnackbar", 4) { SnackbarSection() },
    Section("SCDialog", 4) { DialogSection() },
    Section("SCEmptyState", 6) { EmptySection() },
    Section("SCSkeleton", 7) { SkeletonSection() },
)

@Composable
private fun Swatch(name: String, color: Color, on: Color, size: Dp = 64.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier.size(size).background(color, SCShape.small).border(1.dp, SportConnectTheme.colors.outlineVariant, SCShape.small),
            contentAlignment = Alignment.Center,
        ) {
            Text("Aa", color = on, style = MaterialTheme.typography.titleMedium)
        }
        Text(name, style = MaterialTheme.typography.labelSmall, color = SportConnectTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun ColorsSection() {
    val c = SportConnectTheme.colors
    val e = SportConnectTheme.extendedColors
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Swatch("primary", c.primary, c.onPrimary)
        Swatch("primaryCont.", c.primaryContainer, c.onPrimaryContainer)
        Swatch("secondary", c.secondary, c.onSecondary)
        Swatch("secondaryCont.", c.secondaryContainer, c.onSecondaryContainer)
        Swatch("tertiary", c.tertiary, c.onTertiary)
        Swatch("error", c.error, c.onError)
        Swatch("success", e.success, e.onSuccess)
        Swatch("warning", e.warning, e.onWarning)
        Swatch("surface", c.surface, c.onSurface)
        Swatch("contLow", c.surfaceContainerLow, c.onSurface)
        Swatch("contHigh", c.surfaceContainerHigh, c.onSurface)
        Swatch("inverse", c.inverseSurface, c.inverseOnSurface)
    }
    Label("Esportes (container · acento)")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Sport.entries.forEach { s ->
            val sc = e.sport(s)
            Box(Modifier.size(40.dp).background(sc.container, SCShape.chip), contentAlignment = Alignment.Center) {
                Icon(s.icon, null, tint = sc.accent, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun TypographySection() {
    val t = MaterialTheme.typography
    val x = SportConnectTheme.typographyExt
    val c = SportConnectTheme.colors.onSurface
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("R$ 240,00", style = t.displayLarge, color = c)
        Text("14:59", style = t.displayMedium, color = c)
        Text("Racha de sábado", style = t.headlineLarge, color = c)
        Text("Arena Vila Madalena", style = t.headlineMedium, color = c)
        Text("Jogos desta semana", style = t.headlineSmall, color = c)
        Text("Pague com Pix", style = t.titleLarge, color = c)
        Text("Futebol society · Quadra 2", style = t.titleMedium, color = c)
        Text("Texto corrido em Atkinson Hyperlegible Next: 0O 1lI 5S 8B.", style = t.bodyLarge, color = c)
        Text("Descrição de card e listas — bodyMedium.", style = t.bodyMedium, color = c)
        Text("Legenda / texto de apoio — bodySmall.", style = t.bodySmall, color = c)
        Text("Rótulo de botão — labelLarge", style = t.labelLarge, color = c)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom) {
            Text("8/12", style = x.numericScore, color = c)
            Text("R$ 20,00", style = x.numericPrice, color = c)
        }
        Text("00020126580014BR.GOV.BCB.PIX0136a1b2c3d4", style = x.pixCode, color = c)
    }
}

@Composable
private fun ButtonsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SCButton("Pagar R$ 21,50 com Pix", {}, size = SCButtonSize.Large, fullWidth = true)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SCButton("Criar jogo", {}, leadingIcon = Icons.Rounded.Add)
            SCButton("Pagar com cartão", {}, variant = SCButtonVariant.Secondary)
            SCButton("Convidar galera", {}, variant = SCButtonVariant.Tonal, leadingIcon = Icons.Rounded.GroupAdd)
            SCButton("Ver regras", {}, variant = SCButtonVariant.Text)
            SCButton("Cancelar e reembolsar", {}, variant = SCButtonVariant.Destructive)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SCButton("Small", {}, size = SCButtonSize.Small)
            SCButton("Gerando código", {}, loading = true)
            SCButton("Desabilitado", {}, enabled = false)
            SCButton("Desabilitado", {}, enabled = false, variant = SCButtonVariant.Secondary)
        }
    }
}

@Composable
private fun SportChipsSection() {
    var selected by remember { mutableStateOf(setOf(Sport.FUTEBOL_SOCIETY, Sport.BEACH_TENNIS)) }
    Label("Tag")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Sport.entries.forEach { SCSportChip(it) }
    }
    Label("Filter (selecionável)")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Sport.entries.take(5).forEach { s ->
            SCSportChip(
                s,
                variant = SCSportChipVariant.Filter,
                selected = s in selected,
                onClick = { selected = if (s in selected) selected - s else selected + s },
            )
        }
    }
    Label("Compact")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Sport.entries.forEach { SCSportChip(it, variant = SCSportChipVariant.Compact) }
    }
}

@Composable
private fun StatusSection() {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SCReservationTone.entries.forEach { SCStatusBadge(it) }
    }
    SCStatusBadge(SCReservationTone.AwaitingPayment, size = SCBadgeSize.Large, detail = "8 de 12 pagos")
}

@Composable
private fun SplitSection() {
    SCSplitProgress(12, paid = 7, covered = 1, processing = 1, collected = Money(16000), total = Money(24000))
    Label("Compact · expirada")
    SCSplitProgress(10, paid = 6, variant = SCSplitProgressVariant.Compact, expired = true)
    Label("Continuous (40 cotas)")
    SCSplitProgress(40, paid = 23, covered = 2, variant = SCSplitProgressVariant.Continuous, showLegend = false)
}

@OptIn(ExperimentalTime::class)
@Composable
private fun CountdownSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SCCountdownTimer(CatalogNow + 3.hours + 20.minutes, label = "para fechar o jogo")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SCCountdownTimer(CatalogNow + 42.minutes + 10.seconds, variant = SCCountdownVariant.Chip)
            SCCountdownTimer(CatalogNow + 4.minutes + 59.seconds, variant = SCCountdownVariant.Chip)
            SCCountdownTimer(CatalogNow - 1.minutes, variant = SCCountdownVariant.Chip)
        }
        SCCountdownTimer(CatalogNow + 14.minutes + 59.seconds, variant = SCCountdownVariant.Large, label = "para o código expirar")
    }
}

@Composable
private fun TextFieldsSection() {
    var name by remember { mutableStateOf("Racha de sábado") }
    var money by remember { mutableStateOf("2150") }
    var phone by remember { mutableStateOf("11987654321") }
    var cpf by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var bad by remember { mutableStateOf("300") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SCTextField(name, { name = it }, "Nome do jogo", maxLength = 40, supportingText = "Aparece no convite")
        SCTextField(money, { money = it }, "Valor da cota", type = SCTextFieldType.Money)
        SCTextField(phone, { phone = it }, "Celular", type = SCTextFieldType.Phone)
        SCTextField(cpf, { cpf = it }, "CPF", type = SCTextFieldType.Cpf, placeholder = "000.000.000-00")
        SCTextField(search, { search = it }, "Buscar quadras", type = SCTextFieldType.Search, placeholder = "Bairro, arena ou esporte")
        SCTextField(bad, { bad = it }, "Valor mínimo", type = SCTextFieldType.Money, errorText = "Informe um valor a partir de R$ 5,00")
        SCTextField("Arena Vila Madalena", {}, "Arena", enabled = false)
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun GameCardsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SCGameCard(
            GameCardUi(
                sport = Sport.FUTEBOL_SOCIETY,
                title = "Racha de sábado",
                arenaName = "Arena Vila Madalena",
                courtName = "Quadra 2",
                dateTimeLabel = "Sáb, 21/11 · 19h–20h",
                status = SCReservationTone.AwaitingPayment,
                statusLabel = "Pré-reserva · 8 de 12 pagas",
                paidCount = 8,
                totalShares = 12,
                shareValue = Money(2000),
                participants = people,
                holdExpiresAt = CatalogNow + 42.minutes,
                myShare = SCShareState.Pending,
                shareCaption = "Sua cota",
            ),
            onClick = {},
            primaryAction = SCAction("Pagar R$ 20,00", {}),
        )
        SCGameCard(
            GameCardUi(
                sport = Sport.BEACH_TENNIS,
                title = "Beach tennis da firma",
                arenaName = "Praia Clube",
                courtName = "Quadra de areia 1",
                dateTimeLabel = "Dom, 22/11 · 8h–10h",
                status = SCReservationTone.Confirmed,
                paidCount = 3,
                coveredCount = 1,
                totalShares = 4,
                shareValue = Money(4500),
                participants = people.take(4),
                shareCaption = "Cota",
            ),
            onClick = {},
            variant = SCGameCardVariant.Organizer,
        )
    }
}

@Composable
private fun CourtCardsSection() {
    val slots = listOf(
        SlotUi("a", "18:00", "19:00"), SlotUi("b", "19:00", "20:00"), SlotUi("c", "21:00", "22:00"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SCCourtCard(
            CourtCardUi(
                arenaName = "Arena Vila Madalena",
                courtName = "Quadra 2",
                detailsLabel = "Grama sintética · Coberta",
                sports = listOf(Sport.FUTEBOL_SOCIETY, Sport.FUTSAL, Sport.HANDEBOL),
                priceFrom = Money(18000),
                distanceLabel = "2,4 km",
                nextSlots = slots,
            ),
            onClick = {},
            onSlotClick = {},
        )
        SCCourtCard(
            CourtCardUi(
                arenaName = "Padel Point Pinheiros",
                courtName = "Quadra 1",
                detailsLabel = "Vidro · Coberta",
                sports = listOf(Sport.PADEL, Sport.TENIS),
                priceFrom = Money(16000),
                distanceLabel = "1,1 km",
            ),
            onClick = {},
            variant = SCCourtCardVariant.Horizontal,
        )
    }
}

@Composable
private fun TimeSlotSection() {
    val days = (21..27).map { LocalDate(2026, 11, it) }
    var day by remember { mutableStateOf(days.first()) }
    var selection by remember { mutableStateOf(setOf("s4")) }
    val slots = listOf(
        SlotUi("s1", "17:00", "18:00", Money(18000), SCSlotState.Busy),
        SlotUi("s2", "18:00", "19:00", Money(18000), SCSlotState.OnHold, holdReleasesAtLabel = "18:40"),
        SlotUi("s3", "19:00", "20:00", Money(22000)),
        SlotUi("s4", "20:00", "21:00", Money(22000)),
        SlotUi("s5", "21:00", "22:00", Money(20000)),
        SlotUi("s6", "22:00", "23:00", Money(18000), SCSlotState.Busy),
    )
    SCTimeSlotPicker(days, day, { day = it }, slots, selection, { selection = it }, columns = 2)
}

@Composable
private fun ParticipantSection() {
    Column {
        SCParticipantRow("Rafa Costa", SCShareState.Pending, isSelf = true, action = SCAction("Pagar", {}))
        SCParticipantRow("Ana Souza", SCShareState.Paid, role = "Organizadora", paidAtLabel = "14:32")
        SCParticipantRow("Bruno Lima", SCShareState.Pending, action = SCAction("Lembrar", {}, Icons.Rounded.Notifications, accessibilityLabel = "Lembrar Bruno de pagar"))
        SCParticipantRow("Carla Dias", SCShareState.Processing)
        SCParticipantRow("Diego Rocha", SCShareState.Covered)
        SCOpenSpotRow(action = SCAction("Convidar", {}, Icons.Rounded.Share))
    }
}

@Composable
private fun PriceSection() {
    SCPriceBreakdown(
        lines = listOf(
            PriceLine("Quadra (1h)", Money(24000)),
            PriceLine("Dividido por 12", valueText = "12 cotas", emphasis = SCPriceEmphasis.Muted),
            PriceLine("Sua cota", Money(2000)),
            PriceLine("Taxa de conveniência", Money(150), hint = "Por que tem taxa?", onHintClick = {}),
        ),
        total = PriceLine("Total a pagar", Money(2150)),
        footnote = "Se o jogo não fechar até o prazo, devolvemos o valor.",
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun PixSection() {
    Box(Modifier.fillMaxWidth().background(SportConnectTheme.colors.surfaceContainerLow, SCShape.sheet)) {
        SCPixPaymentContent(
            amount = Money(2150),
            pixCopyPaste = "00020126580014BR.GOV.BCB.PIX0136a1b2c3d4-e5f6-7890-abcd-ef1234567890520400005303986540521.505802BR5913SPORTCONNECT6009SAO PAULO62070503***6304B14F",
            expiresAt = CatalogNow + 14.minutes + 59.seconds,
            state = SCPixState.Waiting,
            onCopy = {},
            onRegenerate = {},
            onPayWithCard = {},
            requestInitialFocus = false,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun AvatarSection() {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
        SCAvatarStack(people)
        SCAvatarStack(people.take(6), paymentAware = true, size = 40.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarSection() {
    Column(Modifier.fillMaxWidth().border(1.dp, SportConnectTheme.colors.outlineVariant, SCShape.small)) {
        SCTopBar("Detalhe do jogo", actions = {
            IconButton({}) { Icon(Icons.Rounded.Share, "Compartilhar convite") }
        })
        HorizontalDivider(color = SportConnectTheme.colors.outlineVariant)
        SCTopBar("Início", variant = SCTopBarVariant.CenterAligned, navigation = null)
        HorizontalDivider(color = SportConnectTheme.colors.outlineVariant)
        SCTopBar("3 horários selecionados", variant = SCTopBarVariant.Contextual, navigation = SCTopBarNavigation.Close)
    }
}

private val navItems = listOf(
    SCNavItem("home", "Início", Icons.Outlined.Home, Icons.Rounded.Home),
    SCNavItem("games", "Jogos", Icons.Outlined.SportsScore, Icons.Rounded.SportsScore, badgeCount = 2, badgeDescription = "2 pendências"),
    SCNavItem("search", "Buscar", Icons.Rounded.Search),
    SCNavItem("profile", "Perfil", Icons.Outlined.Person, Icons.Rounded.Person),
)

@Composable
private fun NavSection() {
    var route by remember { mutableStateOf("games") }
    Label("Bottom navigation (compact)")
    Box(Modifier.fillMaxWidth().border(1.dp, SportConnectTheme.colors.outlineVariant, SCShape.small)) {
        SCBottomNav(navItems, route, { route = it })
    }
    Label("Navigation rail (medium/expanded)")
    Row(Modifier.height(320.dp).border(1.dp, SportConnectTheme.colors.outlineVariant, SCShape.small)) {
        SCNavigationRail(navItems, route, { route = it }, header = {
            Icon(SCLogo.Symbol, contentDescription = "SportConnect", tint = Color.Unspecified, modifier = Modifier.size(40.dp))
        })
        Box(Modifier.weight(1f).padding(16.dp)) {
            Text("Conteúdo", style = MaterialTheme.typography.bodyMedium, color = SportConnectTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun SnackbarSection() {
    Column {
        SCSnackbar("Código Pix copiado", kind = SCSnackbarKind.Success)
        SCSnackbar("Convite removido", kind = SCSnackbarKind.Undo, actionLabel = "Desfazer")
        SCSnackbar("Não foi possível copiar. Toque e segure o código para copiar.", kind = SCSnackbarKind.Error, onDismiss = {})
    }
}

@Composable
private fun DialogSection() {
    Box(
        Modifier.fillMaxWidth().background(SportConnectTheme.colors.scrim.copy(alpha = 0.32f), SCShape.small).padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        SCDialogContent(
            title = "Cancelar o jogo?",
            body = "Os 8 pagamentos serão reembolsados em até 1 dia útil.",
            icon = Icons.Rounded.Cancel,
            variant = SCDialogVariant.Destructive,
            confirm = SCAction("Cancelar e reembolsar", {}),
            dismiss = SCAction("Manter jogo", {}),
            requestInitialFocus = false,
        )
    }
}

@Composable
private fun EmptySection() {
    SCEmptyState(
        title = "Nenhum jogo ainda",
        description = "Crie um jogo e mande o link no grupo. A gente cuida do rateio.",
        primaryAction = SCAction("Criar meu primeiro jogo", {}, Icons.Rounded.Add),
        secondaryAction = SCAction("Buscar quadras", {}),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(SCIllustration.NoResults, SCIllustration.Error, SCIllustration.Offline).forEach {
            SCIllustrationImage(it, Modifier.weight(1f).height(96.dp))
        }
    }
}

@Composable
private fun SkeletonSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GameCardSkeleton(Modifier.fillMaxWidth())
        ParticipantRowSkeleton()
        TimeSlotGridSkeleton(rows = 1)
        CourtCardSkeleton(Modifier.widthIn(max = 280.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SCSkeleton(SCSkeletonShape.Circle(32.dp))
            SCSkeleton(SCSkeletonShape.Line(0.6f))
        }
    }
}

// ---------- Previews ----------

@Preview
@Composable
private fun CatalogLightPreview() {
    SportConnectTheme(darkTheme = false) { DesignSystemCatalog() }
}

@Preview
@Composable
private fun CatalogDarkPreview() {
    SportConnectTheme(darkTheme = true) { DesignSystemCatalog(darkTheme = true) }
}

@Preview
@Composable
private fun ButtonsPreview() {
    SportConnectTheme { Surface { Column(Modifier.padding(16.dp)) { ButtonsSection() } } }
}

@Preview
@Composable
private fun GameCardPreview() {
    SportConnectTheme { CompositionLocalProvider(LocalSCClock provides CatalogClock) { Surface { Box(Modifier.padding(16.dp)) { GameCardsSection() } } } }
}

@Preview
@Composable
private fun PixPreview() {
    SportConnectTheme(darkTheme = true) { CompositionLocalProvider(LocalSCClock provides CatalogClock) { Surface { PixSection() } } }
}
