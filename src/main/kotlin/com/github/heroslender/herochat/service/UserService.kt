package com.github.heroslender.herochat.service

import com.github.heroslender.herochat.data.ConsoleUser
import com.github.heroslender.herochat.data.PlayerUser
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.data.UserSettings
import com.github.heroslender.herochat.database.UserSettingsRepository
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class UserService(
    private val repository: UserSettingsRepository,
    private val logger: HytaleLogger,
    dataFolder: File,
) {
    private val cache = ConcurrentHashMap<UUID, User>()
    private val blockedPlayers = ConcurrentHashMap<UUID, MutableSet<UUID>>()
    private val playerDataFolder = File(dataFolder, "playerdata")

    init {
        if (!playerDataFolder.exists() && !playerDataFolder.mkdirs()) {
            logger.atSevere().log("Could not create playerdata folder at ${playerDataFolder.absolutePath}.")
        }

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
        blockedPlayers[user.uuid] = loadBlockedPlayers(user.uuid)

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
        blockedPlayers.remove(player.uuid)
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

    fun hasBlocked(blockerUuid: UUID, targetUuid: UUID): Boolean {
        return getBlockedPlayers(blockerUuid).contains(targetUuid)
    }

    fun blockPlayer(blockerUuid: UUID, targetUuid: UUID): Boolean {
        if (blockerUuid == targetUuid) {
            return false
        }

        val blocked = getBlockedPlayers(blockerUuid)
        val added = blocked.add(targetUuid)
        if (added) {
            saveBlockedPlayers(blockerUuid, blocked)
        }

        return added
    }

    fun unblockPlayer(blockerUuid: UUID, targetUuid: UUID): Boolean {
        val blocked = getBlockedPlayers(blockerUuid)
        val removed = blocked.remove(targetUuid)
        if (removed) {
            saveBlockedPlayers(blockerUuid, blocked)
        }

        return removed
    }

    fun unloadAll() {
        cache.clear()
        blockedPlayers.clear()
    }

    private fun getBlockedPlayers(uuid: UUID): MutableSet<UUID> {
        return blockedPlayers.computeIfAbsent(uuid) { loadBlockedPlayers(it) }
    }

    private fun loadBlockedPlayers(uuid: UUID): MutableSet<UUID> {
        val file = getPlayerDataFile(uuid)
        val blocked = ConcurrentHashMap.newKeySet<UUID>()
        if (!file.exists()) {
            return blocked
        }

        try {
            file.forEachLine { line ->
                val value = line.trim()
                if (value.isNotEmpty()) {
                    try {
                        blocked.add(UUID.fromString(value))
                    } catch (_: IllegalArgumentException) {
                        // Ignore invalid UUID lines
                    }
                }
            }
        } catch (e: Exception) {
            logger.atSevere().log("Failed to load blocked players for $uuid: ${e.message}")
            e.printStackTrace()
        }

        return blocked
    }

    private fun saveBlockedPlayers(uuid: UUID, blocked: Set<UUID>) {
        val file = getPlayerDataFile(uuid)

        try {
            if (blocked.isEmpty()) {
                if (file.exists() && !file.delete()) {
                    logger.atInfo().log("Could not delete empty block file for $uuid.")
                }
                return
            }

            if (!playerDataFolder.exists() && !playerDataFolder.mkdirs()) {
                logger.atSevere().log("Could not create playerdata folder at ${playerDataFolder.absolutePath}.")
                return
            }

            val content = blocked
                .map(UUID::toString)
                .sorted()
                .joinToString(System.lineSeparator())
            file.writeText(content)
        } catch (e: Exception) {
            logger.atSevere().log("Failed to save blocked players for $uuid: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun getPlayerDataFile(uuid: UUID): File {
        return File(playerDataFolder, "$uuid.txt")
    }
}
