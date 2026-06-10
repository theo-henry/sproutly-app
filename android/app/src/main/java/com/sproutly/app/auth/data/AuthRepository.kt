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

class AuthRepository {
    private val auth = SupabaseClientProvider.client.auth

    fun sessionStatus(): Flow<SessionStatus> = auth.sessionStatus

    fun currentUser(): UserInfo? = auth.currentUserOrNull()

    fun authEvents(): Flow<AuthEvent> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> AuthEvent.SignedIn(status.session.user)
            is SessionStatus.NotAuthenticated -> AuthEvent.SignedOut
            else -> AuthEvent.Loading
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
}

sealed interface AuthEvent {
    data object Loading : AuthEvent
    data object SignedOut : AuthEvent
    data class SignedIn(val user: UserInfo?) : AuthEvent
}
