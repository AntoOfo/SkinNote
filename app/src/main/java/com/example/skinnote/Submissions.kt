package com.example.skinnote

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
            adapter = SubmissionAdapter(entries)
            recyclerView.adapter = adapter
        }


    }
}