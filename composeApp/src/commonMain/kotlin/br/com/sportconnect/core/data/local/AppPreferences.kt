package br.com.sportconnect.core.data.local

import com.russhwolf.settings.Settings

/**
 * Preferencias locais do aparelho (multiplatform-settings: SharedPreferences no Android,
 * NSUserDefaults no iOS, java.util.prefs no desktop e localStorage na web).
 *
 * Guarda so o necessario para abrir o app no lugar certo (B3 do backlog 11): se o onboarding ja foi
 * visto e quem esta logado. Nada de dados sensiveis aqui; tokens reais vao para armazenamento seguro.
 */
class AppPreferences(private val settings: Settings) {

    var onboardingDone: Boolean
        get() = settings.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = settings.putBoolean(KEY_ONBOARDING_DONE, value)

    /** Sessao salva (demo): id, nome e celular de quem estava logado; null = deslogado. */
    var session: SavedSession?
        get() {
            val id = settings.getStringOrNull(KEY_SESSION_ID) ?: return null
            return SavedSession(
                id = id,
                nome = settings.getStringOrNull(KEY_SESSION_NAME).orEmpty(),
                celular = settings.getStringOrNull(KEY_SESSION_PHONE).orEmpty(),
                email = settings.getStringOrNull(KEY_SESSION_EMAIL),
            )
        }
        set(value) {
            if (value == null) {
                settings.remove(KEY_SESSION_ID)
                settings.remove(KEY_SESSION_NAME)
                settings.remove(KEY_SESSION_PHONE)
                settings.remove(KEY_SESSION_EMAIL)
            } else {
                settings.putString(KEY_SESSION_ID, value.id)
                settings.putString(KEY_SESSION_NAME, value.nome)
                settings.putString(KEY_SESSION_PHONE, value.celular)
                if (value.email == null) settings.remove(KEY_SESSION_EMAIL) else settings.putString(KEY_SESSION_EMAIL, value.email)
            }
        }

    /** Consentimentos LGPD aceitos neste aparelho (versao dos termos aceita e opt-in de marketing). */
    var acceptedTermsVersion: String?
        get() = settings.getStringOrNull(KEY_TERMS_VERSION)
        set(value) = if (value == null) settings.remove(KEY_TERMS_VERSION) else settings.putString(KEY_TERMS_VERSION, value)

    var marketingOptIn: Boolean
        get() = settings.getBoolean(KEY_MARKETING, false)
        set(value) = settings.putBoolean(KEY_MARKETING, value)

    /** "Meus esportes" (perfil/onboarding): nomes de [br.com.sportconnect.core.model.Sport]. */
    var favoriteSports: Set<String>
        get() = settings.getStringOrNull(KEY_SPORTS)?.split(',')?.filter { it.isNotBlank() }?.toSet().orEmpty()
        set(value) = settings.putString(KEY_SPORTS, value.joinToString(","))

    /** Onde buscar quadras (B10): coordenadas do aparelho ou do centro da cidade digitada. */
    var searchOrigin: SavedOrigin?
        get() {
            val lat = settings.getDoubleOrNull(KEY_LAT) ?: return null
            val lon = settings.getDoubleOrNull(KEY_LON) ?: return null
            return SavedOrigin(lat, lon, settings.getStringOrNull(KEY_CITY))
        }
        set(value) {
            if (value == null) {
                settings.remove(KEY_LAT); settings.remove(KEY_LON); settings.remove(KEY_CITY)
            } else {
                settings.putDouble(KEY_LAT, value.latitude)
                settings.putDouble(KEY_LON, value.longitude)
                if (value.city == null) settings.remove(KEY_CITY) else settings.putString(KEY_CITY, value.city)
            }
        }

    data class SavedOrigin(val latitude: Double, val longitude: Double, val city: String?)

    data class SavedSession(val id: String, val nome: String, val celular: String, val email: String? = null)

    private companion object {
        const val PREFIX = "sportconnect."
        const val KEY_ONBOARDING_DONE = PREFIX + "onboarding_done"
        const val KEY_SESSION_ID = PREFIX + "session_user_id"
        const val KEY_SESSION_NAME = PREFIX + "session_user_name"
        const val KEY_SESSION_PHONE = PREFIX + "session_user_phone"
        const val KEY_SESSION_EMAIL = PREFIX + "session_user_email"
        const val KEY_TERMS_VERSION = PREFIX + "terms_version"
        const val KEY_MARKETING = PREFIX + "marketing_opt_in"
        const val KEY_SPORTS = PREFIX + "favorite_sports"
        const val KEY_LAT = PREFIX + "origin_lat"
        const val KEY_LON = PREFIX + "origin_lon"
        const val KEY_CITY = PREFIX + "origin_city"
    }
}
