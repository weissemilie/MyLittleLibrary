package dk.au.emilieweiss.mylittlelibrary.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.au.emilieweiss.mylittlelibrary.data.model.Book
import dk.au.emilieweiss.mylittlelibrary.data.repository.BookLookupException
import dk.au.emilieweiss.mylittlelibrary.data.repository.BookLookupRepository
import dk.au.emilieweiss.mylittlelibrary.data.repository.BookRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    /** Kameraet scanner. Alle andre tilstande betyder, at scanningen er på pause. */
    data object Scanning : ScanUiState
    data class LookingUp(val isbn: String) : ScanUiState
    data class Found(val book: Book, val isSaving: Boolean = false) : ScanUiState
    data class NotFound(
        val isbn: String,
        val title: String = "",
        val author: String = "",
        val isSaving: Boolean = false
    ) : ScanUiState
    data class AlreadyInLibrary(val book: Book) : ScanUiState
    data class Saved(val title: String) : ScanUiState
    data class Error(val isbn: String, val message: String) : ScanUiState
}

class ScanViewModel(
    private val bookRepository: BookRepository = BookRepository(),
    private val lookupRepository: BookLookupRepository = BookLookupRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Scanning)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var job: Job? = null

    val isScanning: Boolean
        get() = _uiState.value is ScanUiState.Scanning

    /** Kaldes fra kameraets analyzer. Kun den første kode, mens vi scanner, bliver slået op. */
    fun onBarcodeDetected(isbn: String) {
        if (_uiState.compareAndSet(ScanUiState.Scanning, ScanUiState.LookingUp(isbn))) {
            lookup(isbn)
        }
    }

    /** Manuel indtastning bruger samme flow som scanning. */
    fun onManualIsbn(input: String) {
        val isbn = clean(input)
        if (!Regex("""\d{13}|\d{9}[\dX]""").matches(isbn)) {
            job?.cancel()
            _uiState.value = ScanUiState.Error(
                input.trim(),
                "Det ISBN ser ikke rigtigt ud. Et ISBN har 10 eller 13 cifre."
            )
            return
        }
        job?.cancel()
        _uiState.value = ScanUiState.LookingUp(isbn)
        lookup(isbn)
    }

    fun retry(isbn: String) = onManualIsbn(isbn)

    fun scanAgain() {
        job?.cancel()
        _uiState.value = ScanUiState.Scanning
    }

    fun onTitleChange(title: String) {
        _uiState.update { if (it is ScanUiState.NotFound) it.copy(title = title) else it }
    }

    fun onAuthorChange(author: String) {
        _uiState.update { if (it is ScanUiState.NotFound) it.copy(author = author) else it }
    }

    fun save() {
        val state = _uiState.value
        val book = when (state) {
            is ScanUiState.Found -> state.book
            is ScanUiState.NotFound -> {
                if (state.title.isBlank()) return
                Book(isbn = state.isbn, title = state.title.trim(), author = state.author.trim())
            }
            else -> return
        }
        _uiState.update {
            when (it) {
                is ScanUiState.Found -> it.copy(isSaving = true)
                is ScanUiState.NotFound -> it.copy(isSaving = true)
                else -> it
            }
        }
        job?.cancel()
        job = viewModelScope.launch {
            try {
                bookRepository.addBook(book)
                _uiState.value = ScanUiState.Saved(book.title)
                // Kort bekræftelse, derefter kan scanningen fortsætte
                delay(2000)
                _uiState.update { if (it is ScanUiState.Saved) ScanUiState.Scanning else it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error(book.isbn, e.localizedMessage ?: "Bogen kunne ikke gemmes.")
            }
        }
    }

    private fun lookup(isbn: String) {
        job = viewModelScope.launch {
            try {
                val existing = bookRepository.getBooks().first().firstOrNull { clean(it.isbn) == isbn }
                if (existing != null) {
                    _uiState.value = ScanUiState.AlreadyInLibrary(existing)
                    return@launch
                }
                val book = lookupRepository.lookupByIsbn(isbn)
                _uiState.value = if (book != null) ScanUiState.Found(book) else ScanUiState.NotFound(isbn)
            } catch (e: CancellationException) {
                throw e
            } catch (e: BookLookupException) {
                _uiState.value = ScanUiState.Error(isbn, e.message ?: "Opslaget fejlede.")
            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error(isbn, e.localizedMessage ?: "Noget gik galt.")
            }
        }
    }

    private fun clean(isbn: String) =
        isbn.filterNot { it.isWhitespace() || it == '-' }.uppercase()
}
