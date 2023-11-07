package code.waiting_time_activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import code.waiting_time_activity.databinding.ActivitySurvey3Binding
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

class SurveyActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivitySurvey3Binding
    var responseItem = ""
    var timeStart = ""
    var timeEnd = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurvey3Binding.inflate(layoutInflater)
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

    fun submit1(view: View) {
        writeFile()

        updateStat()

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

    private fun writeFile() {

        timeEnd = LocalDateTime.now().toString()
        fun Boolean.toInt() = if (this) 1 else 0
        with(binding) {
            responseItem += "\nNotification, $timeStart,$timeEnd,${editTextTime.text},${editTextObject.text},${editTextLocation.text},${checkBoxAlone.isChecked.toInt()},${checkBoxFriend.isChecked.toInt()},${checkBoxFamily.isChecked.toInt()},${checkBoxCompanionOther.isChecked.toInt()},${editTextCompanionOther.text},${checkBoxPhone.isChecked.toInt()},${checkBoxComputer.isChecked.toInt()},${checkBoxWatch.isChecked.toInt()},${checkBoxDeviceOther.isChecked.toInt()},${editTextDeviceOther.text},${editTextActivity.text},${editTextDuration.text},${editTextPreActivity.text}"
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
            } catch (e: IOException) {
                e.printStackTrace()
            }
            responseItem = ""
            Toast.makeText(this, "Response recorded", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(this, "Response not recorded", Toast.LENGTH_SHORT).show()
    }

    fun switchToSurvey1(view: View){
        val intent = Intent(this, SurveyActivity4::class.java)
        startActivity(intent)
        finish()
    }
}