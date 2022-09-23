package com.preonboarding.locationhistory.feature.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preonboarding.locationhistory.LocationHistoryApp
import com.preonboarding.locationhistory.data.entity.History
import com.preonboarding.locationhistory.feature.history.domain.usecase.GetHistoryUseCase
import com.preonboarding.locationhistory.feature.history.domain.usecase.SaveHistoryUseCase
import com.preonboarding.locationhistory.feature.map.domain.DialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val saveHistoryUseCase: SaveHistoryUseCase
) : ViewModel() {

    private val _historyFromDate = MutableStateFlow<List<History>>(emptyList())
    val historyFromDate = _historyFromDate.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState>(DialogState())
    val dialogState = _dialogState.asStateFlow()

    private val _selectedMarker = MutableSharedFlow<Int>()
    val selectedMarker = _selectedMarker.asSharedFlow()

    private val _setTime = MutableLiveData<String>()
    val setTime: LiveData<String>
        get() = _setTime

    init {
        getSetTime()
    }

    fun getHistoryFromDate(date: String) {
        viewModelScope.launch {
            _historyFromDate.update {
                getHistoryUseCase(date)
            }
        }
    }

    fun saveHistory(date: Long, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            saveHistoryUseCase(date, latitude, longitude)
        }
    }

    private fun getSetTime() {
        viewModelScope.launch {
            _setTime.value = LocationHistoryApp.prefs.setTime
        }
    }

    fun selectMarker(markerId: Int) {
        viewModelScope.launch {
            _selectedMarker.emit(markerId)
        }
    }

    fun setDialogState(state: Boolean, tag: Int = 0) {
        viewModelScope.launch {
            _dialogState.update {
                if (tag != 0) {
                    _dialogState.value.copy(isDialogShowed = state, dialogTag = tag)
                } else {
                    _dialogState.value.copy(isDialogShowed = state)
                }
            }
        }
    }

}