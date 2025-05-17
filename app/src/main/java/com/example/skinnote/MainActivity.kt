package com.example.skinnote

import android.content.Intent
import android.icu.util.Calendar
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var db: SkinNoteDatabase
    private lateinit var dao: SkinNoteDao

    private var addProductDialog: AlertDialog? = null

    // spinner declarations
    private lateinit var faceSpinner: Spinner
    private lateinit var cleanserSpinner: Spinner
    private lateinit var serumSpinner: Spinner
    private lateinit var moisSpinner: Spinner

    // lists for spinner data
    private val faceProductList = mutableListOf("Select")
    private val cleanserProductList = mutableListOf("Select")
    private val serumProductList = mutableListOf("Select")
    private val moisProductList = mutableListOf("Select")

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

        db = SkinNoteDatabase.getDatabase(this)
        dao = db.skinNoteDao()

        loadProductsIntoSpinners()

        // mutable texts
        val timeText = findViewById<TextClock>(R.id.timeText)
        val dateText = findViewById<TextClock>(R.id.dateText)
        val skinfeelText = findViewById<TextView>(R.id.skinfeelText)

        // spinners
        faceSpinner = findViewById(R.id.faceSpinner)
        cleanserSpinner = findViewById(R.id.cleanserSpinner)
        serumSpinner = findViewById(R.id.serumSpinner)
        moisSpinner = findViewById(R.id.moisSpinner)

        // buttons
        val addBtn = findViewById<ImageView>(R.id.addBtn)
        val menuBtn = findViewById<ImageView>(R.id.menuBtn)
        val submitBtn = findViewById<ImageView>(R.id.submitBtn)

        // seekbar
        val skinBar = findViewById<SeekBar>(R.id.skinBar)

        // emoji
        val emoji = findViewById<TextView>(R.id.emojiTxt)
        emoji.visibility = View.INVISIBLE  // starts off invisible


        // time/date formatting
        timeText.format12Hour = null
        timeText.format24Hour = "HH:mm"

        dateText.format12Hour = null
        dateText.format24Hour = "dd MMMM yyyy"

        // connecting adapters to lists then linking em to spinners
        faceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, faceProductList)
        faceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        faceSpinner.adapter = faceAdapter

        cleanserAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cleanserProductList)
        cleanserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cleanserSpinner.adapter = cleanserAdapter

        serumAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, serumProductList)
        serumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        serumSpinner.adapter = serumAdapter

        moisAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, moisProductList)
        moisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        moisSpinner.adapter = moisAdapter

        addBtn.setOnClickListener {
            showAddProductDialog()
        }

        menuBtn.setOnClickListener {
            val intent = Intent(this, Submissions::class.java)
            startActivity(intent)
        }

        var emojiShown = false
        // seekbar code
        skinBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (progress) {
                    0 -> skinfeelText.text = "Poor"
                    1 -> skinfeelText.text = "Okay"
                    2 -> skinfeelText.text = "Good"
                    3 -> skinfeelText.text = "Perfect"
                }

                // emoji fade in when seekbar is moved
                seekBar?.let {
                    if (!emojiShown) {
                        emoji.alpha = 0f
                        emoji.visibility = View.VISIBLE
                        emoji.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start()
                        emojiShown = true
                    }
                }

                seekBar?.let {
                    emoji.visibility = View.VISIBLE   // make emoji visible

                     when (progress) {
                        0 -> emoji.text = "😡"
                        1 -> emoji.text = "😐"
                        2 -> emoji.text = "🙂"
                        3 -> emoji.text = "😇"
                    }

                    // calculate thumbs X position
                    val thumb = it.thumb
                    val offsetX = it.x + it.paddingLeft + ((it.width - it.paddingLeft - it.paddingRight) * progress / it.max.toFloat()) - (emoji.width / 2)
                    val offsetY = it.y - emoji.height - 10
                    emoji.x = offsetX
                    emoji.y = offsetY

                    // emoji animation on movement
                    emoji.scaleX = 0.8f
                    emoji.scaleY = 0.8f
                    emoji.animate()
                        .x(offsetX)
                        .y(offsetY)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()

                }

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        submitBtn.setOnClickListener {
            // grabbing spinner/seekbar choices
            val faceWash = faceSpinner.selectedItem.toString()
            val cleanser = cleanserSpinner.selectedItem.toString()
            val serum = serumSpinner.selectedItem.toString()
            val moisturiser = moisSpinner.selectedItem.toString()
            val skinFeel = skinBar.progress

            // making entry object (check entity class)
            val entry = SkinEntry(
                faceWash = faceWash,
                cleanser = cleanser,
                serum = serum,
                moisturiser = moisturiser,
                skinFeel = skinFeel
            )

            // puts this into room db
            lifecycleScope.launch {
                dao.insertEntry(entry)

                Toast.makeText(this@MainActivity, "Entry saved!", Toast.LENGTH_SHORT).show()
            }
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
                val faceProduct = faceEditText.text.toString().trim()
                val cleanserProduct = cleanserEditText.text.toString().trim()
                val serumProduct = serumEditText.text.toString().trim()
                val moisProduct = moisEditText.text.toString().trim()

                lifecycleScope.launch {
                    if (faceProduct.isNotEmpty()) {
                        dao.insertProduct(ProductsEntry(name = faceProduct, type = "faceWash"))
                    }
                    if (cleanserProduct.isNotEmpty()) {
                        dao.insertProduct(ProductsEntry(name = cleanserProduct, type = "cleanser"))
                    }
                    if (serumProduct.isNotEmpty()) {
                        dao.insertProduct(ProductsEntry(name = serumProduct, type = "serum"))
                    }
                    if (moisProduct.isNotEmpty()) {
                        dao.insertProduct(ProductsEntry(name = moisProduct, type = "moisturiser"))
                    }

                    loadProductsIntoSpinners()

                    withContext(Dispatchers.Main) {
                        faceEditText.text.clear()
                        cleanserEditText.text.clear()
                        serumEditText.text.clear()
                        moisEditText.text.clear()

                        addProductDialog?.dismiss()
                        Toast.makeText(this@MainActivity, "Products added!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        addProductDialog?.show()

    }

    // loads products into spinners when app is launched
    private fun loadProductsIntoSpinners() {
        lifecycleScope.launch {
            val faceWashListDb = dao.getProductsByType("faceWash").map {it.name}
            val cleanserListDb = dao.getProductsByType("cleanser").map {it.name}
            val serumListDb = dao.getProductsByType("serum").map {it.name}
            val moisturiserListDb = dao.getProductsByType("moisturiser").map {it.name}

            // switches to main thread to update ui
            withContext(Dispatchers.Main) {

                //clears existing lists and adds new lists from db
                faceProductList.clear()
                faceProductList.add("Select")
                faceProductList.addAll(faceWashListDb)

                cleanserProductList.clear()
                cleanserProductList.add("Select")
                cleanserProductList.addAll(cleanserListDb)

                serumProductList.clear()
                serumProductList.add("Select")
                serumProductList.addAll(serumListDb)

                moisProductList.clear()
                moisProductList.add("Select")
                moisProductList.addAll(moisturiserListDb)

                faceAdapter.notifyDataSetChanged()
                cleanserAdapter.notifyDataSetChanged()
                serumAdapter.notifyDataSetChanged()
                moisAdapter.notifyDataSetChanged()

            }
        }
    }
}