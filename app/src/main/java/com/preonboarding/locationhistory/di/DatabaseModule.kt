package com.preonboarding.locationhistory.di

import android.content.Context
import com.preonboarding.locationhistory.data.database.MapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMapDao(appDatabase: MapDatabase) = appDatabase.mapDao()

    @Provides
    @Singleton
    fun provideMapDatabase(
        @ApplicationContext context: Context
    ): MapDatabase = MapDatabase.getInstance(context)

}