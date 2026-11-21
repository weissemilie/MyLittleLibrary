package dk.au.emilieweiss.mylittlelibrary.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Book(
    @DocumentId val id: String = "",
    val isbn: String = "",
    val title: String = "",
    val author: String = "",
    val coverUrl: String = "",
    val description: String = "",
    val publisher: String = "",
    val numberOfPages: Int? = null,
    @ServerTimestamp val addedAt: Timestamp? = null,
    val lentTo: String? = null
)
