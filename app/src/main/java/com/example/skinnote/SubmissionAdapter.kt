package com.example.skinnote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        holder.faceWashText.text = "Face Wash: ${submission.faceWash}"
        holder.cleanserText.text = "Cleanser: ${submission.cleanser}"
        holder.serumText.text = "Serum: ${submission.serum}"
        holder.moisturiserText.text = "Moisturizer: ${submission.moisturiser}"
        holder.seekBarValueText.text = "Skin Feel: ${submission.skinFeel}"

        val formattedTime = android.text.format.DateFormat.format("dd MM yyyy, HH:mm", submission.timestamp)
        holder.timestampText.text = "Date: $formattedTime"
    }


}