package com.example.skinnote

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextClock
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var addProductDialog: AlertDialog? = null


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

        addBtn.setOnClickListener {
            showAddProductDialog()
        }

    }

    private fun showAddProductDialog() {

        if (addProductDialog == null) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_dialog, null)

            val doneBtn = dialogView.findViewById<Button>(R.id.doneBtn)
            val faceEditText = dialogView.findViewById<EditText>(R.id.faceEditText)
            val cleanserEditText = dialogView.findViewById<EditText>(R.id.cleanserEditText)
            val serumEditText = dialogView.findViewById<EditText>(R.id.serumEditText)
            val moisEditText = dialogView.findViewById<EditText>(R.id.moisEditText)

            builder.setView(dialogView)
            addProductDialog = builder.create()
            addProductDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

            doneBtn.setOnClickListener {
                // this will save all inputs and add to respective spinners

                addProductDialog?.dismiss()
            }
        }

        addProductDialog?.show()

    }
}