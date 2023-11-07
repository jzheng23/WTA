package code.waiting_time_activity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime

class NoActivity : AppCompatActivity() {

    var responseItem = ""
    var timeEnd = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        writeFile()
        finishAffinity()
    }

    private fun writeFile() {
        timeEnd = LocalDateTime.now().toString()
        responseItem += "\nNo,, $timeEnd"
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
        val  manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationID)
    }
}