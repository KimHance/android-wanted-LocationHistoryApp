package com.preonboarding.locationhistory.di

import com.preonboarding.locationhistory.data.repository.MapRepositoryImpl
import com.preonboarding.locationhistory.feature.history.domain.MapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindMapRepository(
        repository: MapRepositoryImpl
    ): MapRepository
}