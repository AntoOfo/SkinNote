package com.example.skinnote

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextClock
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // mutable texts
        val timeText = findViewById<TextClock>(R.id.timeText)
        val dateText = findViewById<TextView>(R.id.dateText)
        val skinfeelText = findViewById<TextView>(R.id.skinfeelText)

        // spinners
        val faceSpinner = findViewById<Spinner>(R.id.faceSpinner)
        val cleanserSpinner = findViewById<Spinner>(R.id.cleanserSpinner)
        val serumSpinner = findViewById<Spinner>(R.id.serumSpinner)
        val moisSpinner = findViewById<Spinner>(R.id.moisSpinner)

        // buttons
        val addBtn = findViewById<ImageView>(R.id.addBtn)
        val menuBtn = findViewById<ImageView>(R.id.menuBtn)

        val skinBar = findViewById<SeekBar>(R.id.skinBar)

        timeText.format12Hour = null
        timeText.format24Hour = "HH:mm"

    }
}