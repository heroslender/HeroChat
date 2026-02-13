package com.github.heroslender.herochat

import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.dependencies.PlaceholderAPIDependency
import com.hypixel.hytale.protocol.MaybeBool
import com.hypixel.hytale.server.core.permissions.PermissionsModule
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.*

class ComponentParserTest {
    private lateinit var parser: ComponentParser
    private val senderId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        parser = ComponentParser()
    }

    @Test
    fun `should parse plain text without placeholders`() {
        val text = "Hello World"

        val msg = parser.parse(senderId, text)

        assert(msg.children.size == 1) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children.first().rawText.equals("Hello World")) { "Unexpected rawText: ${msg.children.first().rawText}" }
    }

    @Test
    fun `should parse simple color code`() {
        val text = "Hello {#ff0000}Red"
        val msg = parser.parse(senderId, text)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children[1].color.equals("#ff0000")) { "Unexpected color: ${msg.children[1].color}" }
    }

    @Test
    fun `should parse formatting tags (bold)`() {
        val text = "Normal {bold}Bold"
        val msg = parser.parse(senderId, text)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children[1].formattedMessage.bold == MaybeBool.True) { "Unexpected bold format: ${msg.children[1].formattedMessage.bold}" }
    }

    @Test
    fun `should replace placeholders using PlaceholderAPI`() {
        val player: PlayerRef = mock()
        val universe: Universe = mock()
        val universeMockedStatic = mockStatic(Universe::class.java)
        universeMockedStatic.`when`<Universe> { Universe.get() }.thenReturn(universe)
        whenever(universe.getPlayer(any())).thenReturn(player)
        val papiMockedStatic = mockStatic(PlaceholderAPIDependency::class.java)
        papiMockedStatic.`when`<String?> { PlaceholderAPIDependency.parsePlaceholder(player, "player_name") }
            .thenReturn("Steve")

        val text = "Hello {player_name}"
        val msg = parser.parse(senderId, text)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        papiMockedStatic.verify { PlaceholderAPIDependency.parsePlaceholder(player, "player_name") }

        universeMockedStatic.close()
        papiMockedStatic.close()
    }

    @Test
    fun `should handle escaped placeholders`() {
        val text = "Escaped \\{b}"
        val msg = parser.parse(senderId, text)
        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children[1].rawText.equals("{")) { "Unexpected text: ${msg.children[1].formattedMessage.bold}" }
        assert(msg.children[2].rawText.equals("b}")) { "Unexpected text: ${msg.children[1].formattedMessage.bold}" }
    }

    @Test
    fun `should use ComponentConfig map for replacement`() {
        val text = "Click {link}"
        val components = mapOf(
            "link" to ComponentConfig(text = "here", permission = null)
        )

        val msg = parser.parse(senderId, text, components)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children[1].rawText.equals("here")) { "Unexpected text: ${msg.children[1].formattedMessage.bold}" }
    }

    @Test
    fun `should respect permissions in ComponentConfig`() {
        val text = "Click {link}"
        val components = mapOf(
            "link" to ComponentConfig(text = "here", permission = "some.permission")
        )

        mockStatic(PermissionsModule::class.java).use { permsMock ->
            val perms: PermissionsModule = mock()
            permsMock.`when`<PermissionsModule> { PermissionsModule.get() }.thenReturn(perms)
            whenever(perms.hasPermission(senderId, "some.permission")).thenReturn(true)

            var msg = parser.parse(senderId, text, components)
            assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
            assert(msg.children[1].rawText.equals("here")) { "Unexpected text: ${msg.children[1].formattedMessage.bold}" }

            whenever(perms.hasPermission(senderId, "some.permission")).thenReturn(false)

            parser = ComponentParser()
            msg = parser.parse(senderId, text, components)
            assert(msg.children.size == 1) { "Unexpected number of children: ${msg.children.size}" }
        }
    }

    @Test
    fun `should ignore invalid or unknown placeholders`() {
        val player: PlayerRef = mock()
        val universe: Universe = mock()
        val universeMockedStatic = mockStatic(Universe::class.java)
        universeMockedStatic.`when`<Universe> { Universe.get() }.thenReturn(universe)
        whenever(universe.getPlayer(any())).thenReturn(player)
        val papiMockedStatic = mockStatic(PlaceholderAPIDependency::class.java)
        papiMockedStatic.`when`<String?> { PlaceholderAPIDependency.parsePlaceholder(player, "player_name") }
            .thenReturn(null)
        val text = "Hello {unknown_placeholder}"
        val msg = parser.parse(senderId, text)

        assert(msg.children.size == 1) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children.first().rawText.equals("Hello ")) { "Unexpected rawText: ${msg.children.first().rawText}" }

        universeMockedStatic.close()
        papiMockedStatic.close()
    }
}