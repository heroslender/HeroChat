package com.github.heroslender.herochat.service

import com.github.heroslender.herochat.data.UserSettings
import com.github.heroslender.herochat.database.UserSettingsRepository
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class UserService(private val repository: UserSettingsRepository) {
    private val cache = ConcurrentHashMap<UUID, UserSettings>()

    /**
     * Gets settings from cache. If not present, loads synchronously (blocking).
     * Ideally, call loadUserAsync on join.
     */
    fun getSettings(uuid: UUID): UserSettings {
        return cache.computeIfAbsent(uuid) { repository.load(it) }
    }

    /**
     * Loads user data from DB asynchronously and populates cache.
     * Call this on PlayerJoinEvent.
     */
    fun loadUserAsync(uuid: UUID): CompletableFuture<UserSettings> {
        return CompletableFuture.supplyAsync {
            val settings = repository.load(uuid)
            cache[uuid] = settings
            settings
        }
    }

    /**
     * Unload cache for player on leave (PlayerLeaveEvent).
     */
    fun unloadUser(uuid: UUID) {
        cache.remove(uuid)
    }

    /**
     * Updates a user's settings and triggers an async save.
     */
    fun updateSettings(uuid: UUID, modifier: (UserSettings) -> Unit) {
        val settings = getSettings(uuid)
        modifier(settings)
        CompletableFuture.runAsync { repository.save(settings) }
    }
    
    fun saveAll() {
        cache.values.forEach { repository.save(it) }
    }
}