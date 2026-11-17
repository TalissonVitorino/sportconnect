package br.com.sportconnect.feature.arenaadmin.common

import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.model.PapelUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Estado de carregamento comum as telas da arena. */
enum class LoadState { Loading, Ready, Error, NoArena }

/** Arena do usuario logado (gestor/operador). Emite null se o usuario nao tem papel de arena. */
fun AuthRepository.observeArenaId(): Flow<String?> = currentUser.map { u ->
    u?.arenaId?.takeIf { PapelUsuario.GESTOR_ARENA in u.papeis || PapelUsuario.OPERADOR_ARENA in u.papeis }
}.distinctUntilChanged()

/** Operador nao ve valores financeiros (10 §2.17 acessibilidade / §2.21). */
fun AuthRepository.isManager(): Boolean = currentUser.value?.papeis?.contains(PapelUsuario.GESTOR_ARENA) == true
