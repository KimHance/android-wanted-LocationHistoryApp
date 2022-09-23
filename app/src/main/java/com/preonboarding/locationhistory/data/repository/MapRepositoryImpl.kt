package com.preonboarding.locationhistory.data.repository

import com.preonboarding.locationhistory.data.database.MapDao
import com.preonboarding.locationhistory.data.entity.History
import com.preonboarding.locationhistory.feature.history.domain.MapRepository
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val mapDao: MapDao
) : MapRepository {

    override suspend fun getHistoryFromDate(date: String): List<History> {
        return runCatching {
            mapDao.getHistoryFromDate(date)
        }.getOrThrow()
    }

    override suspend fun saveHistory(history: History) {
        runCatching {
            mapDao.saveHistory(history)
        }.onFailure { e ->
            throw  e
        }
    }
}