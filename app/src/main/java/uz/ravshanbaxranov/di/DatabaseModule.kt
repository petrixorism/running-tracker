package uz.ravshanbaxranov.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import uz.ravshanbaxranov.data.RunRepositoryImpl
import uz.ravshanbaxranov.data.db.RunDao
import uz.ravshanbaxranov.data.db.RunDatabase
import uz.ravshanbaxranov.domain.RunRepository
import uz.ravshanbaxranov.domain.usecase.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @[Provides Singleton]
    fun provideTrackingRepository(
        db: RunDatabase,
        dataStore: DataStore<Preferences>
    ): RunRepository =
        RunRepositoryImpl(db.dao, dataStore)

    @[Provides Singleton]
    fun provideTrackUseCases(repository: RunRepository): RunUseCase {
        return RunUseCase(
            addRun = AddRun(repository),
            deleteRun = DeleteRun(repository),
            getRuns = GetRuns(repository),
            getTotalAndAvgData = GetTotalAndAvgData(repository)
        )
    }


    @[Provides Singleton]
    fun provideWordInfoDatabase(app: Application): RunDatabase {
        return Room
            .databaseBuilder(app, RunDatabase::class.java, "run_db")
            .allowMainThreadQueries()
            .build()
    }

    @[Provides Singleton]
    fun provideDao(db: RunDatabase): RunDao = db.dao


    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile("user_preferences") }
        )
    }


}