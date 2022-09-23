package com.preonboarding.locationhistory.feature.history.domain.usecase

import com.preonboarding.locationhistory.data.entity.History
import com.preonboarding.locationhistory.data.entity.toFormatDate
import com.preonboarding.locationhistory.data.entity.toFormatTime
import com.preonboarding.locationhistory.feature.history.domain.MapRepository
import javax.inject.Inject

class SaveHistoryUseCase @Inject constructor(
    private val mapRepository: MapRepository
) {
    suspend operator fun invoke(date: Long, latitude: Double, longitude: Double) {
        val history = History(date.toFormatTime(), date.toFormatDate(), latitude, longitude)
        mapRepository.saveHistory(history)
    }
}