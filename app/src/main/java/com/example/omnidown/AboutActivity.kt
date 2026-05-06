package com.example.omnidown

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        
        val btnBack = findViewById<android.view.View>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val btnVisitWebsite = findViewById<android.widget.Button>(R.id.btnVisitWebsite)
        btnVisitWebsite.setOnClickListener {
            val url = "https://sh4lu-z-projects.vercel.app/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}
