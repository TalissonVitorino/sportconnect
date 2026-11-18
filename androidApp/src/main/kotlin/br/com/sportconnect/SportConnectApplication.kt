package br.com.sportconnect

import android.app.Application
import br.com.sportconnect.di.initKoin
import org.koin.android.ext.koin.androidContext

class SportConnectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@SportConnectApplication) }
    }
}
