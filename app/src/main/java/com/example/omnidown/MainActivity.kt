package com.example.omnidown

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

interface ProgressCallback {
    fun invoke(progressStr: String)
}

class MainActivity : AppCompatActivity() {

    private lateinit var etUrl: TextInputEditText
    private lateinit var btnAnalyze: Button
    private lateinit var cvVideoInfo: View
    private lateinit var ivThumbnail: android.widget.ImageView
    private lateinit var tvVideoTitle: TextView
    private lateinit var tvVideoDuration: TextView
    private lateinit var spinnerFormats: Spinner
    private lateinit var btnDownload: Button
    private lateinit var progressLayout: View
    private lateinit var tvProgressStatus: TextView
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var tvProgressDetails: TextView
    private lateinit var btnOpenFile: Button
    private lateinit var btnHistory: View
    private lateinit var btnAbout: View
    private lateinit var tvFooterText: TextView

    private val formatList = mutableListOf<FormatInfo>()
    private var currentUrl = ""
    private var lastDownloadedFilePath: String? = null

    data class FormatInfo(val formatId: String, val resolution: String, val ext: String) {
        override fun toString(): String = "$resolution ($ext)"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Python if not already started
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        etUrl = findViewById(R.id.etUrl)
        btnAnalyze = findViewById(R.id.btnAnalyze)
        cvVideoInfo = findViewById(R.id.cvVideoInfo)
        ivThumbnail = findViewById(R.id.ivThumbnail)
        tvVideoTitle = findViewById(R.id.tvVideoTitle)
        tvVideoDuration = findViewById(R.id.tvVideoDuration)
        spinnerFormats = findViewById(R.id.spinnerFormats)
        btnDownload = findViewById(R.id.btnDownload)
        progressLayout = findViewById(R.id.progressLayout)
        tvProgressStatus = findViewById(R.id.tvProgressStatus)
        progressBar = findViewById(R.id.progressBar)
        tvProgressDetails = findViewById(R.id.tvProgressDetails)
        btnOpenFile = findViewById(R.id.btnOpenFile)

        btnAnalyze.setOnClickListener {
            val url = etUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                analyzeUrl(url)
            } else {
                Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            }
        }

        btnDownload.setOnClickListener {
            checkPermissionsAndDownload()
        }

        btnOpenFile.setOnClickListener {
            lastDownloadedFilePath?.let { path ->
                openFile(path)
            }
        }

        btnHistory = findViewById(R.id.btnHistory)
        btnHistory.setOnClickListener {
            startActivity(Intent(this, DownloadsActivity::class.java))
        }

        btnAbout = findViewById(R.id.btnAbout)
        btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        tvFooterText = findViewById(R.id.tvFooterText)
        tvFooterText.setOnClickListener {
            val url = "https://sh4lu-z-projects.vercel.app/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun analyzeUrl(url: String) {
        currentUrl = url
        cvVideoInfo.visibility = View.GONE
        progressLayout.visibility = View.VISIBLE
        btnOpenFile.visibility = View.GONE
        tvProgressStatus.text = getString(R.string.analyzing_text)
        progressBar.isIndeterminate = true
        tvProgressDetails.text = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val py = Python.getInstance()
                val module = py.getModule("downloader")
                val ffmpegPath = applicationInfo.nativeLibraryDir + "/libffmpeg.so"
                val resultJson = module.callAttr("get_video_info", url, ffmpegPath).toString()
                
                withContext(Dispatchers.Main) {
                    handleAnalyzeResult(resultJson)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("OmniDown", "Analyze Error", e)
                }
            }
        }
    }

    private fun handleAnalyzeResult(resultJson: String) {
        progressLayout.visibility = View.GONE
        try {
            val json = JSONObject(resultJson)
            if (json.getString("status") == "success") {
                val data = json.getJSONObject("data")
                tvVideoTitle.text = data.getString("title")
                tvVideoDuration.text = "Duration: " + data.getString("duration")

                formatList.clear()
                val formatsArray = data.getJSONArray("formats")
                for (i in 0 until formatsArray.length()) {
                    val formatObj = formatsArray.getJSONObject(i)
                    formatList.add(FormatInfo(
                        formatObj.getString("format_id"),
                        formatObj.getString("resolution"),
                        formatObj.getString("ext")
                    ))
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, formatList)
                spinnerFormats.adapter = adapter
                
                val thumbnailUrl = data.optString("thumbnail", "")
                if (thumbnailUrl.isNotEmpty()) {
                    ivThumbnail.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val inputStream = java.net.URL(thumbnailUrl).openStream()
                            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                            withContext(Dispatchers.Main) {
                                ivThumbnail.setImageBitmap(bitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    ivThumbnail.visibility = View.GONE
                }
                
                cvVideoInfo.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Failed to analyze link", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionsAndDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = android.net.Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivityForResult(intent, 200)
                } catch (e: Exception) {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent, 200)
                }
            } else {
                startDownload()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            } else {
                startDownload()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    startDownload()
                } else {
                    Toast.makeText(this, "Allow all files access to save videos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDownload()
        } else {
            Toast.makeText(this, "Storage permission is required to download", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startDownload() {
        val selectedFormat = spinnerFormats.selectedItem as? FormatInfo ?: return
        
        // Define download path based on format type
        val subFolder = if (selectedFormat.formatId == "bestaudio") "Audio" else "Video"
        val downloadDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OmniDown/$subFolder")
        if (!downloadDir.exists()) downloadDir.mkdirs()
        
        cvVideoInfo.visibility = View.GONE
        progressLayout.visibility = View.VISIBLE
        btnOpenFile.visibility = View.GONE
        progressBar.isIndeterminate = false
        progressBar.progress = 0
        tvProgressStatus.text = "Downloading..."
        tvProgressDetails.text = "Saving to: " + downloadDir.absolutePath
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val py = Python.getInstance()
                val module = py.getModule("downloader")
                val ffmpegPath = applicationInfo.nativeLibraryDir + "/libffmpeg.so"
                
                val callback = object : ProgressCallback {
                    override fun invoke(progressStr: String) {
                        runOnUiThread {
                            val parts = progressStr.split("|")
                            if (parts.size >= 3) {
                                val percentStr = parts[0].replace("%", "").trim()
                                val speed = parts[1]
                                val sizes = parts[2]
                                
                                try {
                                    val percent = percentStr.toFloat().toInt()
                                    progressBar.progress = percent
                                    tvProgressStatus.text = "Downloading: $percent%"
                                    tvProgressDetails.text = "Speed: $speed | $sizes"
                                } catch (e: Exception) {
                                    // Ignore parse errors from yt-dlp output strings
                                }
                            }
                        }
                    }
                }
                
                val resultJson = module.callAttr("download_video", currentUrl, downloadDir.absolutePath, selectedFormat.formatId, callback, ffmpegPath).toString()
                
                withContext(Dispatchers.Main) {
                    try {
                        val json = JSONObject(resultJson)
                        if (json.getString("status") == "success") {
                            val savedPath = json.optString("filename", downloadDir.absolutePath)
                            lastDownloadedFilePath = savedPath
                            progressBar.progress = 100
                            tvProgressStatus.text = "Download Complete!"
                            tvProgressDetails.text = "Saved to:\n$savedPath"
                            btnOpenFile.visibility = View.VISIBLE
                            
                            try {
                                MediaScannerConnection.scanFile(this@MainActivity, arrayOf(savedPath), null, null)
                            } catch (e: Exception) {}
                            
                            Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_LONG).show()
                        } else {
                            tvProgressStatus.text = "Download Failed"
                            tvProgressDetails.text = json.getString("message")
                            Toast.makeText(this@MainActivity, "Error: ${json.getString("message")}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        progressBar.progress = 100
                        tvProgressStatus.text = "Download Complete!"
                        tvProgressDetails.text = "Saved to: " + downloadDir.absolutePath
                        Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvProgressStatus.text = "Download Failed"
                    tvProgressDetails.text = e.message
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openFile(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_VIEW)
                val mimeType = if (filePath.endsWith(".m4a") || filePath.endsWith(".mp3")) "audio/*" else "video/*"
                intent.setDataAndType(uri, mimeType)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } else {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
