package com.example.myroadr.Adpaters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myroadr.R
import com.example.myroadr.models.SupportMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

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
            val context = holder.itemView.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reply, null)

            val subjectInput = dialogView.findViewById<EditText>(R.id.editSubject)
            val bodyInput = dialogView.findViewById<EditText>(R.id.editBody)

            val dialog = AlertDialog.Builder(context)
                .setTitle("Reply to ${message.userEmail}")
                .setView(dialogView)
                .setPositiveButton("Send") { _, _ ->
                    val subject = subjectInput.text.toString()
                    val body = bodyInput.text.toString()

                    sendEmailViaGoogleScript(context, message.userEmail, subject, body)
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
        }
    }
    fun sendEmailViaGoogleScript(
        context: Context,
        toEmail: String,
        subject: String,
        message: String
    ) {
        val json = JSONObject().apply {
            put("to", toEmail)
            put("subject", subject)
            put("message", message)
        }

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://script.google.com/macros/s/AKfycbzsPsyYvpfUdC83kCfs-a3LZPbtkEqHpkO870D1Eo1MIz60lECQnF9hLaXQtL6X4J4k4w/exec") // <--- remplace ici
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "Échec : ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                (context as Activity).runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Email envoyé avec succès", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Erreur : ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }




}
