package code.waiting_time_activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import code.waiting_time_activity.databinding.ActivityMainBinding
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private val createFile = 0
    private lateinit var binding: ActivityMainBinding
    private var countText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkFirstRun()

    }

    override fun onResume() {
        super.onResume()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //checkFirstRun()
        checkNewDay()
        val sharedPref = this.getSharedPreferences("File Uri", MODE_PRIVATE) ?: return
        countText =
            "Today: " + sharedPref.getInt("Today", 0) + "; Total: " + sharedPref.getInt("Total", 0)
        with(binding) {
            countTextView.text = countText
        }
    }

    private fun checkNewDay() {
        val sharedPref = this.getSharedPreferences("File Uri", MODE_PRIVATE) ?: return
        if (sharedPref.getString("Last report date", "") != LocalDate.now().toString()) {
            with(sharedPref.edit()) {
                putInt("Today", 0)
                commit()
            }
        }
    }

    private fun createDataFile() {
        AlertDialog.Builder(this)
            .setTitle("First-time Run")
            .setMessage(
                "Please create the data file"
            )
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, "WaitingData.txt")
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, "")
                }
                startActivityForResult(intent, createFile)
            }
            .show()

    }

    private fun checkFirstRun() {
        val sharedPref = this.getSharedPreferences("File Uri", MODE_PRIVATE) ?: return
        if (sharedPref.getBoolean("FirstRun", true)) {

            AlertDialog.Builder(this)
                .setTitle("First-time Run")
                .setMessage(
                    "Please create the data file"
                )
                .setPositiveButton("OK") { _, _ ->
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, "WaitingData.txt")
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, "")
                    }
                    startActivityForResult(intent, createFile)
                }
                .show()
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        var uri: Uri = Uri.parse("")
        if (requestCode == createFile && resultCode == RESULT_OK) {
            uri = data!!.data!!
        }

        val sharedPref = this.getSharedPreferences("File Uri", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("URI", uri.toString())
            putBoolean("FirstRun", false)
            commit()
        }


        val variableName =
            "Status,TimeStart,TimeEnd,Time,Object,Location,Alone,Friend,Family,OtherCompanion,CompanionDescription,Phone,Computer,Watch,OtherDevice,DeviceDescription,Activity,Duration,PreActivity"
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        // Check for the freshest data.
        contentResolver.takePersistableUriPermission(uri, takeFlags)
        try {
            contentResolver.openFileDescriptor(uri!!, "wa")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(
                        (variableName)
                            .toByteArray()
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            //Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(this, "Variable name created", Toast.LENGTH_SHORT).show()


    }

    fun amWaiting(view: View) {
        val intent = Intent(this, SurveyActivity::class.java)
        startActivity(intent)
    }

    fun wasWaiting(view: View) {
        val intent = Intent(this, SurveyActivity2::class.java)
        startActivity(intent)
    }

    fun setting(view: View) {
        val intent = Intent(this, SettingActivity2::class.java)
        startActivity(intent)
    }

    fun help(view: View) {
        val intent = Intent(this, HelpActivity2::class.java)
        startActivity(intent)
    }
}














