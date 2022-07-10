package com.chantreck.alarmclock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chantreck.alarmclock.data.AlarmEntity
import com.chantreck.alarmclock.data.Database
import kotlinx.coroutines.launch

class MainViewModel(private val context: Application): AndroidViewModel(context) {
    private val dao = Database.getInstance(context).dao()

    val alarms: LiveData<List<AlarmEntity>> = dao.observeAll()

    private val _latestId = MutableLiveData<Int>()
    val latestId: LiveData<Int> get() = _latestId

    fun addAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = dao.addAlarm(alarm)
            _latestId.postValue(id.toInt())
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        val id = alarm.id
        val enabledNewValue = !alarm.enabled
        val enabled = if (enabledNewValue) 1 else 0

        viewModelScope.launch {
            dao.updateAlarm(id, enabled)
        }
    }
}