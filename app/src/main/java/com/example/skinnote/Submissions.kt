package com.example.skinnote

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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

        recyclerView = findViewById(R.id.submissionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // grab dao from db
        dao = SkinNoteDatabase.getDatabase(applicationContext).skinNoteDao()

        // get data from db nd shows it
        lifecycleScope.launch {
            val entries = dao.getAllEntries()
            adapter = SubmissionAdapter(entries) { clickedImageUri ->
                showImageDialog(clickedImageUri)
            }
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
}