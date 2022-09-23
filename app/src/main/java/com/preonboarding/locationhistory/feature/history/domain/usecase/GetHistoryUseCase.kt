package com.preonboarding.locationhistory.feature.history.domain.usecase

import com.preonboarding.locationhistory.data.entity.History
import com.preonboarding.locationhistory.feature.history.domain.MapRepository
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val mapRepository: MapRepository
) {
    suspend operator fun invoke(date: String): List<History> =
        mapRepository.getHistoryFromDate(date)
}