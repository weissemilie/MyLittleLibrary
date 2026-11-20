package dk.au.emilieweiss.mylittlelibrary.ui.library

import androidx.lifecycle.SavedStateHandle
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

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)

class BookDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: BookRepository = BookRepository()
) : ViewModel() {

    private val bookId: String = savedStateHandle["bookId"] ?: ""

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getBook(bookId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Noget gik galt") }
                }
                .collect { book ->
                    _uiState.update { it.copy(book = book, isLoading = false) }
                }
        }
    }

    fun deleteBook() {
        viewModelScope.launch {
            try {
                repository.deleteBook(bookId)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "Noget gik galt") }
            }
        }
    }
}
