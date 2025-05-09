package com.example.myroadr.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myroadr.R
import com.example.myroadr.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        //auth = FirebaseAuth.getInstance()
       // loadUserName(view)
        return view
    }

    private fun loadUserName(view: View) {
        val user = auth.currentUser
        val nameTextView = view.findViewById<TextView>(R.id.profileSmya)

        if (user != null) {
            val userId = user.uid
            val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            dbRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("username").getValue(String::class.java)
                nameTextView.text = (username ?: "Nom inconnu").toString()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Erreur de chargement du nom", Toast.LENGTH_SHORT).show()
            }
        } else {
            nameTextView.text = "Utilisateur non connecté"
        }
    }
}
