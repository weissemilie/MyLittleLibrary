package dk.au.emilieweiss.mylittlelibrary.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dk.au.emilieweiss.mylittlelibrary.data.model.Book
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BookRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun booksCollection(): CollectionReference {
        val uid = auth.currentUser?.uid ?: error("Ingen bruger er logget ind")
        return firestore.collection("users").document(uid).collection("books")
    }

    fun getBooks(): Flow<List<Book>> = callbackFlow {
        val registration = booksCollection()
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(
                    snapshot?.documents?.mapNotNull {
                        it.toObject(Book::class.java, ServerTimestampBehavior.ESTIMATE)
                    } ?: emptyList()
                )
            }
        awaitClose { registration.remove() }
    }

    fun getBook(bookId: String): Flow<Book?> = callbackFlow {
        val registration = booksCollection().document(bookId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Book::class.java, ServerTimestampBehavior.ESTIMATE))
            }
        awaitClose { registration.remove() }
    }

    suspend fun addBook(book: Book) {
        booksCollection().add(book).await()
    }

    suspend fun deleteBook(bookId: String) {
        booksCollection().document(bookId).delete().await()
    }
}
