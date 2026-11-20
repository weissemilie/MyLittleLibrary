package dk.au.emilieweiss.mylittlelibrary.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.au.emilieweiss.mylittlelibrary.data.model.Book
import dk.au.emilieweiss.mylittlelibrary.data.repository.BookRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class LibraryViewModel(
    private val repository: BookRepository = BookRepository()
) : ViewModel() {

    private var allBooks: List<Book> = emptyList()

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getBooks()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Noget gik galt") }
                }
                .collect { books ->
                    allBooks = books
                    _uiState.update {
                        it.copy(books = filter(books, it.searchQuery), isLoading = false, error = null)
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, books = filter(allBooks, query)) }
    }

    fun addBook(title: String, author: String, isbn: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            _uiState.update { it.copy(error = "Titel er påkrævet") }
            return
        }
        launchWrite {
            repository.addBook(Book(title = trimmedTitle, author = author.trim(), isbn = isbn.trim()))
        }
    }

    fun deleteBook(bookId: String) {
        launchWrite { repository.deleteBook(bookId) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun filter(books: List<Book>, query: String): List<Book> {
        val q = query.trim()
        if (q.isEmpty()) return books
        return books.filter {
            it.title.contains(q, ignoreCase = true) || it.author.contains(q, ignoreCase = true)
        }
    }

    private fun launchWrite(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "Noget gik galt") }
            }
        }
    }
}
