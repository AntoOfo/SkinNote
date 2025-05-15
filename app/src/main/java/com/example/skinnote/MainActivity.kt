package com.example.skinnote

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
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

    // lists for spinner data
    private val faceProducts = mutableListOf("Select")
    private val cleanserProducts = mutableListOf("Select")
    private val serumProducts = mutableListOf("Select")
    private val moisProducts = mutableListOf("Select")

    // adapters to bridge lists to respective
    private lateinit var faceAdapter: ArrayAdapter<String>
    private lateinit var cleanserAdapter: ArrayAdapter<String>
    private lateinit var serumAdapter: ArrayAdapter<String>
    private lateinit var moisAdapter: ArrayAdapter<String>


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

        // connecting adapters to lists then linking em to spinners
        faceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, faceProducts)
        faceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        faceSpinner.adapter = faceAdapter

        cleanserAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cleanserProducts)
        cleanserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cleanserSpinner.adapter = cleanserAdapter

        serumAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, serumProducts)
        serumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        serumSpinner.adapter = serumAdapter

        moisAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moisProducts)
        moisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        moisSpinner.adapter = moisAdapter

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
                // turning each input to a string
                val faceProduct = faceEditText.text.toString()
                val cleanserProduct = cleanserEditText.text.toString()
                val serumProduct = serumEditText.text.toString()
                val moisProduct = moisEditText.text.toString()

                addProductDialog?.dismiss()
            }
        }

        addProductDialog?.show()

    }
}