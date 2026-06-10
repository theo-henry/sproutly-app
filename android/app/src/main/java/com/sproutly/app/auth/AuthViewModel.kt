package com.sproutly.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sproutly.app.auth.data.AuthEvent
import com.sproutly.app.auth.data.AuthRepository
import com.sproutly.app.core.result.AppResult
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: UserInfo?) : AuthState
}

data class AuthFormState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form.asStateFlow()

    init {
        viewModelScope.launch {
            repo.authEvents().collect { event ->
                _state.value = when (event) {
                    AuthEvent.Loading -> AuthState.Loading
                    AuthEvent.SignedOut -> AuthState.SignedOut
                    is AuthEvent.SignedIn -> AuthState.SignedIn(event.user)
                }
            }
        }
    }

    fun signIn(email: String, password: String) = submit { repo.signIn(email, password) }
    fun signUp(email: String, password: String) = submit { repo.signUp(email, password) }
    fun signInDemo() = submit { repo.signInDemo() }

    fun signOut() {
        viewModelScope.launch { repo.signOut() }
    }

    fun clearError() { _form.value = _form.value.copy(errorMessage = null) }

    private fun submit(block: suspend () -> AppResult<Unit>) {
        viewModelScope.launch {
            _form.value = AuthFormState(isSubmitting = true)
            when (val result = block()) {
                is AppResult.Success -> _form.value = AuthFormState()
                is AppResult.Failure -> _form.value = AuthFormState(errorMessage = result.message)
            }
        }
    }
}
