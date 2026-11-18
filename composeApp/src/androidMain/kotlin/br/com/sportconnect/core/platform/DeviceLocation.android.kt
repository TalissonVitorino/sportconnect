package br.com.sportconnect.core.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.os.Handler
import android.os.Build
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import br.com.sportconnect.core.model.GeoPonto

actual val deviceLocationSupported: Boolean = true

@Composable
actual fun rememberLocationRequest(onResult: (LocationResult) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback = rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) fetch(context) { callback.value(it) } else callback.value(LocationResult.Denied)
    }
    return {
        if (hasPermission(context)) {
            fetch(context) { callback.value(it) }
        } else {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }
}

private fun hasPermission(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

/**
 * Leitura atual (LocationManagerCompat, sem Play Services), preferindo o provedor "fused"/GPS. Se nao vier em
 * [TIMEOUT_MS] (provedor de rede sem resposta, GPS sem sinal), usa a ultima posicao conhecida; sem nenhuma,
 * Unavailable. Um "Buscando..." eterno e pior que uma posicao aproximada.
 */
@Suppress("MissingPermission")
private fun fetch(context: Context, onResult: (LocationResult) -> Unit) {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val all = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
        add(LocationManager.GPS_PROVIDER)
        add(LocationManager.NETWORK_PROVIDER)
    }
    val enabled = all.filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
    fun lastKnown(): LocationResult = (enabled + LocationManager.PASSIVE_PROVIDER)
        .firstNotNullOfOrNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
        ?.let { LocationResult.Found(GeoPonto(it.latitude, it.longitude)) }
        ?: LocationResult.Unavailable

    val provider = enabled.firstOrNull() ?: return onResult(lastKnown())
    var delivered = false
    val handler = Handler(Looper.getMainLooper())
    val cancel = android.os.CancellationSignal()
    fun deliver(r: LocationResult) {
        if (delivered) return
        delivered = true
        handler.removeCallbacksAndMessages(null)
        onResult(r)
    }
    handler.postDelayed({ cancel.cancel(); deliver(lastKnown()) }, TIMEOUT_MS)
    runCatching {
        LocationManagerCompat.getCurrentLocation(
            lm, provider, cancel, ContextCompat.getMainExecutor(context),
            androidx.core.util.Consumer<Location?> { loc ->
                deliver(if (loc != null) LocationResult.Found(GeoPonto(loc.latitude, loc.longitude)) else lastKnown())
            },
        )
    }.onFailure { deliver(lastKnown()) }
}

private const val TIMEOUT_MS = 8_000L
