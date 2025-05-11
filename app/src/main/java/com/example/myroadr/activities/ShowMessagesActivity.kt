package com.example.myroadr.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myroadr.Adpaters.SupportMessageAdapter
import com.example.myroadr.R
import com.example.myroadr.models.SupportMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowMessagesActivity : AppCompatActivity() {

    private lateinit var adapter: SupportMessageAdapter
    private val messages = mutableListOf<SupportMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_messages)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSupport)
        val emptyView = findViewById<LinearLayout>(R.id.emptyView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SupportMessageAdapter(messages) { message ->
            Toast.makeText(this, "Reply to: ${message.userEmail}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        loadMessages(recyclerView, emptyView)
    }

    private fun loadMessages(recyclerView: RecyclerView, emptyView: View) {
        val dbRef = FirebaseDatabase.getInstance().getReference("support_messages")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (msgSnapshot in snapshot.children) {
                    val msg = msgSnapshot.getValue(SupportMessage::class.java)
                    msg?.let { messages.add(it) }
                }
                adapter.notifyDataSetChanged()

                if (messages.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShowMessagesActivity, "Error loading messages", Toast.LENGTH_SHORT).show()
            }
        })
    }


}