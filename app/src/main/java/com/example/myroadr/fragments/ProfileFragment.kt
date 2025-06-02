package com.example.myroadr.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myroadr.DB.AppDatabase
import com.example.myroadr.Entity.UserProfile
import com.example.myroadr.R
import com.example.myroadr.activities.ContactSupportActivity
import com.example.myroadr.activities.MyEventsActivity
import com.example.myroadr.activities.SplashScreenActivity
import com.example.myroadr.databinding.FragmentProfileBinding
import com.example.myroadr.util.SharedPreferencesUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentProfileBinding
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        loadUserInfos()
        loadProfileFromRoom()

        binding.LogOut.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui") { _, _ ->
                    // Afficher le dialog de chargement
                    val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
                    dialogView.findViewById<TextView>(R.id.loadingText).text = "Déconnexion en cours"
                    val loadingDialog = AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .setCancelable(false)
                        .create()
                    loadingDialog.show()

                    // Récupérer l'UID de l'utilisateur connecté
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    if (userId != null) {
                        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                        userRef.child("enLigne").setValue(false).addOnCompleteListener {
                            // Continuer la déconnexion après la mise à jour
                            FirebaseAuth.getInstance().signOut()
                            SharedPreferencesUtil.setUserLoggedIn(requireContext(), false)
                            loadingDialog.dismiss()

                            startActivity(Intent(activity, SplashScreenActivity::class.java))
                            activity?.finish()
                        }
                    } else {
                        // Utilisateur null ? Déconnecte sans update
                        FirebaseAuth.getInstance().signOut()
                        SharedPreferencesUtil.setUserLoggedIn(requireContext(), false)
                        loadingDialog.dismiss()

                        startActivity(Intent(activity, SplashScreenActivity::class.java))
                        activity?.finish()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }



        binding.floatingAddPicActionButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        binding.support.setOnClickListener{
            val intent = Intent(activity, ContactSupportActivity::class.java)
            startActivity(intent)

        }
        binding.buttonMyEvents.setOnClickListener{
            val intent = Intent(requireContext(), MyEventsActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun loadUserInfos() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            dbRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("username").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)

                binding.profileSmya.text = username ?: "Nom inconnu"
                binding.tvEmail.text = email ?: "Email inconnu"
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Erreur de chargement", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.profileSmya.text = "Non connecté"
            binding.tvEmail.text = "-"
        }
    }

    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun bytesToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun saveProfileToRoom(name: String?, email: String?, imageBitmap: Bitmap?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profile = UserProfile(
            id = uid,
            name = name ?: "Nom inconnu",
            email = email ?: "Email inconnu",
            image = imageBitmap?.let { bitmapToBytes(it) }
        )
        lifecycleScope.launch {
            AppDatabase.getDatabase(requireContext()).userProfileDao().insert(profile)
        }
    }

    private fun loadProfileFromRoom() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        lifecycleScope.launch {
            val profile = AppDatabase.getDatabase(requireContext()).userProfileDao().getProfile(uid)
            profile?.let {
                binding.profileSmya.text = it.name
                binding.tvEmail.text = it.email
                it.image?.let { imageBytes ->
                    binding.profilePic.setImageBitmap(bytesToBitmap(imageBytes))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val uri = data.data!!
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            saveProfileToRoom(auth.currentUser?.displayName, auth.currentUser?.email, bitmap)
            binding.profilePic.setImageBitmap(bitmap)
        }
    }
}
