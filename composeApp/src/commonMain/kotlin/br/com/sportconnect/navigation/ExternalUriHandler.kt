package br.com.sportconnect.navigation

/**
 * Ponte entre os links que abrem o app (App Links no Android, Universal Links no iOS) e o NavController
 * (D5 do backlog 11). A plataforma chama [onNewUri]; o [AppNavHost] registra o [listener] e navega.
 * Se o link chega antes da UI existir (abertura a frio), ele fica guardado ate o listener aparecer.
 */
object ExternalUriHandler {
    private var cached: String? = null

    var listener: ((String) -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                cached?.let(value)
                cached = null
            }
        }

    fun onNewUri(uri: String) {
        val l = listener
        if (l != null) l(uri) else cached = uri
    }
}
