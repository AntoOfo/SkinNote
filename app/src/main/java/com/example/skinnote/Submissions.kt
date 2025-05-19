package com.example.skinnote

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class Submissions : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubmissionAdapter
    private lateinit var dao: SkinNoteDao
    private var imageDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submissions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backBtn = findViewById<ImageView>(R.id.backButton)

        backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        recyclerView = findViewById(R.id.submissionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // grab dao from db
        dao = SkinNoteDatabase.getDatabase(applicationContext).skinNoteDao()

        // get data from db nd shows it
        lifecycleScope.launch {
            val entries = dao.getAllEntries()

            adapter = SubmissionAdapter(
                entries.toMutableList(),
                onImageClick = { clickedImageUri ->
                    showImageDialog(clickedImageUri)
                },
                onEditClick = { entryToEdit ->
                    showEditDialog(entryToEdit)  // gonna make edit dialog
                },
                onDeleteClick = { entryToDelete ->
                    lifecycleScope.launch {
                        dao.deleteEntry(entryToDelete)
                        loadEntries()
                    }
                }

            )

            recyclerView.adapter = adapter
        }


    }

    private fun showImageDialog(uri: Uri) {
        if (imageDialog == null) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.image_preview_dialog, null)

            val imageView = dialogView.findViewById<ImageView>(R.id.dialogImage)
            val closeBtn = dialogView.findViewById<Button>(R.id.closeBtn)

            imageView.setImageURI(uri)

            closeBtn.setOnClickListener {
                imageDialog?.dismiss()
                imageDialog = null
            }

            builder.setView(dialogView)
            imageDialog = builder.create()
            imageDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            imageDialog?.show()
        }

    }

    private fun showEditDialog(entry: SkinEntry) {

        val dialogView = layoutInflater.inflate(R.layout.edit_dialog, null) // You can design this XML
        val skinBar = dialogView.findViewById<SeekBar>(R.id.editSeekBar)
        val saveBtn = dialogView.findViewById<Button>(R.id.saveEditBtn)
        val faceSpinner = dialogView.findViewById<Spinner>(R.id.faceEditSpinner)
        val cleanserSpinner = dialogView.findViewById<Spinner>(R.id.cleanserEditSpinner)
        val serumSpinner = dialogView.findViewById<Spinner>(R.id.serumEditSpinner)
        val moisturiserSpinner = dialogView.findViewById<Spinner>(R.id.moisturiserEditSpinner)


        skinBar.progress = entry.skinFeel ?: 0

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        lifecycleScope.launch {
            // loads products from db
            val faceWashProducts = dao.getProductsByType("faceWash").map { it.name }
            val cleanserProducts = dao.getProductsByType("cleanser").map { it.name }
            val serumProducts = dao.getProductsByType("serum").map { it.name }
            val moisturiserProducts = dao.getProductsByType("moisturiser").map { it.name }

            // helper function to setup spinner with list & select current value
            fun setupSpinner(spinner: Spinner, items: List<String>, selectedValue: String) {
                // select option at the start to allow for a "no selection"
                val options = listOf("Select") + items
                val adapter = android.widget.ArrayAdapter(
                    this@Submissions,
                    android.R.layout.simple_spinner_item,
                    options
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                val index = options.indexOf(selectedValue).takeIf { it >= 0 } ?: 0
                spinner.setSelection(index)
            }

            setupSpinner(faceSpinner, faceWashProducts, entry.faceWash)
            setupSpinner(cleanserSpinner, cleanserProducts, entry.cleanser)
            setupSpinner(serumSpinner, serumProducts, entry.serum)
            setupSpinner(moisturiserSpinner, moisturiserProducts, entry.moisturiser)

            dialog.show()
        }

        saveBtn.setOnClickListener {
            lifecycleScope.launch {
                // updates entry with spinner choices
                entry.faceWash = faceSpinner.selectedItem.toString().takeIf { it != "Select" } ?: ""
                entry.cleanser = cleanserSpinner.selectedItem.toString().takeIf { it != "Select" } ?: ""
                entry.serum = serumSpinner.selectedItem.toString().takeIf { it != "Select" } ?: ""
                entry.moisturiser = moisturiserSpinner.selectedItem.toString().takeIf { it != "Select" } ?: ""
                entry.skinFeel = skinBar.progress

                dao.updateEntry(entry)
                loadEntries()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun loadEntries() {
        lifecycleScope.launch {
            val updatedEntries = dao.getAllEntries()
            adapter.updateData(updatedEntries)
        }
    }
}