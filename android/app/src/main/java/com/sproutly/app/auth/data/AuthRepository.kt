package com.sproutly.app.auth.data

import com.sproutly.app.core.config.AppConfig
import com.sproutly.app.core.network.SupabaseClientProvider
import com.sproutly.app.core.result.AppResult
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

class AuthRepository {
    private val auth = SupabaseClientProvider.client.auth

    fun sessionStatus(): Flow<SessionStatus> = auth.sessionStatus

    fun currentUser(): UserInfo? = auth.currentUserOrNull()

    fun authEvents(): Flow<AuthEvent> = auth.sessionStatus.map { status ->
        status.toAuthEvent()
    }

    suspend fun restoreSession(timeoutMillis: Long = 5_000): AuthEvent {
        val loadResult = withTimeoutOrNull(timeoutMillis) {
            auth.loadFromStorage()
        }

        if (loadResult == null) {
            auth.clearSession()
            return AuthEvent.SignedOut
        }

        return auth.sessionStatus.value.toAuthEvent().let { event ->
            if (event == AuthEvent.Loading) AuthEvent.SignedOut else event
        }
    }

    suspend fun signIn(email: String, password: String): AppResult<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }.toAppResult()

    suspend fun signUp(email: String, password: String): AppResult<Unit> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }.toAppResult()

    suspend fun signInDemo(): AppResult<Unit> =
        signIn(AppConfig.demoEmail, AppConfig.demoPassword)

    suspend fun signOut(): AppResult<Unit> = runCatching { auth.signOut() }.toAppResult()

    private fun <T> Result<T>.toAppResult(): AppResult<Unit> = fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Failure(it.message ?: "Unknown error", it) }
    )

    private fun SessionStatus.toAuthEvent(): AuthEvent = when (this) {
        is SessionStatus.Authenticated -> AuthEvent.SignedIn(session.user)
        is SessionStatus.NotAuthenticated -> AuthEvent.SignedOut
        is SessionStatus.RefreshFailure -> AuthEvent.SignedOut
        SessionStatus.Initializing -> AuthEvent.Loading
    }
}

sealed interface AuthEvent {
    data object Loading : AuthEvent
    data object SignedOut : AuthEvent
    data class SignedIn(val user: UserInfo?) : AuthEvent
}
