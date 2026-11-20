package dk.au.emilieweiss.mylittlelibrary.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(displayName: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Brugeren blev ikke oprettet")

        user.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
        ).await()

        val userDoc = mapOf(
            "displayName" to displayName,
            "email" to email,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("users").document(user.uid).set(userDoc).await()
    }

    fun signOut() {
        auth.signOut()
    }
}
