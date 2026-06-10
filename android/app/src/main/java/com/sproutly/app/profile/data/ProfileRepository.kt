package com.sproutly.app.profile.data

import com.sproutly.app.core.config.AppConfig
import com.sproutly.app.core.network.SupabaseClientProvider
import com.sproutly.app.core.result.AppResult
import com.sproutly.app.profile.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

class ProfileRepository {
    private val client get() = SupabaseClientProvider.client
    private val table get() = client.postgrest.from("profiles")
    private val bucket get() = client.storage.from(AppConfig.AVATAR_BUCKET)

    suspend fun getCurrent(): AppResult<Profile?> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id ?: return AppResult.Success(null)
        table.select { filter { eq("id", userId) } }
            .decodeSingleOrNull<Profile>()
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Failure(it.message ?: "Failed to load profile", it) }
    )

    suspend fun upsert(profile: Profile): AppResult<Profile> = runCatching {
        table.upsert(profile) { select() }.decodeSingle<Profile>()
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Failure(it.message ?: "Failed to save profile", it) }
    )

    suspend fun uploadAvatar(userId: String, bytes: ByteArray, ext: String = "jpg"): AppResult<String> =
        runCatching {
            val path = "$userId/avatar.$ext"
            bucket.upload(path, bytes) { upsert = true }
            path
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Failure(it.message ?: "Failed to upload avatar", it) }
        )

    fun avatarPublicUrl(path: String): String = bucket.publicUrl(path)
}
