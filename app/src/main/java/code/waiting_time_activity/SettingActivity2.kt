package code.waiting_time_activity

import android.app.*
import android.app.AlarmManager.INTERVAL_DAY
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import code.waiting_time_activity.databinding.ActivitySetting2Binding
import java.time.LocalDateTime
import java.util.*


class SettingActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivitySetting2Binding
    private val title = "Hi there"
    private val message = "Have you been waiting for anything since the last report?"

    private val minDiff = 180
    private var time1: Long = 0
    private var time2: Long = 0
    private var time3: Long = 0
    private var reminderMode = 2
    private val hourInt = intArrayOf(11, 18, 21)
    private val hourString = Array(3) { i -> "hour" + (i + 1) }
    private val minuteString = Array(3) { i -> "minute" + (i + 1) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetting2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()
        with(binding) {


            submitButton.setOnClickListener { scheduleNotification() }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                val radio: RadioButton? = findViewById(checkedId)
                when (radio?.text) {
                    "Off" -> {

                        reminderMode = 0
                        setAlarm(0)
                    }
                    "Lazy" -> {

                        reminderMode = 1

                    }
                    else -> {

                        reminderMode = 2

                    }
                }
                saveReminderMode()
            }
        }
        setReminderTime()
    }

    private fun saveReminderMode() {
        val sharedPref = this.getSharedPreferences("File Uri", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt("reminderModeString", reminderMode)
            commit()
        }

    }


    private fun setReminderTime() {
        val sharedPref = this.getSharedPreferences("File Uri", Context.MODE_PRIVATE) ?: return
        val hour = Array(3) { i -> sharedPref.getInt(hourString[i], hourInt[i]) }
        val minute = Array(3) { i -> sharedPref.getInt(minuteString[i], 0) }
        reminderMode = sharedPref.getInt("reminderModeString", 2)
        when (reminderMode) {
            0 -> {
                binding.radioGroup.check(R.id.radioButton)
            }
            1 -> {
                binding.radioGroup.check(R.id.radioButton2)
            }
            2 -> {
                binding.radioGroup.check(R.id.radioButton3)
            }
        }
        val tp = arrayOf(binding.timePicker1, binding.timePicker2, binding.timePicker3)

        for (i in 0..2) {
            tp[i].hour = hour[i]
            tp[i].minute = minute[i]
        }
    }

    fun resetReminderTime(view: View) {
        val tp = arrayOf(binding.timePicker1, binding.timePicker2, binding.timePicker3)
        for (i in 0..2) {
            tp[i].hour = hourInt[i]
            tp[i].minute = 0
        }

    }


    private fun scheduleNotification() {
        getTime()
        checkTime()

    }

    private fun checkTime() {
        //TODO("Within certain range")

        val diff1 = (time2 - time1) / 60000
        val diff2 = (time3 - time2) / 60000
        if (diff1 < minDiff) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(
                    "Reminder 2 should be 3 hours later than Reminder 1"
                )
                .setPositiveButton("Okay") { _, _ -> }
                .show()
        } else if (diff2 < minDiff) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(
                    "Reminder 3 should be 3 hours later than Reminder 2"
                )
                .setPositiveButton("Okay") { _, _ -> }
                .show()
        } else {
            setAlarm(reminderMode)
        }
    }

    /*
    fun postponeAlarm(view: View){
        val timeNow = LocalTime.now()
        val dateToday = LocalDate.now()
        val timeInMinutes = timeNow.hour * 60 + timeNow.minute
        val sharedPref = this.getSharedPreferences("File Uri", Context.MODE_PRIVATE) ?: return
        val hour = Array(3) { i -> sharedPref.getInt(hourString[i], hourInt[i]) }
        val minute = Array(3) { i -> sharedPref.getInt(minuteString[i], 0) }
        val timeForReminders = Array(3) { i -> hour[i] * 60 + minute[i] }
        val day = dateToday.dayOfMonth
        val month = dateToday.monthValue -1
        val year = dateToday.year
        val calendar = Calendar.getInstance()

        if (timeInMinutes < timeForReminders[0]) {
            calendar.set(year, month, day, hour[0], minute[0])
            updateReminder(calendar, 1)
            Toast.makeText(this, "Reminder 1 pushed to " + hour[0].toString()+":"+minute[0].toString(), Toast.LENGTH_SHORT).show()
        } else if (timeInMinutes < timeForReminders[1]) {
            calendar.set(year, month, day, hour[1], minute[1])
            updateReminder(calendar, 2)
            Toast.makeText(this, "Reminder 2 pushed to " + hour[1].toString()+":"+minute[1].toString(), Toast.LENGTH_SHORT).show()
        } else if (timeInMinutes < timeForReminders[2]) {
            calendar.set(year, month, day, hour[2], minute[2])
            updateReminder(calendar, 3)
            Toast.makeText(this, "Reminder 3 pushed to " + hour[2].toString()+":"+minute[2].toString(), Toast.LENGTH_SHORT).show()
        }
        //Toast.makeText(this, Calendar.getInstance().get(Calendar.HOUR).toString()+Calendar.getInstance().get(Calendar.MONTH).toString()+Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString(), Toast.LENGTH_SHORT).show()
        //Toast.makeText(this, calendar.get(Calendar.YEAR).toString()+calendar.get(Calendar.MONTH).toString()+calendar.get(Calendar.DAY_OF_MONTH).toString(), Toast.LENGTH_SHORT).show()

    }

    private fun updateReminder(calendar: Calendar, reminderNumber: Int) {
        val timeNextReminder = calendar.timeInMillis
        //val timeNextReminder = Calendar.getInstance().timeInMillis + 30000
        val intent = Intent(applicationContext, Notification::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, "Knock knock")
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            reminderNumber,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            //Calendar.getInstance().timeInMillis+5000,
            timeNextReminder,
            pendingIntent
        )
    }

     */

    fun testAlarm(view: View) {
        val intent = Intent(applicationContext, Notification::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, "Testing")


        val pendingIntent0 = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        val alarmManager0 = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager0.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            Calendar.getInstance().timeInMillis,
            //INTERVAL_DAY,
            pendingIntent0
        )

        Toast.makeText(this, "Reminder will show up soon", Toast.LENGTH_SHORT).show()

    }

    private fun setAlarm(reminderMode: Int) {


        val alarmManagers = Array(24) {
            getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
        val intents = Array(24) {
            Intent(applicationContext, Notification::class.java)
        }
        for (i in 0..23) {
            intents[i].putExtra(titleExtra, title)
            intents[i].putExtra(messageExtra, message)
        }

        val timeArray1 = Array(8) { i ->
            time1 + INTERVAL_DAY * i
        }
        val timeArray2 = Array(8) { i ->
            time2 + INTERVAL_DAY * i
        }
        val timeArray3 = Array(8) { i ->
            time3 + INTERVAL_DAY * i
        }
        val timeArray = timeArray1 + timeArray2 + timeArray3

        val requestCodes = Array(24) { i -> i + 1 }
        val pendingIntents = Array(24) { i ->
            PendingIntent.getBroadcast(
                applicationContext,
                requestCodes[i],
                intents[i],
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        when (reminderMode) {
            2 -> {
                for (i in 0..23) {
                    alarmManagers[i].setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeArray[i],
                        pendingIntents[i]
                    )
                }
                Toast.makeText(this, "Reminders set", Toast.LENGTH_SHORT).show()
            }
            0 -> {
                for (i in 0..23) {
                    alarmManagers[i].cancel(pendingIntents[i])
                }
                Toast.makeText(this, "Reminders disabled", Toast.LENGTH_SHORT).show()

            }
            else -> {
                alarmManagers[0].setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    time1,
                    INTERVAL_DAY,
                    pendingIntents[0]
                )

                alarmManagers[1].setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    time2,
                    INTERVAL_DAY,
                    pendingIntents[1]
                )

                alarmManagers[2].setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    time3,
                    INTERVAL_DAY,
                    pendingIntents[2]
                )
                Toast.makeText(this, "Reminders set", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun getTime() {
        val time = LocalDateTime.now()
        val minute1 = binding.timePicker1.minute
        val hour1 = binding.timePicker1.hour
        val minute2 = binding.timePicker2.minute
        val hour2 = binding.timePicker2.hour
        val minute3 = binding.timePicker3.minute
        val hour3 = binding.timePicker3.hour
        val day = time.dayOfMonth
        val month = time.monthValue - 1
        val year = time.year

        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        val calendar3 = Calendar.getInstance()

        calendar1.set(year, month, day, hour1, minute1)
        calendar2.set(year, month, day, hour2, minute2)
        calendar3.set(year, month, day, hour3, minute3)

        time1 = calendar1.timeInMillis
        time2 = calendar2.timeInMillis
        time3 = calendar3.timeInMillis

        val sharedPref = this.getSharedPreferences("File Uri", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt("hour1", hour1)
            putInt("hour2", hour2)
            putInt("hour3", hour3)
            putInt("minute1", minute1)
            putInt("minute2", minute2)
            putInt("minute3", minute3)
            commit()
        }

    }

    private fun createNotificationChannel() {
        val name = "Notification Channel"
        val desc = "A Description of the Channel"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    fun shareFile(view: View) { // pass the file path where the actual file is located.
        val sharedPref = this.getSharedPreferences("File Uri", Context.MODE_PRIVATE) ?: return
        val string1 = sharedPref.getString("URI", "")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type =
                "text/plain" // "*/*" will accepts all types of files, if you want specific then change it on your need.
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(
                Intent.EXTRA_SUBJECT,
                "WTA-ESM data"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "Sharing file from the WTA-ESM"
            )
            putExtra(Intent.EXTRA_STREAM, Uri.parse(string1))
        }
        startActivity(shareIntent)
    }

}