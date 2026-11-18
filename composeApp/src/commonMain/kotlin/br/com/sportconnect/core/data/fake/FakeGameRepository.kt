package br.com.sportconnect.core.data.fake

import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.model.Convite
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.ListaEspera
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Jogos em memoria com [MutableStateFlow]: cada mudanca (ex.: webhook de pagamento) e emitida em tempo real. */
class FakeGameRepository(seed: DemoData) : GameRepository {
    /** Fake: simula a ida e volta ao servidor; os dados ja estao atualizados em memoria. */
    override suspend fun sincronizar() = kotlinx.coroutines.delay(500)


    private val jogos = MutableStateFlow<Map<String, Jogo>>(LinkedHashMap(seed.jogos.associateBy { it.id }))
    private val convites = MutableStateFlow(seed.convites.associateBy { it.codigoCurto.uppercase() })
    private val filas = MutableStateFlow<Map<String, List<ListaEspera>>>(emptyMap())
    private var contador = 0

    override fun observeGames(): Flow<List<Jogo>> = jogos.map { it.values.toList() }
    override fun observeGame(id: String): Flow<Jogo?> = jogos.map { it[id] }.distinctUntilChanged()
    override suspend fun game(id: String): Jogo? = jogos.value[id]

    override suspend fun save(jogo: Jogo) {
        jogos.update { it + (jogo.id to jogo) }
    }

    override suspend fun update(id: String, transform: (Jogo) -> Jogo): Jogo? {
        var resultado: Jogo? = null
        jogos.update { mapa ->
            val atual = mapa[id] ?: return@update mapa
            transform(atual).also { resultado = it }.let { mapa + (id to it) }
        }
        return resultado
    }

    override suspend fun conviteByCodigo(codigo: String): Convite? = convites.value[codigo.uppercase()]

    override suspend fun saveConvite(convite: Convite) {
        convites.update { it + (convite.codigoCurto.uppercase() to convite) }
    }

    override fun observeWaitlist(jogoId: String): Flow<List<ListaEspera>> =
        filas.map { it[jogoId].orEmpty() }.distinctUntilChanged()

    override suspend fun saveWaitlist(jogoId: String, fila: List<ListaEspera>) {
        filas.update { it + (jogoId to fila) }
    }

    override fun novoId(prefixo: String): String = "$prefixo-novo-${++contador}"
}
