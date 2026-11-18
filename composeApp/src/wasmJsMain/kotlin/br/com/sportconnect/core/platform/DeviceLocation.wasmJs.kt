package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import br.com.sportconnect.core.model.GeoPonto

/** W2 (doc 13): a web usa a API de geolocalizacao do navegador (precisa de HTTPS ou localhost). */
private fun geolocationAvailable(): Boolean = js("typeof navigator !== 'undefined' && !!navigator.geolocation")

/** Precisao aproximada, ate 10 s, aceita posicao de ate 10 min atras. [onError] recebe o codigo (1 = negado). */
private fun requestPosition(onOk: (Double, Double) -> Unit, onError: (Int) -> Unit): Unit = js(
    """navigator.geolocation.getCurrentPosition(
        function (p) { onOk(p.coords.latitude, p.coords.longitude); },
        function (e) { onError(e.code); },
        { enableHighAccuracy: false, timeout: 10000, maximumAge: 600000 })""",
)

actual val deviceLocationSupported: Boolean = geolocationAvailable()

@Composable
actual fun rememberLocationRequest(onResult: (LocationResult) -> Unit): () -> Unit {
    val callback = rememberUpdatedState(onResult)
    return remember {
        {
            if (!deviceLocationSupported) {
                callback.value(LocationResult.Unavailable)
            } else {
                requestPosition(
                    { lat, lon -> callback.value(LocationResult.Found(GeoPonto(lat, lon))) },
                    { code -> callback.value(if (code == 1) LocationResult.Denied else LocationResult.Unavailable) },
                )
            }
        }
    }
}
