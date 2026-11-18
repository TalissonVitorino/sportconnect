package br.com.sportconnect.core.config

/**
 * Chaves de recursos ainda nao liberados. Passo a passo para ligar cada um em docs/12-checklist-publicacao.md.
 */
object FeatureFlags {
    /**
     * B8: "Continuar com Google/Apple" no login (09 ficha 2: "atras de flag"). Precisa de:
     * Android -> [GOOGLE_WEB_CLIENT_ID]; iOS -> capability "Sign in with Apple" no Xcode (conta paga).
     * Se o Google entrar no iOS, a App Store exige tambem o Apple (diretriz 4.8) — o iOS ja oferece Apple.
     */
    const val SOCIAL_LOGIN = false

    /** OAuth "Web client ID" do Google Cloud (nao o Android client ID). Vazio = Google indisponivel. */
    const val GOOGLE_WEB_CLIENT_ID = ""

    /**
     * E12: pagina que leva para a loja certa (Play/App Store) ou abre o app ja instalado.
     * `?j=<jogo>` deixa a pagina abrir o app direto no jogo. Ver docs/12-checklist-publicacao.md.
     */
    /**
     * M1 (doc 13): modelo dos tiles do mapa. OpenStreetMap serve para desenvolvimento e uso leve; antes de
     * publicar, troque por um provedor com chave (12 §14), ex.: MapTiler "https://api.maptiler.com/maps/streets-v2/256/{z}/{x}/{y}.png?key=SUA_CHAVE".
     */
    const val MAP_TILE_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"

    const val APP_DOWNLOAD_URL = "https://sportconnect.app/app"

    fun appLink(gameId: String?): String = if (gameId == null) APP_DOWNLOAD_URL else "$APP_DOWNLOAD_URL?j=$gameId"
}
