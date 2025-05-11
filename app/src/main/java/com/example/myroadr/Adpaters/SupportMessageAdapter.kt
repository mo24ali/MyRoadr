package com.example.myroadr.Adpaters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myroadr.R
import com.example.myroadr.models.SupportMessage

class SupportMessageAdapter(
    private val messages: List<SupportMessage>,
    private val onReplyClick: (SupportMessage) -> Unit
) : RecyclerView.Adapter<SupportMessageAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val emailText: TextView = itemView.findViewById(R.id.emailText)
        val replyButton: Button = itemView.findViewById(R.id.replyButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_support_message, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.description
        holder.dateText.text = "${message.date} ${message.time}"
        holder.emailText.text = message.userEmail
        holder.replyButton.setOnClickListener {
            onReplyClick(message)
        }
    }
}
