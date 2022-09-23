package com.preonboarding.locationhistory.feature.history.domain

import com.preonboarding.locationhistory.data.entity.History

interface MapRepository {

    suspend fun getHistoryFromDate(date: String): List<History>

    suspend fun saveHistory(history: History)
}