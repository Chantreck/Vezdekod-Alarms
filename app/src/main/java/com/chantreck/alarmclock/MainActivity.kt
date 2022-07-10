package com.chantreck.alarmclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chantreck.alarmclock.data.AlarmEntity
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val days = listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс")
    private lateinit var mediaPlayer: MediaPlayer

    companion object {
        var ins: MainActivity? = null
        fun getInstance(): MainActivity? {
            return ins
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.clock)
        ins = this

        setContent {
            SetUI()
        }
    }

    @Composable
    fun SetUI() {
        val (showDialog, setShowDialog) = remember { mutableStateOf(false) }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        setShowDialog(true)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "")
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                TopAppBar {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Будильники", fontSize = 18.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { mediaPlayer.stop() }) {
                        Text(text = "Stop")
                    }
                }

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val liveData = viewModel.alarms
                    val alarms by liveData.observeAsState(initial = listOf())

                    for (alarm in alarms) {
                        ClockCard(alarm)
                    }
                }
            }
        }

        ShowDialog(showDialog, setShowDialog)
    }

    @Composable
    fun ClockCard(alarm: AlarmEntity) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column {
                    Text(text = alarm.time, fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = alarm.days, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = alarm.enabled, onCheckedChange = { viewModel.updateAlarm(alarm) },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }

    /* Dialog */

    @Composable
    fun ShowDialog(
        showDialog: Boolean,
        setShowDialog: (Boolean) -> Unit,
    ) {
        val time = remember { mutableStateOf("12:00") }
        val selectedDays = remember { mutableStateOf(List(7) { false }) }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text("Добавить будильник")
                },
                confirmButton = {
                    Button(onClick = {
                        val entity = AlarmEntity(
                            time = time.value,
                            days = daysToString(selectedDays)
                        )
                        viewModel.addAlarm(entity)

                        viewModel.latestId.observe(this) { id ->
                            println(id)
                            setAlarm(time.value, id)
                        }

                        clear(time, selectedDays, setShowDialog)
                    }) {
                        Text("Добавить")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        clear(time, selectedDays, setShowDialog)
                    }) {
                        Text("Отменить")
                    }
                },
                text = {
                    AlarmContent(time, selectedDays)
                }
            )
        }
    }

    @Composable
    fun AlarmContent(time: MutableState<String>, selectedDays: MutableState<List<Boolean>>) {
        val hours = time.value.take(2).toInt()
        val minutes = time.value.takeLast(2).toInt()

        val timePickerDialog = TimePickerDialog(
            LocalContext.current,
            { _, hour, minute ->
                time.value = getTime(hour, minute)
            }, hours, minutes, true
        )

        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = time.value, fontSize = 32.sp)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(text = "Выбрать время")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until 7) {
                    Box(
                        Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                    ) {
                        DayPicker(i, selectedDays)
                    }
                    if (i < 6) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun DayPicker(index: Int, selectedDays: MutableState<List<Boolean>>) {
        val isSelected = selectedDays.value[index]
        val color = if (isSelected) Color.Blue else Color.DarkGray

        Button(
            onClick = {
                val newList = selectedDays.value.toMutableList()
                newList[index] = !isSelected
                selectedDays.value = newList
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
            shape = RoundedCornerShape(50),
            elevation = null,
            border = BorderStroke(1.dp, color)
        ) {
            Text(
                text = days[index].take(1),
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }

    private fun getTime(hour: Int, minute: Int): String {
        val hourStr = if (hour < 10) "0$hour" else "$hour"
        val minuteStr = if (minute < 10) "0$minute" else "$minute"

        return "$hourStr:$minuteStr"
    }

    private fun daysToString(selectedDays: MutableState<List<Boolean>>): String {
        val result = mutableListOf<String>()

        for (i in 0 until 7) {
            if (selectedDays.value[i]) {
                result.add(days[i])
            }
        }

        return result.joinToString(", ")
    }

    private fun setAlarm(time: String, id: Int) {
        val hours = time.take(2).toInt()
        val minutes = time.takeLast(2).toInt()

        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0)

        alarmIntent.putExtra("ID", id)

        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
        }

        manager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY, pendingIntent
        )
    }

    private fun clear(
        time: MutableState<String>,
        selectedDays: MutableState<List<Boolean>>,
        setShowDialog: (Boolean) -> Unit
    ) {
        time.value = "12:00"
        selectedDays.value = List(7) { false }

        setShowDialog(false)
    }

    fun onAlarmStart() {
        mediaPlayer.start()
    }
}