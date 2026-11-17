@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Stadium
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.designsystem.component.AvatarUi
import br.com.sportconnect.core.designsystem.component.PriceLine
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCAvatar
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCPriceBreakdown
import br.com.sportconnect.core.designsystem.component.SCPriceEmphasis
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarVariant
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.usecase.Aviso
import br.com.sportconnect.core.domain.usecase.GetMyPaymentsUseCase
import br.com.sportconnect.core.domain.usecase.GetNotificationsUseCase
import br.com.sportconnect.core.domain.usecase.MeuPagamento
import br.com.sportconnect.core.domain.usecase.TipoAviso
import br.com.sportconnect.core.model.PapelUsuario
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.Usuario
import br.com.sportconnect.feature.common.CenteredContent
import br.com.sportconnect.feature.common.SectionHeader
import br.com.sportconnect.feature.common.rememberShareActions
import br.com.sportconnect.navigation.Route
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.auth_logout
import br.com.sportconnect.resources.common_convenience_fee
import br.com.sportconnect.resources.common_share
import br.com.sportconnect.resources.empty_notifications_body
import br.com.sportconnect.resources.empty_notifications_title
import br.com.sportconnect.resources.empty_payments_body
import br.com.sportconnect.resources.empty_payments_title
import br.com.sportconnect.resources.notifications_mark_all
import br.com.sportconnect.resources.notifications_title
import br.com.sportconnect.resources.notifications_unread_label
import br.com.sportconnect.resources.payment_summary_share
import br.com.sportconnect.resources.payment_summary_total
import br.com.sportconnect.resources.profile_have_arena
import br.com.sportconnect.resources.profile_mode_arena
import br.com.sportconnect.resources.profile_mode_athlete
import br.com.sportconnect.resources.profile_my_games
import br.com.sportconnect.resources.profile_notifications
import br.com.sportconnect.resources.profile_payments
import br.com.sportconnect.resources.profile_privacy
import br.com.sportconnect.resources.profile_saved_cards
import br.com.sportconnect.resources.profile_sports
import br.com.sportconnect.resources.profile_support
import br.com.sportconnect.resources.profile_title
import br.com.sportconnect.resources.profile_use_as
import br.com.sportconnect.resources.profile_version
import br.com.sportconnect.resources.push_game_cancelled_body
import br.com.sportconnect.resources.push_game_cancelled_title
import br.com.sportconnect.resources.push_game_confirmed_body
import br.com.sportconnect.resources.push_game_confirmed_title
import br.com.sportconnect.resources.push_organizer_exposure_body
import br.com.sportconnect.resources.push_organizer_progress_body
import br.com.sportconnect.resources.push_organizer_progress_title
import br.com.sportconnect.resources.push_payment_received_body
import br.com.sportconnect.resources.push_payment_received_title
import br.com.sportconnect.resources.receipt_paid_at
import br.com.sportconnect.resources.receipt_title
import br.com.sportconnect.resources.receipt_transaction_id
import br.com.sportconnect.resources.refund_status_done
import br.com.sportconnect.resources.refund_title
import br.com.sportconnect.resources.expiry_warning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Clock
import kotlin.time.Instant

// =================================================================== perfil + modo

/** Modo do app (09 §2.1). */
enum class AppMode { Athlete, Arena }

data class ProfileUiState(val user: Usuario? = null, val switching: Boolean = false) {
    val hasArenaRole: Boolean get() = user?.papeis?.any { it == PapelUsuario.GESTOR_ARENA || it == PapelUsuario.OPERADOR_ARENA } == true
}

/**
 * Perfil e troca de modo. No MVP fake, "Tenho uma arena"/"Minha Arena" entra como o gestor da demo
 * (`entrarComo("u-gestor")`) e "Atleta" volta para o organizador da demo.
 */
class ProfileViewModel(private val auth: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { auth.currentUser.collect { u -> _uiState.update { it.copy(user = u) } } }
    }

    fun switchMode(mode: AppMode, onDone: () -> Unit) {
        if (_uiState.value.switching) return
        _uiState.update { it.copy(switching = true) }
        viewModelScope.launch {
            val id = if (mode == AppMode.Arena) ARENA_DEMO_USER else ATHLETE_DEMO_USER
            auth.entrarComo(id)
            _uiState.update { it.copy(switching = false) }
            onDone()
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            auth.logout()
            onDone()
        }
    }

    companion object {
        const val ARENA_DEMO_USER = "u-gestor"
        const val ATHLETE_DEMO_USER = "u-organizador"
    }
}

data class ProfileActions(
    val myGames: () -> Unit = {},
    val payments: () -> Unit = {},
    val notifications: () -> Unit = {},
    val switchMode: (AppMode) -> Unit = {},
    val loggedOut: () -> Unit = {},
)

@Composable
fun ProfileRoute(actions: ProfileActions, vm: ProfileViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        actions = actions,
        onSwitch = { mode -> vm.switchMode(mode) { actions.switchMode(mode) } },
        onLogout = { vm.logout(actions.loggedOut) },
    )
}

/** Perfil (10 §2.14) com seletor "Usar como" (papel de arena) ou "Tenho uma arena". */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    actions: ProfileActions = ProfileActions(),
    onSwitch: (AppMode) -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Scaffold(
        containerColor = c.surface,
        topBar = { SCTopBar(stringResource(Res.string.profile_title), variant = SCTopBarVariant.CenterAligned, navigation = null) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            CenteredContent(Modifier.padding(horizontal = 16.dp)) {
                val u = state.user
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    SCAvatar(AvatarUi(u?.nome ?: "?"), size = 64.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(u?.nome.orEmpty(), style = t.titleLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
                        Text(u?.celular?.let { PtBrFormat.maskedPhone(it) }.orEmpty(), style = t.bodyMedium, color = c.onSurfaceVariant)
                    }
                }
                if (state.hasArenaRole) {
                    Text(stringResource(Res.string.profile_use_as), style = t.titleSmall, color = c.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("profile_mode_switch")) {
                        // No MVP fake o modo segue o perfil logado: gestor = Minha Arena.
                        SegmentedButton(
                            selected = false,
                            onClick = { onSwitch(AppMode.Athlete) },
                            enabled = !state.switching,
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                        ) { Text(stringResource(Res.string.profile_mode_athlete)) }
                        SegmentedButton(
                            selected = true,
                            onClick = { onSwitch(AppMode.Arena) },
                            enabled = !state.switching,
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                        ) { Text(stringResource(Res.string.profile_mode_arena)) }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                ProfileItem(Icons.Rounded.SportsScore, stringResource(Res.string.profile_my_games), "my_games", actions.myGames)
                ProfileItem(Icons.Rounded.Payments, stringResource(Res.string.profile_payments), "payments", actions.payments)
                ProfileItem(Icons.Rounded.SportsSoccer, stringResource(Res.string.profile_sports), "sports") {}
                ProfileItem(Icons.Rounded.Notifications, stringResource(Res.string.profile_notifications), "notifications", actions.notifications)
                ProfileItem(Icons.Rounded.CreditCard, stringResource(Res.string.profile_saved_cards), "saved_cards") {}
                ProfileItem(Icons.Rounded.PrivacyTip, stringResource(Res.string.profile_privacy), "privacy") {}
                ProfileItem(Icons.AutoMirrored.Rounded.HelpOutline, stringResource(Res.string.profile_support), "support") {}
                Spacer(Modifier.height(16.dp))
                if (!state.hasArenaRole) {
                    SCButton(
                        stringResource(Res.string.profile_have_arena),
                        { onSwitch(AppMode.Arena) },
                        variant = SCButtonVariant.Tonal,
                        leadingIcon = Icons.Rounded.Stadium,
                        loading = state.switching,
                        fullWidth = true,
                        modifier = Modifier.testTag("profile_have_arena"),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(Res.string.auth_logout),
                    style = t.labelLarge,
                    color = c.error,
                    modifier = Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onLogout).padding(vertical = 14.dp).testTag("profile_logout"),
                )
                Text(stringResource(Res.string.profile_version, "0.1.0"), style = t.bodySmall, color = c.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileItem(icon: ImageVector, label: String, id: String, onClick: () -> Unit) {
    val c = SportConnectTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button, onClick = onClick).testTag("profile_item_$id"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = c.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = c.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = c.onSurfaceVariant)
    }
}

// =================================================================== pagamentos e comprovante

data class PaymentsUiState(val loading: Boolean = true, val items: List<MeuPagamento> = emptyList())

class PaymentsViewModel(getMyPayments: GetMyPaymentsUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { getMyPayments().collect { list -> _uiState.value = PaymentsUiState(false, list) } }
    }
}

@Composable
fun PaymentsRoute(onBack: () -> Unit, onReceipt: (String) -> Unit, vm: PaymentsViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    PaymentsScreen(state, onBack, onReceipt)
}

/** Pagamentos e reembolsos (10 §2.14): lista por mes, chip de reembolso. */
@Composable
fun PaymentsScreen(state: PaymentsUiState, onBack: () -> Unit = {}, onReceipt: (String) -> Unit = {}) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val ext = SportConnectTheme.extendedColors
    Scaffold(containerColor = c.surface, topBar = { SCTopBar(stringResource(Res.string.profile_payments), onNavigationClick = onBack) }) { padding ->
        if (!state.loading && state.items.isEmpty()) {
            SCEmptyState(stringResource(Res.string.empty_payments_title), stringResource(Res.string.empty_payments_body), Modifier.padding(padding))
            return@Scaffold
        }
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 640.dp).fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                state.items.groupBy { PtBrFormat.monthYear(PtBrFormat.date(it.pagoEm)) }.forEach { (month, items) ->
                    item(key = month) { SectionHeader(month.replaceFirstChar { it.uppercase() }, Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                    items(items, key = { it.pagamentoId }) { p ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 72.dp).clickable { onReceipt(p.pagamentoId) }.testTag("payments_item_${p.pagamentoId}"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(p.jogo.titulo, style = t.bodyLarge, color = c.onSurface)
                                Text("${PtBrFormat.date(PtBrFormat.date(p.pagoEm))} · ${p.metodo.rotulo}", style = t.bodyMedium, color = c.onSurfaceVariant)
                                if (p.reembolsado) {
                                    Row(
                                        Modifier.padding(top = 4.dp).background(ext.successContainer, MaterialTheme.shapes.extraSmall).padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, null, tint = ext.onSuccessContainer, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(stringResource(Res.string.refund_title) + " · " + stringResource(Res.string.refund_status_done), style = t.labelMedium, color = ext.onSuccessContainer)
                                    }
                                }
                            }
                            Text(p.total.format(), style = t.titleMedium.copy(fontFeatureSettings = "tnum"), color = c.onSurface)
                        }
                        HorizontalDivider(color = c.outlineVariant)
                    }
                }
            }
        }
    }
}

class ReceiptViewModel(private val route: Route.Receipt, getMyPayments: GetMyPaymentsUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<MeuPagamento?>(null)
    val uiState: StateFlow<MeuPagamento?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { getMyPayments().collect { list -> _uiState.value = list.firstOrNull { it.pagamentoId == route.paymentId } } }
    }
}

@Composable
fun ReceiptRoute(route: Route.Receipt, onBack: () -> Unit) {
    val vm: ReceiptViewModel = koinViewModel(key = "receipt-${route.paymentId}") { parametersOf(route) }
    val p by vm.uiState.collectAsStateWithLifecycle()
    ReceiptScreen(p, onBack)
}

/** Comprovante (10 §2.14). */
@Composable
fun ReceiptScreen(p: MeuPagamento?, onBack: () -> Unit = {}) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val share = rememberShareActions()
    Scaffold(containerColor = c.surface, topBar = { SCTopBar(stringResource(Res.string.receipt_title), onNavigationClick = onBack) }) { padding ->
        if (p == null) return@Scaffold
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            CenteredContent(Modifier.padding(16.dp)) {
                Column(
                    Modifier.fillMaxWidth().background(c.surfaceContainerLowest, MaterialTheme.shapes.large)
                        .border(1.dp, c.outlineVariant, MaterialTheme.shapes.large).padding(16.dp),
                ) {
                    Text(p.jogo.titulo, style = t.titleMedium, color = c.onSurface)
                    Text("${p.jogo.arenaNome} · ${p.jogo.quadraNome}", style = t.bodyMedium, color = c.onSurfaceVariant)
                    Text(stringResource(Res.string.receipt_paid_at, "${PtBrFormat.dateTime(p.pagoEm)} · ${p.metodo.rotulo}"), style = t.bodyMedium, color = c.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    SCPriceBreakdown(
                        lines = listOf(
                            PriceLine(stringResource(Res.string.payment_summary_share), p.valorCota),
                            PriceLine(stringResource(Res.string.common_convenience_fee), p.taxa, emphasis = SCPriceEmphasis.Muted),
                        ),
                        total = PriceLine(stringResource(Res.string.payment_summary_total), p.total),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(Res.string.receipt_transaction_id), style = t.titleSmall, color = c.onSurface)
                    Text(p.pagamentoId, style = SportConnectTheme.typographyExt.pixCode, color = c.onSurfaceVariant)
                }
                Spacer(Modifier.height(16.dp))
                val text = "${p.jogo.titulo} · ${p.total.format()} · ${p.pagamentoId}"
                SCButton(stringResource(Res.string.common_share), { share.copy(text) }, variant = SCButtonVariant.Secondary, modifier = Modifier.testTag("receipt_share"))
            }
        }
    }
}

// =================================================================== avisos

data class NotificationsUiState(val loading: Boolean = true, val items: List<Aviso> = emptyList(), val now: Instant = Instant.DISTANT_PAST)

class NotificationsViewModel(private val getNotifications: GetNotificationsUseCase, private val clock: Clock) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { getNotifications().collect { _uiState.value = NotificationsUiState(false, it, clock.now()) } }
    }

    fun markAll() = getNotifications.marcarTodos(_uiState.value.items.map { it.id })
}

@Composable
fun NotificationsRoute(onBack: () -> Unit, onOpenGame: (String) -> Unit, vm: NotificationsViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    NotificationsScreen(state, onBack, onOpenGame, vm::markAll)
}

/** Avisos (10 §2.15): itens por dia, icone por tipo, nao lidos com ponto. */
@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onBack: () -> Unit = {},
    onOpenGame: (String) -> Unit = {},
    onMarkAll: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    val t = MaterialTheme.typography
    Scaffold(
        containerColor = c.surface,
        topBar = {
            SCTopBar(stringResource(Res.string.notifications_title), onNavigationClick = onBack, actions = {
                val markAll = stringResource(Res.string.notifications_mark_all)
                IconButton(onClick = onMarkAll, modifier = Modifier.testTag("notifications_mark_all").semantics { contentDescription = markAll }) {
                    Icon(Icons.Rounded.DoneAll, null)
                }
            })
        },
    ) { padding ->
        if (!state.loading && state.items.isEmpty()) {
            SCEmptyState(stringResource(Res.string.empty_notifications_title), stringResource(Res.string.empty_notifications_body), Modifier.padding(padding))
            return@Scaffold
        }
        val unreadLabel = stringResource(Res.string.notifications_unread_label)
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 640.dp).fillMaxSize()) {
                state.items.groupBy { PtBrFormat.relativeDay(PtBrFormat.date(it.em), PtBrFormat.date(state.now)) }.forEach { (day, items) ->
                    item(key = "d$day") { SectionHeader(day.replaceFirstChar { it.uppercase() }, Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
                    items(items, key = { it.id }) { a ->
                        val (icon, bg, fg) = when (a.tipo) {
                            TipoAviso.PAGAMENTO_RECEBIDO, TipoAviso.PROGRESSO -> Triple(Icons.Rounded.Payments, c.tertiaryContainer, c.onTertiaryContainer)
                            TipoAviso.CONFIRMADO -> Triple(Icons.Rounded.CheckCircle, ext.successContainer, ext.onSuccessContainer)
                            TipoAviso.PRAZO -> Triple(Icons.Rounded.Timer, ext.warningContainer, ext.onWarningContainer)
                            TipoAviso.CANCELADO -> Triple(Icons.Rounded.Cancel, c.errorContainer, c.onErrorContainer)
                        }
                        val j = a.jogo
                        val (title, body) = when (a.tipo) {
                            TipoAviso.PAGAMENTO_RECEBIDO -> stringResource(Res.string.push_payment_received_title) to stringResource(Res.string.push_payment_received_body, j.titulo)
                            TipoAviso.PROGRESSO -> stringResource(Res.string.push_organizer_progress_title, a.pagas, a.total) to
                                stringResource(Res.string.push_organizer_progress_body, a.total - a.pagas, j.reserva.prazoCotas?.let { PtBrFormat.relative(it, state.now) } ?: "")
                            TipoAviso.PRAZO -> stringResource(Res.string.expiry_warning, j.reserva.prazoCotas?.let { PtBrFormat.relative(it, state.now) } ?: "") to
                                stringResource(Res.string.push_organizer_exposure_body, a.total - a.pagas, a.valor.format())
                            TipoAviso.CONFIRMADO -> stringResource(Res.string.push_game_confirmed_title) to
                                stringResource(Res.string.push_game_confirmed_body, PtBrFormat.dateTime(j.inicio), j.arenaNome)
                            TipoAviso.CANCELADO -> stringResource(Res.string.push_game_cancelled_title) to
                                stringResource(Res.string.push_game_cancelled_body, j.titulo, a.valor.format())
                        }
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 72.dp)
                                .background(if (a.lido) Color.Transparent else c.surfaceContainerLow)
                                .clickable { onOpenGame(j.id) }
                                .semantics(mergeDescendants = true) { if (!a.lido) contentDescription = "$title, $unreadLabel. $body" }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("notification_item_${a.id}"),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(Modifier.size(40.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(title, style = t.titleSmall, color = c.onSurface)
                                Text(body, style = t.bodyMedium, color = c.onSurfaceVariant, maxLines = 2)
                                Text(PtBrFormat.hour(a.em), style = t.labelSmall, color = c.onSurfaceVariant)
                            }
                            if (!a.lido) Box(Modifier.padding(top = 6.dp).size(8.dp).background(c.primary, CircleShape))
                        }
                    }
                }
            }
        }
    }
}
