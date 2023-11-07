package code.waiting_time_activity

import android.app.AlarmManager
import android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import code.waiting_time_activity.databinding.ActivitySurveyBinding
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class SurveyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyBinding
    var responseItem = ""
    var timeStart = ""
    var timeEnd = ""
    private val title = "Hi there"
    private val message = "Have you been waiting for something or someone since last report?"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        timeStart = LocalDateTime.now().toString()
        with(binding) {
            checkBoxCompanionOther.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) editTextCompanionOther.visibility = View.VISIBLE
                else editTextCompanionOther.visibility = View.GONE
            }
            checkBoxDeviceOther.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) editTextDeviceOther.visibility = View.VISIBLE
                else editTextDeviceOther.visibility = View.GONE
            }
        }
    }

    fun submit(view: View) {
        writeFile()
        updateStat()
        //cancelNextReminder()
        finishAffinity()
    }

    private fun updateStat() {
        val sharedPref = this.getSharedPreferences("File Uri", MODE_PRIVATE) ?: return
        if (sharedPref.getString("Last report date", "") != LocalDate.now().toString()) {
            with(sharedPref.edit()) {
                putInt("Today", 0)
                commit()
            }
        }
        val today = sharedPref.getInt("Today", 0) + 1
        val total = sharedPref.getInt("Total", 0) + 1
        val dateToday = LocalDate.now()
        with(sharedPref.edit()) {
            putInt("Total", total)
            putInt("Today", today)
            putString("Last report date", dateToday.toString())
            commit()
        }
    }

    private fun cancelNextReminder() {
        val timeNow = LocalTime.now()
        val dateToday = LocalDate.now()
        val timeInMinutes = timeNow.hour * 60 + timeNow.minute
        val day = dateToday.dayOfMonth
        val month = dateToday.monthValue - 1
        val year = dateToday.year
        val calendar = Calendar.getInstance()
        val hourInt = intArrayOf(11, 18, 21)
        val hourString = Array(3) { i -> "hour" + (i + 1) }
        val minuteString = Array(3) { i -> "minute" + (i + 1) }
        val sharedPref = this.getSharedPreferences("File Uri", Context.MODE_PRIVATE) ?: return
        val hour = Array(3) { i -> sharedPref.getInt(hourString[i], hourInt[i]) }
        val minute = Array(3) { i -> sharedPref.getInt(minuteString[i], 0) }
        val timeForReminders = Array(3) { i -> hour[i] * 60 + minute[i] }

        if (timeInMinutes < timeForReminders[0]) {
            calendar.set(year, month, day, hour[0], minute[0])
            updateReminder(calendar, 1)
            Toast.makeText(this, "Reminder 1 canceled", Toast.LENGTH_SHORT).show()
        } else if (timeInMinutes < timeForReminders[1]) {
            calendar.set(year, month, day, hour[1], minute[1])
            updateReminder(calendar, 2)
            Toast.makeText(this, "Reminder 2 canceled", Toast.LENGTH_SHORT).show()
        } else if (timeInMinutes < timeForReminders[2]) {
            calendar.set(year, month, day, hour[2], minute[2])
            updateReminder(calendar, 3)
            Toast.makeText(this, hour[2].toString()+":"+minute[2].toString(), Toast.LENGTH_SHORT).show()
        }


    }

    private fun updateReminder(calendar: Calendar, reminderNumber: Int) {
        val timeNextReminder = calendar.timeInMillis + INTERVAL_FIFTEEN_MINUTES
        //val timeNextReminder = Calendar.getInstance().timeInMillis + 30000
        val intent = Intent(applicationContext, Notification::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, reminderNumber.toString())
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            reminderNumber,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            timeNextReminder,
            INTERVAL_FIFTEEN_MINUTES,
            pendingIntent
        )
    }

    private fun writeFile() {
        timeEnd = LocalDateTime.now().toString()
        fun Boolean.toInt() = if (this) 1 else 0
        with(binding) {
            responseItem += "\nIs, $timeStart,$timeEnd,,${editTextObject.text},${editTextLocation.text},${checkBoxAlone.isChecked.toInt()},${checkBoxFriend.isChecked.toInt()},${checkBoxFamily.isChecked.toInt()},${checkBoxCompanionOther.isChecked.toInt()},${editTextCompanionOther.text},${checkBoxPhone.isChecked.toInt()},${checkBoxComputer.isChecked.toInt()},${checkBoxWatch.isChecked.toInt()},${checkBoxDeviceOther.isChecked.toInt()},${editTextDeviceOther.text},${editTextActivity.text},${editTextDuration.text},${editTextPreActivity.text}"

        }

        val sharedPref = this.getSharedPreferences("File Uri", Context.MODE_PRIVATE) ?: return
        val string1 = sharedPref.getString("URI", "")
        if (string1 != "") {
            val uri = Uri.parse(string1)
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            // Check for the freshest data.
            contentResolver.takePersistableUriPermission(uri, takeFlags)
            try {
                contentResolver.openFileDescriptor(uri!!, "wa")?.use {
                    FileOutputStream(it.fileDescriptor).use {
                        it.write(
                            (responseItem)
                                .toByteArray()
                        )
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                //Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                //Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show()
            }
            responseItem = ""
            Toast.makeText(this, "Response recorded", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(this, "Please uninstall and reinstall", Toast.LENGTH_SHORT).show()
    }


}