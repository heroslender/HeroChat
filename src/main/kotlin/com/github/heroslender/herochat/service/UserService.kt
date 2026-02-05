package com.github.heroslender.herochat.service

import com.github.heroslender.herochat.data.ConsoleUser
import com.github.heroslender.herochat.data.PlayerUser
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.data.UserSettings
import com.github.heroslender.herochat.database.UserSettingsRepository
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class UserService(
    private val repository: UserSettingsRepository,
    private val logger: HytaleLogger,
) {
    private val cache = ConcurrentHashMap<UUID, User>()

    init {
        loadUser(ConsoleUser())
    }

    fun getUser(uuid: UUID): User? {
        return cache[uuid]
    }

    fun getUser(playerRef: PlayerRef): User? {
        return getUser(playerRef.uuid)
    }

    fun getUsers(): Collection<User> {
        return cache.values
    }

    fun getUsersInWorld(worldUuid: UUID): Collection<User> {
        return cache.values.filter { it is PlayerUser && it.player.worldUuid == worldUuid }
    }

    fun getUsersNearby(user: User, distanceSquared: Double): Collection<User>? {
        val worldUuid = (user as? PlayerUser)?.player?.worldUuid ?: return null
        return cache.values.filter {
            it is PlayerUser
                    && it.player.worldUuid == worldUuid
                    && it.distanceSquared(user) <= distanceSquared
        }
    }

    /**
     * Called when a player joins.
     * Initializes the ChatUser with settings and caches it.
     */
    fun onJoin(player: PlayerRef) {
        logger.atFine().log("User joined ${player.username}, loading data...")
        loadUser(PlayerUser(player, UserSettings(player.uuid)))
    }

    fun loadUser(user: User) {
        cache[user.uuid] = user

        fetchUserSettingsAsync(user.uuid).thenAccept { userSettings ->
            user.settings = userSettings
            logger.atInfo().log("User loaded ${user.username}: $userSettings")
        }
    }

    /**
     * Called when a player disconnects.
     * Removes the ChatUser from the cache.
     */
    fun onQuit(player: PlayerRef) {
        logger.atInfo().log("User left ${player.username}, clearing data.")
        cache.remove(player.uuid)
    }

    fun fetchUserSettingsAsync(uuid: UUID): CompletableFuture<UserSettings> {
        return CompletableFuture.supplyAsync {
            repository.load(uuid)
        }
    }

    /**
     * Updates a user's settings and triggers an async save.
     */
    fun User.updateSettings(modifier: (UserSettings) -> Unit) {
        modifier(settings)
        CompletableFuture.runAsync { repository.save(settings.copy()) }
    }

    fun getSpies(): List<User> {
        return cache.values.filter { it.settings.spyMode }
    }

    fun unloadAll() {
        cache.clear()
    }
}