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
}
