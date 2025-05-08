package com.example.myroadr.helpers

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()


    fun registerUser(email: String, password: String, activity: Activity, onSuccess: (FirebaseUser?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onSuccess(auth.currentUser)
                } else {
                    Toast.makeText(activity, "Inscription échouée : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    fun loginUser(email: String, password: String, activity: Activity, onSuccess: (FirebaseUser?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onSuccess(auth.currentUser)
                } else {
                    Toast.makeText(activity, "Connexion échouée : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // 🔓 Logout
    fun logout() {
        auth.signOut()
    }

    fun addToFirestore(collection: String, data: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collection)
            .add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
    fun uploadFile(path: String, fileBytes: ByteArray, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val ref = storage.reference.child(path)
        val uploadTask = ref.putBytes(fileBytes)

        uploadTask
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    fun getDocument(collection: String, documentId: String, onSuccess: (Map<String, Any>?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collection).document(documentId)
            .get()
            .addOnSuccessListener { document ->
                onSuccess(document.data)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun resetPassword(email: String, activity: Activity, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "Email de réinitialisation envoyé à $email", Toast.LENGTH_LONG).show()
                    onSuccess()
                } else {
                    val exception = task.exception
                    if (exception != null) {
                        Toast.makeText(activity, "Erreur : ${exception.message}", Toast.LENGTH_LONG).show()
                        onFailure(exception)
                    }
                }
            }
    }

}
