package dk.au.emilieweiss.mylittlelibrary.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.au.emilieweiss.mylittlelibrary.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(isLoggedIn = repository.currentUser != null))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        val trimmedEmail = email.trim()
        validate(trimmedEmail, password)?.let { return setError(it) }
        launchAuth { repository.signIn(trimmedEmail, password) }
    }

    fun signUp(displayName: String, email: String, password: String) {
        val trimmedName = displayName.trim()
        val trimmedEmail = email.trim()
        if (trimmedName.isEmpty()) return setError("Udfyld alle felter")
        validate(trimmedEmail, password)?.let { return setError(it) }
        launchAuth { repository.signUp(trimmedName, trimmedEmail, password) }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState(isLoggedIn = false)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validate(email: String, password: String): String? = when {
        email.isEmpty() || password.isEmpty() -> "Udfyld alle felter"
        password.length < 6 -> "Adgangskoden skal være mindst 6 tegn"
        else -> null
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    private fun launchAuth(block: suspend () -> Unit) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                block()
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Noget gik galt") }
            }
        }
    }
}
