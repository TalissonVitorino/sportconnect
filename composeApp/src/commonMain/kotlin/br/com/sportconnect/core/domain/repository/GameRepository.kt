package br.com.sportconnect.core.domain.repository

import br.com.sportconnect.core.model.Convite
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.ListaEspera
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    /** Todos os jogos, em tempo real (progresso das cotas etc.). */
    fun observeGames(): Flow<List<Jogo>>
    /** Um jogo em tempo real; emite null se nao existir. */
    fun observeGame(id: String): Flow<Jogo?>
    suspend fun game(id: String): Jogo?
    suspend fun save(jogo: Jogo)
    /** Atualizacao atomica; retorna o jogo salvo (ou null se nao existir). */
    suspend fun update(id: String, transform: (Jogo) -> Jogo): Jogo?

    suspend fun conviteByCodigo(codigo: String): Convite?
    suspend fun saveConvite(convite: Convite)

    fun observeWaitlist(jogoId: String): Flow<List<ListaEspera>>
    suspend fun saveWaitlist(jogoId: String, fila: List<ListaEspera>)

    fun novoId(prefixo: String): String
}
