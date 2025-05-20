package com.example.skinnote

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var db: SkinNoteDatabase
    private lateinit var dao: SkinNoteDao

    private var addProductDialog: AlertDialog? = null
    private var helpDialog: AlertDialog? = null
    private var deleteDialog: AlertDialog? = null

    // for image
    private var selectedImageUri: Uri? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var selfieUri: Uri? = null

    private val CAMERA_PERMISSION_CODE = 1001

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

    private var hasSkinBarMoved = false


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

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
        val cameraBtn = findViewById<ImageView>(R.id.cameraBtn)
        val helpBtn = findViewById<ImageView>(R.id.helpBtn)

        // seekbar
        val skinBar = findViewById<SeekBar>(R.id.skinBar)

        // emoji
        val emoji = findViewById<TextView>(R.id.emojiTxt)
        emoji.visibility = View.INVISIBLE  // starts off invisible

        val dateFont = ResourcesCompat.getFont(this, R.font.poppins)

        // time/date formatting
        timeText.format12Hour = null
        timeText.format24Hour = "HH:mm"
        timeText.typeface = dateFont

        dateText.format12Hour = null
        dateText.format24Hour = "dd MMMM yyyy"
        dateText.typeface = dateFont

        // connecting adapters to lists then linking em to spinners
        faceAdapter = ArrayAdapter(this, R.layout.spinner_item, faceProductList)
        faceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        faceSpinner.adapter = faceAdapter

        cleanserAdapter =
            ArrayAdapter(this, R.layout.spinner_item, cleanserProductList)
        cleanserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cleanserSpinner.adapter = cleanserAdapter

        serumAdapter = ArrayAdapter(this, R.layout.spinner_item, serumProductList)
        serumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        serumSpinner.adapter = serumAdapter

        moisAdapter = ArrayAdapter(this, R.layout.spinner_item, moisProductList)
        moisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        moisSpinner.adapter = moisAdapter

        // handles camera result (if photo was taken...)
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(
                        this@MainActivity,
                        "Photo taken successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    //
                }
            }

        helpBtn.setOnClickListener {
            helpBtn.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    // intention here
                    showHelpDialog()

                    helpBtn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        addBtn.setOnClickListener {
            addBtn.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    showAddProductDialog()

                    addBtn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        menuBtn.setOnClickListener {
            val intent = Intent(this, Submissions::class.java)

            menuBtn.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                    menuBtn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        cameraBtn.setOnClickListener {
            cameraBtn.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    checkCameraPermissionAndLaunch()

                    cameraBtn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        // tracks if emoji is shown yet, for animations
        var emojiShown = false
        // seekbar code
        skinBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    hasSkinBarMoved = true
                }

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
                    val offsetX =
                        it.x + it.paddingLeft + ((it.width - it.paddingLeft - it.paddingRight) * progress / it.max.toFloat()) - (emoji.width / 2)
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
            submitBtn.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    submitBtn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(100)
                        .start()
                    // grabbing spinner/seekbar choices
                    val faceWash = faceSpinner.selectedItem.toString()
                    val cleanser = cleanserSpinner.selectedItem.toString()
                    val serum = serumSpinner.selectedItem.toString()
                    val moisturiser = moisSpinner.selectedItem.toString()
                    val skinFeel = skinBar.progress

                    val isFaceSelected = faceWash != "Select"
                    val isCleanserSelected = cleanser != "Select"
                    val isSerumSelected = serum != "Select"
                    val isMoisSelected = moisturiser != "Select"
                    val isSelfieTaken = selfieUri != null

                    if (
                        !isFaceSelected &&
                        !isCleanserSelected &&
                        !isSerumSelected &&
                        !isMoisSelected &&
                        !isSelfieTaken &&
                        !hasSkinBarMoved
                    ) {
                        Toast.makeText(
                            this@MainActivity,
                            "Please fill out at least one field!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@withEndAction
                    }

                    val skinFeelValue: Int? = if (hasSkinBarMoved) skinBar.progress else null

                    // making entry object (check entity class)
                    val entry = SkinEntry(
                        faceWash = faceWash,
                        cleanser = cleanser,
                        serum = serum,
                        moisturiser = moisturiser,
                        skinFeel = skinFeelValue,
                        selfieUri = selfieUri?.toString()
                    )

                    // puts this into room db
                    lifecycleScope.launch {
                        dao.insertEntry(entry)

                        Toast.makeText(this@MainActivity, "Entry saved!", Toast.LENGTH_SHORT).show()
                        resetForm()
                    }
                }
                .start()
        }
    }

    private fun showHelpDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.help_dialog, null)

        val deleteDialogBtn = dialogView.findViewById<Button>(R.id.deleteDialogBtn)
        val closeBtn = dialogView.findViewById<Button>(R.id.closeIntroDialogBtn)

        lifecycleScope.launch {
        val productList = dao.getAllProducts()

        deleteDialogBtn.setOnClickListener {
            helpDialog?.dismiss()

            if (productList.isEmpty()) {
                Toast.makeText(
                    this@MainActivity, "No products to delete",
                    Toast.LENGTH_SHORT
                ).show()
                deleteDialog?.dismiss()
                return@setOnClickListener
            } else {
                showDeleteProductDialog()
            }
        }
        }

        closeBtn.setOnClickListener {
            helpDialog?.dismiss()
        }

        builder.setView(dialogView)
        helpDialog = builder.create()
        helpDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        helpDialog?.show()


    }

    private fun showDeleteProductDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.delete_dialog, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.deleteProductSpinner)
        val deleteBtn = dialogView.findViewById<Button>(R.id.deleteProductBtn)

        builder.setView(dialogView)
        deleteDialog = builder.create()
        deleteDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        lifecycleScope.launch {
            val db = SkinNoteDatabase.getDatabase(this@MainActivity)
            val dao = db.skinNoteDao()
            val productList = dao.getAllProducts()

            val adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_item,
                productList.map { it.name }
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            spinner.adapter = adapter

            deleteBtn.setOnClickListener {
                val selectedProductName = spinner.selectedItem as String
                val selectedProduct = productList.firstOrNull { it.name == selectedProductName }

                if (selectedProduct != null) {
                    lifecycleScope.launch {
                        dao.deleteProduct(selectedProduct)

                        // fetchin all products for updated variable
                        val updatedProductList = dao.getAllProducts()

                        faceProductList.clear()
                        cleanserProductList.clear()
                        serumProductList.clear()
                        moisProductList.clear()

                        faceProductList.add("Select")
                        cleanserProductList.add("Select")
                        serumProductList.add("Select")
                        moisProductList.add("Select")

                        // repopulating with updated db data
                        for (product in updatedProductList) {
                            when (product.type) {
                                "Face Wash" -> faceProductList.add(product.name)
                                "Cleanser" -> cleanserProductList.add(product.name)
                                "Serum" -> serumProductList.add(product.name)
                                "Moisturiser" -> moisProductList.add(product.name)
                            }
                        }

                        // Notify all adapters
                        faceAdapter.notifyDataSetChanged()
                        cleanserAdapter.notifyDataSetChanged()
                        serumAdapter.notifyDataSetChanged()
                        moisAdapter.notifyDataSetChanged()

                        faceSpinner.setSelection(0)
                        cleanserSpinner.setSelection(0)
                        serumSpinner.setSelection(0)
                        moisSpinner.setSelection(0)

                        Toast.makeText(
                            this@MainActivity,
                            "Deleted: ${selectedProduct.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        deleteDialog?.dismiss()
                    }
                }
            }
        }
        deleteDialog?.show()
    }

    // builds dialog  & adds new products to db
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

            doneBtn.setOnClickListener {

                // turning each input to a string
                val faceProduct = faceEditText.text.toString().trim()
                val cleanserProduct = cleanserEditText.text.toString().trim()
                val serumProduct = serumEditText.text.toString().trim()
                val moisProduct = moisEditText.text.toString().trim()

                if (faceProduct.isEmpty() && cleanserProduct.isEmpty() && serumProduct.isEmpty()
                    && moisProduct.isEmpty()
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter at least one product",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // inserts new products in db with type
                lifecycleScope.launch {
                    if (faceProduct.isNotEmpty()) {
                        dao.insertProduct(
                            ProductsEntry(
                                name = faceProduct,
                                type = "faceWash"
                            )
                        )
                    }
                    if (cleanserProduct.isNotEmpty()) {
                        dao.insertProduct(
                            ProductsEntry(
                                name = cleanserProduct,
                                type = "cleanser"
                            )
                        )
                    }
                    if (serumProduct.isNotEmpty()) {
                        dao.insertProduct(
                            ProductsEntry(
                                name = serumProduct,
                                type = "serum"
                            )
                        )
                    }
                    if (moisProduct.isNotEmpty()) {
                        dao.insertProduct(
                            ProductsEntry(
                                name = moisProduct,
                                type = "moisturiser"
                            )
                        )
                    }

                    loadProductsIntoSpinners()

                    // clears edittexts when dialog is closed
                    withContext(Dispatchers.Main) {
                        faceEditText.text.clear()
                        cleanserEditText.text.clear()
                        serumEditText.text.clear()
                        moisEditText.text.clear()

                        addProductDialog?.dismiss()
                        Toast.makeText(
                            this@MainActivity,
                            "Products added!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
        addProductDialog?.show()
    }



    // loads products into spinners when app is launched
    private fun loadProductsIntoSpinners() {
        lifecycleScope.launch {
            val faceWashListDb = dao.getProductsByType("faceWash").map { it.name }
            val cleanserListDb = dao.getProductsByType("cleanser").map { it.name }
            val serumListDb = dao.getProductsByType("serum").map { it.name }
            val moisturiserListDb = dao.getProductsByType("moisturiser").map { it.name }

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

    // creates image file in pictures directory
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    // checks perms
    private fun checkCameraPermissionAndLaunch() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            createImageFile()
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        selfieUri = photoUri
        cameraLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to take selfies",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun resetForm() {
        faceSpinner.setSelection(0)
        cleanserSpinner.setSelection(0)
        serumSpinner.setSelection(0)
        moisSpinner.setSelection(0)

        val skinBar = findViewById<SeekBar>(R.id.skinBar)
        skinBar.progress = 0
        hasSkinBarMoved = false

        val skinfeelText = findViewById<TextView>(R.id.skinfeelText)
        skinfeelText.text = "How does your skin feel today?"

        val emoji = findViewById<TextView>(R.id.emojiTxt)
        emoji.visibility = View.INVISIBLE

        selfieUri = null
    }
}