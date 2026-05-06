package com.example.omnidown

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class DownloadsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)

        val listView = findViewById<ListView>(R.id.listViewDownloads)
        val tvNoDownloads = findViewById<TextView>(R.id.tvNoDownloads)
        
        val btnBack = findViewById<View>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val downloadDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OmniDown")
        
        if (!downloadDir.exists()) {
            tvNoDownloads.visibility = View.VISIBLE
            listView.visibility = View.GONE
            return
        }

        val files = downloadDir.walkTopDown().filter { it.isFile }.sortedByDescending { it.lastModified() }.toList()

        if (files.isEmpty()) {
            tvNoDownloads.visibility = View.VISIBLE
            listView.visibility = View.GONE
        } else {
            val fileNames = files.map { 
                val parent = it.parentFile?.name ?: ""
                if (parent == "Audio" || parent == "Video") "[${parent}] ${it.name}" else it.name
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames)
            listView.adapter = adapter

            listView.setOnItemClickListener { _, _, position, _ ->
                val file = files[position]
                openFile(file.absolutePath)
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
