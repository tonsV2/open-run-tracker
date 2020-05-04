package dk.fitfit.runtracker

import android.app.Application
import dk.fitfit.runtracker.di.databaseModule
import dk.fitfit.runtracker.di.repositoryModule
import dk.fitfit.runtracker.di.utils
import dk.fitfit.runtracker.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(databaseModule, repositoryModule, viewModelModule, utils)
        }
    }
}
