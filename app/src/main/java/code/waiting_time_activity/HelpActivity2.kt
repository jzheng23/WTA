package code.waiting_time_activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.widget.Toast
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

private val openURL = Intent(Intent.ACTION_VIEW)
private val createFile = 0

class HelpActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help2)
    }

    fun openSlides() {
        openURL.data = Uri.parse("https://docs.google.com/presentation/d/1Z3qE59YHh9t3JsOwvF4S7cKhZWrfY7Cgjcvy27l4Ctk/edit?usp=sharing")
        startActivity(openURL)
    }
    fun openVideo() {
        openURL.data = Uri.parse("https://www.youtube.com/playlist?list=PLYaGgzzPYCk4e28QikvxLus-v0K-C60lZ")
        startActivity(openURL)
    }
    fun openWebsite() {
        openURL.data = Uri.parse("https://sites.google.com/umd.edu/wta/home")
        startActivity(openURL)
    }

    fun openPrivacyPolicy() {
        openURL.data = Uri.parse("https://www.jzheng.net/privacy-policy/")
        startActivity(openURL)
    }

    fun reCreateFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "WaitingData.txt")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, "")
        }
        startActivityForResult(intent, createFile)
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


        var variableName: String =
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

    fun resetToday(view: View){
        val sharedPref = this.getSharedPreferences("File Uri", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt("Today", 0)
            commit()
        }
    }
}

