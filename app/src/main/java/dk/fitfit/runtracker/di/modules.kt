package dk.fitfit.runtracker.di

import dk.fitfit.runtracker.data.LocationManager
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.db.RunDatabase
import dk.fitfit.runtracker.viewmodels.LocationUpdateViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

@JvmField
val databaseModule = module {
    single { RunDatabase.getDatabase(get()) }
    single { get<RunDatabase>().runDao() }
    single { get<RunDatabase>().locationDao() }
}

@JvmField
val repositoryModule = module {
    single { LocationRepository(get(), get(), get()) }
    single { LocationManager(get()) }
}

@JvmField
val viewModelModule = module {
    viewModel { LocationUpdateViewModel(get(), get()) }
}
