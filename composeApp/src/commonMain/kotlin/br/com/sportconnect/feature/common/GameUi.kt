package br.com.sportconnect.feature.common

import br.com.sportconnect.core.designsystem.component.AvatarUi
import br.com.sportconnect.core.designsystem.component.GameCardUi
import br.com.sportconnect.core.designsystem.theme.SCShareState
import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.OrigemPagamento
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TipoCota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

/** Contagem do rateio para o [br.com.sportconnect.core.designsystem.component.SCSplitProgress]. */
data class SplitCounts(val total: Int, val paid: Int, val processing: Int, val covered: Int, val refunded: Int)

fun Jogo.splitCounts(now: Instant): SplitCounts {
    val padrao = reserva.cotas.filter { it.tipo == TipoCota.PADRAO }
    return SplitCounts(
        total = numeroVagas,
        paid = padrao.count { it.status == StatusCota.PAGA && it.origem != OrigemPagamento.COBERTURA },
        processing = padrao.count { it.isProcessing(now) },
        covered = padrao.count { it.status == StatusCota.PAGA && it.origem == OrigemPagamento.COBERTURA },
        refunded = padrao.count { it.status == StatusCota.REEMBOLSADA },
    )
}

/** Cota travada por alguem em checkout (Pix enviado, aguardando o banco) = "Em análise". */
fun Cota.isProcessing(now: Instant): Boolean =
    status == StatusCota.PENDENTE && atletaId != null && !organizador && checkoutAte != null && checkoutAte > now

/** Linha da lista "Quem está no jogo". */
data class ParticipantUi(
    val key: String,
    val userId: String?,
    val name: String,
    val state: SCShareState,
    val isSelf: Boolean,
    val isOrganizer: Boolean,
    val paidAt: Instant?,
    val openSpot: Boolean,
)

/** Ordem do 10 §2.9: voce -> pendentes -> em analise -> pagos -> vagas abertas. */
fun Jogo.participantsFor(userId: String?, now: Instant): List<ParticipantUi> {
    val padrao = reserva.cotas.filter { it.tipo == TipoCota.PADRAO }
    val rows = padrao.map { c ->
        val state = when {
            c.status == StatusCota.PAGA && c.origem == OrigemPagamento.COBERTURA -> SCShareState.Covered
            c.status == StatusCota.PAGA -> SCShareState.Paid
            c.status == StatusCota.REEMBOLSADA -> SCShareState.Refunded
            c.isProcessing(now) -> SCShareState.Processing
            else -> SCShareState.Pending
        }
        val open = c.atletaId == null && state == SCShareState.Pending
        ParticipantUi(
            key = c.id,
            userId = c.atletaId,
            name = c.atletaNome ?: "",
            state = state,
            isSelf = userId != null && c.atletaId == userId,
            isOrganizer = c.organizador,
            paidAt = c.pagaEm,
            openSpot = open,
        )
    }
    fun rank(p: ParticipantUi): Int = when {
        p.isSelf -> 0
        p.openSpot -> 5
        p.state == SCShareState.Pending -> 1
        p.state == SCShareState.Processing -> 2
        p.state == SCShareState.Paid || p.state == SCShareState.Covered -> 3
        else -> 4
    }
    return rows.sortedWith(compareBy<ParticipantUi>({ rank(it) }, { it.paidAt ?: Instant.DISTANT_FUTURE }))
}

/** Mapeia o jogo para o card do DS. [statusLabel] ja resolvido pela tela (I11). */
fun Jogo.toCardUi(
    userId: String?,
    statusLabel: String?,
    shareCaption: String?,
    countdownLabel: String?,
    now: Instant,
    roleTag: String? = null,
): GameCardUi {
    val counts = splitCounts(now)
    val minha = userId?.let { cotaDe(it) }
    val myShare = when {
        minha == null -> null
        minha.status == StatusCota.PAGA -> SCShareState.Paid
        minha.isProcessing(now) -> SCShareState.Processing
        else -> SCShareState.Pending
    }
    val pagantes = reserva.cotas.filter { it.status == StatusCota.PAGA && it.atletaNome != null }
    return GameCardUi(
        sport = esporte,
        title = titulo,
        arenaName = arenaNome,
        courtName = quadraNome,
        dateTimeLabel = PtBrFormat.dateTimeRange(inicio, fim),
        status = status.tone(),
        paidCount = counts.paid,
        totalShares = numeroVagas,
        shareValue = minha?.valor ?: valorCotaConvidado,
        participants = pagantes.map { AvatarUi(it.atletaNome!!, paid = true) },
        holdExpiresAt = if (status == StatusReserva.AGUARDANDO_PAGAMENTOS) reserva.prazoCotas else null,
        coveredCount = counts.covered,
        processingCount = counts.processing,
        statusLabel = statusLabel,
        myShare = myShare,
        shareCaption = shareCaption,
        countdownLabel = countdownLabel,
        roleTag = roleTag,
    )
}

/**
 * Flags de sessao da UI (memoria do processo; em producao: DataStore + servidor).
 * - [onboardingDone]: onboarding visto;
 * - [celebrated]: jogos cuja celebracao "Fechou!" ja foi exibida (uma vez por pessoa por jogo, 10 §3.1);
 * - [openedInvites]: convites ja abertos neste aparelho (1a abertura, 10 §3.2).
 */
class UiSessionStore {
    val onboardingDone = MutableStateFlow(false)
    val celebrated = MutableStateFlow<Set<String>>(emptySet())
    val openedInvites = MutableStateFlow<Set<String>>(emptySet())

    fun markCelebrated(gameId: String) = celebrated.update { it + gameId }
    fun markInviteOpened(code: String) = openedInvites.update { it + code.uppercase() }
}
