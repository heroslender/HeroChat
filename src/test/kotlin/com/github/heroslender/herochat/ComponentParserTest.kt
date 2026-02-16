package com.github.heroslender.herochat

import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.message.ComponentParser
import com.github.heroslender.herochat.message.PlaceholderManager
import com.hypixel.hytale.protocol.MaybeBool
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class ComponentParserTest {
    private lateinit var parser: ComponentParser
    private val sender: User = mock()

    private val permission = "test.permission"

    @BeforeEach
    fun setup() {
        parser = ComponentParser(false)

        whenever(sender.hasPermission(any())).thenReturn(false)
        whenever(sender.hasPermission(permission)).thenReturn(true)
    }

    @Test
    fun `should parse plain text without placeholders`() {
        val text = "Hello World"

        val msg = parser.parse(sender, text)

        assert(msg.children.size == 1) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children.first().rawText.equals("Hello World")) { "Unexpected rawText: ${msg.children.first().rawText}" }
    }

    @Test
    fun `should parse simple color code`() {
        val text = "Hello {#ff0000}Red"
        val msg = parser.parse(sender, text)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children[1].color.equals("#ff0000")) { "Unexpected color: ${msg.children[1].color}" }
    }

    @Test
    fun `should parse formatting tags (bold)`() {
        val text = "Normal {bold}Bold"
        val msg = parser.parse(sender, text)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children[0].rawText == "Normal ") { "Unexpected rawText" }
        assert(msg.children[0].children[0].formattedMessage.bold == MaybeBool.True) { "Unexpected bold format: ${msg.children[1].formattedMessage.bold}" }
        assert(msg.children[0].children[0].rawText == "Bold") { "Unexpected rawText" }
    }

    @Test
    fun `should handle escaped placeholders`() {
        val text = "Escaped \\{b}"
        val msg = parser.parse(sender, text)
        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children[0].rawText.equals("Escaped {b}")) { "Unexpected text: ${msg.children[1].rawText}" }
    }

    @Test
    fun `should use ComponentConfig map for replacement`() {
        val text = "Click {link}"
        val components = mapOf(
            "link" to ComponentConfig(text = "here", permission = null)
        )

        val msg = parser.parse(sender, text, components)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children[0].rawText.equals("Click here")) { "Unexpected rawText" }
    }

    @Test
    fun `should respect permissions in ComponentConfig`() {
        val text = "Click {link}"

        var components = mapOf("link" to ComponentConfig(text = "here", permission = permission))
        var msg = parser.parse(sender, text, components)
        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assert(msg.children[0].rawText.equals("Click here")) { "Unexpected rawText" }


        components = mapOf("link" to ComponentConfig(text = "here", permission = "no.permission"))
        parser = ComponentParser(false)
        msg = parser.parse(sender, text, components)
        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assertEquals("Click ", msg.children[0].rawText, "Unexpected rawText")
    }

    @Test
    fun `should replace placeholders`() {
        mockStatic(PlaceholderManager::class.java).use { pmMockStatic ->
            pmMockStatic.`when`<String?> { PlaceholderManager.parsePlaceholder(sender, "player_name") }
                .thenReturn("Player")
            val text = "Hello {player_name}"
            val msg = parser.parse(sender, text)

            assertEquals(1, msg.children.size, "Unexpected number of children")
            assertEquals("Hello Player", msg.children.first().rawText, "Unexpected rawText")
        }
    }

    @Test
    fun `should ignore invalid or unknown placeholders`() {
        mockStatic(PlaceholderManager::class.java).use { pmMockStatic ->
            pmMockStatic.`when`<String?> { PlaceholderManager.parsePlaceholder(sender, "player_name") }
                .thenReturn(null)
            val text = "Hello {player_name}"
            val msg = parser.parse(sender, text)

            assertEquals(1, msg.children.size, "Unexpected number of children")
            assertEquals("Hello ", msg.children.first().rawText, "Unexpected rawText")
        }
    }
}