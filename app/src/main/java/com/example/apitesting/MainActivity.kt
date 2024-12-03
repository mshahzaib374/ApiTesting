package com.example.apitesting

import android.app.ProgressDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.apitesting.databinding.ActivityMainBinding
import com.example.apitesting.retrofit.RetrofitClient
import com.example.apitesting.retrofit.VideoRequest
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var progressDialogs: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        progressDialogs = ProgressDialog(this)
        progressDialogs?.setMessage("Downloading...")


        binding?.apply {
            downloadBtn.setOnClickListener {
                val url = urlEt.text.toString().trim()
                if (!TextUtils.isEmpty(url)) {
                    progressDialogs?.show()
                    hitApi(url)
                }
            }
        }


    }


    private fun hitApi(url: String) {
        val videoRequest = VideoRequest(videoUrl = url)

        RetrofitClient.instance.getVideo(videoRequest)
            .enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: retrofit2.Response<ResponseBody>
                ) {
                    Log.d("TAG", "onResponse $response")
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val isSaved = saveVideoToDownloads(responseBody)
                            if (isSaved) {
                                progressDialogs?.dismiss()
                                Log.d("DownloadVideo", "Video saved successfully")
                            } else {
                                progressDialogs?.dismiss()
                                Log.e("DownloadVideo", "Failed to save video")
                            }
                        }
                    } else {
                        progressDialogs?.dismiss()
                        Log.e("DownloadVideo", "Response error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("DownloadVideo", "Failure: ${t.message}")
                }
            })

    }


    fun saveVideoToDownloads(responseBody: ResponseBody): Boolean {
        return try {
            // Get the Downloads directory path
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadsDir, "HDvideo")

            // Create the subfolder if it doesn't exist
            if (!targetDir.exists()) targetDir.mkdirs()

            // Create the file inside the subfolder
            val videoFile = File(targetDir, "downloaded_video.mp4") // Adjust the name as needed

            // Write the response to the file
            val inputStream: InputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(videoFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            progressDialogs?.dismiss()

            Log.d("SaveVideo", "File saved: ${videoFile.absolutePath}")
            true
        } catch (e: Exception) {
            progressDialogs?.dismiss()

            Log.e("SaveVideo", "Error saving file: ${e.message}")
            false
        }
    }

}