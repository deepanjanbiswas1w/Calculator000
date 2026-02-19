package com.example.premiumcalculator

import android.content.Context
import androidx.room.Room
import com.example.premiumcalculator.data.AppDatabase
import com.example.premiumcalculator.data.HistoryDao
import com.example.premiumcalculator.repository.HistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_db").build()
    }

    @Singleton
    @Provides
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()

    @Singleton
    @Provides
    fun provideHistoryRepository(dao: HistoryDao): HistoryRepository = HistoryRepository(dao)
}
