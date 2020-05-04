package dk.fitfit.runtracker.di

import dk.fitfit.runtracker.data.LocationManager
import dk.fitfit.runtracker.data.LocationRepository
import dk.fitfit.runtracker.data.RunRepository
import dk.fitfit.runtracker.data.db.RunDatabase
import dk.fitfit.runtracker.utils.RouteUtils
import dk.fitfit.runtracker.viewmodels.LocationUpdateViewModel
import dk.fitfit.runtracker.viewmodels.RunListViewModel
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
    single { LocationRepository(get(), get(), get(), get()) }
    single { LocationManager(get()) }
    single { RunRepository(get()) }
}

@JvmField
val viewModelModule = module {
    viewModel { LocationUpdateViewModel(get(), get()) }
    viewModel { RunListViewModel(get(), get()) }
}

@JvmField
val utils = module {
    single { RouteUtils() }
}
