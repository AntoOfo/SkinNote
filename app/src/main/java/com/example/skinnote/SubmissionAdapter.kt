package com.example.skinnote

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// connects data to UI

// adapter for recyclerview to display each submission
class SubmissionAdapter(private val submissions: List<SkinEntry>) :
        RecyclerView.Adapter<SubmissionAdapter.SubmissionViewHolder>() {

            // viewholder references stuff in the cardview xml
            class SubmissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                val faceWashText: TextView = itemView.findViewById(R.id.faceCard)
                val cleanserText: TextView = itemView.findViewById(R.id.cleanserCard)
                val serumText: TextView = itemView.findViewById(R.id.serumCard)
                val moisturiserText: TextView = itemView.findViewById(R.id.moisCard)
                val seekBarValueText: TextView = itemView.findViewById(R.id.skinfeelCard)
                val timestampText: TextView = itemView.findViewById(R.id.dateCard)
                val selfieImage: ImageView = itemView.findViewById(R.id.selfieImage)
            }

    // inflates layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_skin_entry, parent, false)
        return SubmissionViewHolder(view)
    }

    // return number of items
    override fun getItemCount(): Int = submissions.size

    // bind data to views
    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        val submission = submissions[position]

        // face wash
        if (submission.faceWash == "Select" || submission.faceWash.isNullOrEmpty()) {
            holder.faceWashText.visibility = View.GONE
        } else {
            holder.faceWashText.visibility = View.VISIBLE
            holder.faceWashText.text = "Face Wash: ${submission.faceWash}"
        }

        // cleanser
        if (submission.cleanser == "Select" || submission.cleanser.isNullOrEmpty()) {
            holder.cleanserText.visibility = View.GONE
        } else {
            holder.cleanserText.visibility = View.VISIBLE
            holder.cleanserText.text = "Cleanser: ${submission.cleanser}"
        }

        // serum
        if (submission.serum == "Select" || submission.serum.isNullOrEmpty()) {
            holder.serumText.visibility = View.GONE
        } else {
            holder.serumText.visibility = View.VISIBLE
            holder.serumText.text = "Serum: ${submission.serum}"
        }

        // moisturiser
        if (submission.moisturiser == "Select" || submission.moisturiser.isNullOrEmpty()) {
            holder.moisturiserText.visibility = View.GONE
        } else {
            holder.moisturiserText.visibility = View.VISIBLE
            holder.moisturiserText.text = "Moisturiser: ${submission.moisturiser}"
        }

        val formattedTime = android.text.format.DateFormat.format("dd MM yyyy, HH:mm", submission.timestamp)
        holder.timestampText.text = "Date: $formattedTime"

        if (submission.skinFeel != null) {
            val skinFeelEmoji = when (submission.skinFeel) {
                0 -> "😡"
                1 -> "😐"
                2 -> "🙂"
                3 -> "😇"
                else -> "❓" // fallback for unexpected values
            }

            holder.seekBarValueText.visibility = View.VISIBLE
            holder.seekBarValueText.text = "Skin Feel: $skinFeelEmoji"
        } else {
            holder.seekBarValueText.visibility = View.GONE

        }

        if (submission.selfieUri != null) {
            holder.selfieImage.visibility = View.VISIBLE
            holder.selfieImage.setImageURI(Uri.parse(submission.selfieUri))
        } else {
            holder.selfieImage.visibility = View.GONE
        }
    }

}