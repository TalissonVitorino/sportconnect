package br.com.sportconnect.core.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
enum class CanalConvite { WHATSAPP, COPIADO, NATIVO, OUTRO }

@Serializable
enum class StatusConvite { ATIVO, ENCERRADO }

@Serializable
data class Convite(
    val id: String,
    val jogoId: String,
    val codigoCurto: String,
    val url: String,
    val canal: CanalConvite = CanalConvite.WHATSAPP,
    val status: StatusConvite = StatusConvite.ATIVO,
    val aberturas: Int = 0,
)

@Serializable
enum class StatusListaEspera { AGUARDANDO, NOTIFICADO, CONVERTIDO, EXPIRADO, SAIU }

/** Entrada na lista de espera (RN-27). */
@Serializable
data class ListaEspera(
    val id: String,
    val jogoId: String,
    val usuarioId: String,
    val usuarioNome: String,
    val posicao: Int,
    val entrouEm: Instant,
    val status: StatusListaEspera = StatusListaEspera.AGUARDANDO,
    val notificadoEm: Instant? = null,
    val exclusividadeAte: Instant? = null,
)
