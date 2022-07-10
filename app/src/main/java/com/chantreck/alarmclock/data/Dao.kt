package com.chantreck.alarmclock.data

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao

@Dao
interface Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAlarm(alarm: AlarmEntity): Long

    @Query("SELECT * FROM alarm")
    fun observeAll(): LiveData<List<AlarmEntity>>

    @Query("UPDATE alarm SET enabled = :enabled WHERE id = :id")
    suspend fun updateAlarm(id: Int, enabled: Int)
}