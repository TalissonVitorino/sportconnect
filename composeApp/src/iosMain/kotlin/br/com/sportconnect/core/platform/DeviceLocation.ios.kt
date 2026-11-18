package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import br.com.sportconnect.core.model.GeoPonto
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyKilometer
import platform.Foundation.NSError
import platform.darwin.NSObject

actual val deviceLocationSupported: Boolean = true

@Composable
actual fun rememberLocationRequest(onResult: (LocationResult) -> Unit): () -> Unit {
    val callback = rememberUpdatedState(onResult)
    val helper = remember { IosLocationHelper() }
    return { helper.request { callback.value(it) } }
}

/** CLLocationManager com permissao "durante o uso" (texto em Info.plist: NSLocationWhenInUseUsageDescription). */
@OptIn(ExperimentalForeignApi::class)
private class IosLocationHelper : NSObject(), CLLocationManagerDelegateProtocol {
    private val manager = CLLocationManager()
    private var pending: ((LocationResult) -> Unit)? = null

    init {
        manager.delegate = this
        manager.desiredAccuracy = kCLLocationAccuracyKilometer
    }

    fun request(onResult: (LocationResult) -> Unit) {
        pending = onResult
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusNotDetermined -> manager.requestWhenInUseAuthorization()
            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> finish(LocationResult.Denied)
            else -> manager.requestLocation()
        }
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        if (pending == null) return
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> manager.requestLocation()
            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> finish(LocationResult.Denied)
            else -> Unit
        }
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val loc = didUpdateLocations.lastOrNull() as? CLLocation ?: return finish(LocationResult.Unavailable)
        val point = loc.coordinate.useContents { GeoPonto(latitude, longitude) }
        finish(LocationResult.Found(point))
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        finish(LocationResult.Unavailable)
    }

    private fun finish(result: LocationResult) {
        val cb = pending ?: return
        pending = null
        cb(result)
    }
}
