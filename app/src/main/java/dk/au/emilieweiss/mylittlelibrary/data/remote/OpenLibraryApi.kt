package dk.au.emilieweiss.mylittlelibrary.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {
    /**
     * Svaret er et objekt hvor nøglen er "ISBN:{isbn}". Et ukendt ISBN giver et tomt objekt.
     */
    @GET("api/books?format=json&jscmd=data")
    suspend fun getBooksByIsbn(@Query("bibkeys") bibkeys: String): Map<String, OpenLibraryBook>
}

data class OpenLibraryBook(
    val title: String? = null,
    val authors: List<OpenLibraryAuthor>? = null,
    @SerializedName("publish_date") val publishDate: String? = null,
    @SerializedName("number_of_pages") val numberOfPages: Int? = null,
    val publishers: List<OpenLibraryPublisher>? = null,
    val excerpts: List<OpenLibraryExcerpt>? = null,
    val cover: OpenLibraryCover? = null
)

data class OpenLibraryAuthor(
    val name: String? = null
)

data class OpenLibraryPublisher(
    val name: String? = null
)

data class OpenLibraryExcerpt(
    val text: String? = null
)

data class OpenLibraryCover(
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null
)
