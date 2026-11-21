package dk.au.emilieweiss.mylittlelibrary.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.au.emilieweiss.mylittlelibrary.data.model.Book
import dk.au.emilieweiss.mylittlelibrary.data.repository.BookLookupException
import dk.au.emilieweiss.mylittlelibrary.data.repository.BookLookupRepository
import dk.au.emilieweiss.mylittlelibrary.data.repository.BookRepository
import kotlinx.coroutines.Job
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

data class AddBookUiState(
    val isbn: String = "",
    val title: String = "",
    val author: String = "",
    val coverUrl: String = "",
    val description: String = "",
    val publisher: String = "",
    val numberOfPages: Int? = null,
    val isLookingUp: Boolean = false,
    val lookupError: String? = null
)

class LibraryViewModel(
    private val repository: BookRepository = BookRepository(),
    private val lookupRepository: BookLookupRepository = BookLookupRepository()
) : ViewModel() {

    private var lookupJob: Job? = null

    private val _addBookState = MutableStateFlow(AddBookUiState())
    val addBookState: StateFlow<AddBookUiState> = _addBookState.asStateFlow()

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

    fun onNewIsbnChange(isbn: String) {
        _addBookState.update { it.copy(isbn = isbn, lookupError = null) }
    }

    fun onNewTitleChange(title: String) {
        _addBookState.update { it.copy(title = title) }
    }

    fun onNewAuthorChange(author: String) {
        _addBookState.update { it.copy(author = author) }
    }

    fun resetAddBook() {
        lookupJob?.cancel()
        _addBookState.value = AddBookUiState()
    }

    fun lookupIsbn() {
        val isbn = _addBookState.value.isbn
        if (isbn.isBlank()) {
            _addBookState.update { it.copy(lookupError = "Skriv et ISBN, før du henter bogen.") }
            return
        }
        lookupJob?.cancel()
        _addBookState.update { it.copy(isLookingUp = true, lookupError = null) }
        lookupJob = viewModelScope.launch {
            try {
                val book = lookupRepository.lookupByIsbn(isbn)
                if (book == null) {
                    _addBookState.update {
                        it.copy(
                            isLookingUp = false,
                            lookupError = "Vi kunne ikke finde en bog med det ISBN. Du kan udfylde felterne selv."
                        )
                    }
                } else {
                    _addBookState.update {
                        it.copy(
                            isbn = book.isbn,
                            title = book.title,
                            author = book.author,
                            coverUrl = book.coverUrl,
                            description = book.description,
                            publisher = book.publisher,
                            numberOfPages = book.numberOfPages,
                            isLookingUp = false
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: BookLookupException) {
                _addBookState.update { it.copy(isLookingUp = false, lookupError = e.message) }
            } catch (e: Exception) {
                _addBookState.update { it.copy(isLookingUp = false, lookupError = "Noget gik galt. Prøv igen.") }
            }
        }
    }

    fun addBook(
        title: String,
        author: String,
        isbn: String,
        coverUrl: String = "",
        description: String = "",
        publisher: String = "",
        numberOfPages: Int? = null
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            _uiState.update { it.copy(error = "Titel er påkrævet") }
            return
        }
        launchWrite {
            repository.addBook(
                Book(
                    title = trimmedTitle,
                    author = author.trim(),
                    isbn = isbn.trim(),
                    coverUrl = coverUrl,
                    description = description.trim(),
                    publisher = publisher.trim(),
                    numberOfPages = numberOfPages
                )
            )
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
