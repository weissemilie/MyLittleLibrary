package dk.au.emilieweiss.mylittlelibrary.data.repository

import android.util.Log
import com.google.gson.JsonParseException
import dk.au.emilieweiss.mylittlelibrary.data.model.Book
import dk.au.emilieweiss.mylittlelibrary.data.remote.OpenLibraryApi
import dk.au.emilieweiss.mylittlelibrary.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class BookLookupException(message: String, cause: Throwable? = null) : Exception(message, cause)

private const val TAG = "BookLookupRepository"

private val sentenceEnd = Regex("""[.!?…]+(?=\s|$)""")

/** Første sætning af en tekst, eller hele teksten hvis der ikke er et sætningsslut. */
private fun firstSentence(text: String): String {
    val clean = text.trim().replace(Regex("\\s+"), " ")
    val end = sentenceEnd.find(clean)?.range?.last
    return (if (end != null) clean.substring(0, end + 1) else clean).take(300)
}

/** Oversætter en exception til en forståelig fejl. Kun rigtige netværksfejl får forbindelsesbeskeden. */
private fun Exception.toLookupException(): BookLookupException = when (this) {
    is SocketTimeoutException ->
        BookLookupException("Open Library svarer for langsomt. Prøv igen om lidt.", this)
    is UnknownHostException, is ConnectException ->
        BookLookupException("Kunne ikke få forbindelse. Tjek din internetforbindelse.", this)
    is SSLException ->
        BookLookupException("Den sikre forbindelse til Open Library kunne ikke oprettes.", this)
    is HttpException ->
        BookLookupException("Open Library svarede med en fejl (${code()}). Prøv igen senere.", this)
    is JsonParseException ->
        BookLookupException("Svaret fra Open Library kunne ikke læses.", this)
    is IOException ->
        BookLookupException("Der opstod en netværksfejl. Prøv igen.", this)
    else ->
        BookLookupException("Noget uventet gik galt under opslaget.", this)
}

class BookLookupRepository(
    private val api: OpenLibraryApi = RetrofitClient.openLibraryApi
) {
    /** Returnerer bogen, eller null hvis Open Library ikke kender ISBN'et. */
    suspend fun lookupByIsbn(isbn: String): Book? = withContext(Dispatchers.IO) {
        val cleanIsbn = isbn.filterNot { it.isWhitespace() || it == '-' }
        if (cleanIsbn.isEmpty()) return@withContext null

        val response = try {
            api.getBooksByIsbn("ISBN:$cleanIsbn")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Opslag af ISBN $cleanIsbn fejlede: ${e::class.java.name}: ${e.message}", e)
            throw e.toLookupException()
        }

        val data = response["ISBN:$cleanIsbn"] ?: return@withContext null
        val title = data.title?.takeIf { it.isNotBlank() } ?: return@withContext null

        Book(
            isbn = cleanIsbn,
            title = title,
            author = data.authors.orEmpty().mapNotNull { it.name?.takeIf(String::isNotBlank) }.joinToString(", "),
            // ?default=false giver 404 i stedet for et tomt billede, så skærmen kan vise sit eget pladsholderkort
            description = data.excerpts.orEmpty()
                .firstNotNullOfOrNull { it.text?.takeIf(String::isNotBlank) }
                ?.let(::firstSentence)
                .orEmpty(),
            publisher = data.publishers.orEmpty()
                .mapNotNull { it.name?.takeIf(String::isNotBlank) }
                .joinToString(", "),
            numberOfPages = data.numberOfPages?.takeIf { it > 0 },
            coverUrl = data.cover?.large?.takeIf { it.isNotBlank() }
                ?: "https://covers.openlibrary.org/b/isbn/$cleanIsbn-L.jpg?default=false"
        )
    }
}
