package com.github.heroslender.herochat

import com.github.heroslender.herochat.config.ComponentConfig
import com.github.heroslender.herochat.data.User
import com.github.heroslender.herochat.message.MessageParser
import com.github.heroslender.herochat.message.PlaceholderManager
import com.hypixel.hytale.protocol.MaybeBool
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class MessageParserTest {
    private val sender: User = mock()
    private val permission = "test.permission"

    @BeforeEach
    fun setup() {
        whenever(sender.hasPermission(any())).thenReturn(false)
        whenever(sender.hasPermission(permission)).thenReturn(true)
    }

    @Test
    fun `should parse plain text without placeholders`() {
        val text = "Hello World"

        val msg = MessageParser.parse(sender, text)

        assertEquals(1, msg.children.size, "Unexpected number of children")
        assertEquals("Hello World", msg.children.first().rawText, "Unexpected rawText")
    }

    @Test
    fun `should parse simple color code`() {
        val text = "Hello {#ff0000}Red"
        val msg = MessageParser.parse(sender, text)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assertEquals("#ff0000", msg.children[1].color, "Unexpected color")
    }

    @Test
    fun `should parse formatting tags (bold)`() {
        val text = "Normal {bold}Bold"
        val msg = MessageParser.parse(sender, text)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assertEquals("Normal ", msg.children.first().rawText, "Unexpected rawText")
        assertEquals(MaybeBool.True, msg.children[0].children[0].formattedMessage.bold, "Unexpected bold format")
        assertEquals("Bold", msg.children[0].children[0].rawText, "Unexpected rawText")
    }

    @Test
    fun `should handle escaped placeholders`() {
        val text = "Escaped \\{b}"

        val msg = MessageParser.parse(sender, text)

        assertEquals(1, msg.children.size, "Unexpected number of children")
        assertEquals("Escaped {b}", msg.children[0].rawText, "Unexpected rawText")
    }

    @Test
    fun `should use ComponentConfig map for replacement`() {
        val text = "Click {link}"
        val components = mapOf(
            "link" to ComponentConfig(text = "here", permission = null)
        )

        val msg = MessageParser.parse(sender, text, components)

        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assertEquals("Click here", msg.children[0].rawText, "Unexpected rawText")
    }

    @Test
    fun `should respect permissions in ComponentConfig`() {
        val text = "Click {link}"

        var components = mapOf("link" to ComponentConfig(text = "here", permission = permission))
        var msg = MessageParser.parse(sender, text, components)
        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assertEquals("Click here", msg.children[0].rawText, "Unexpected rawText")


        components = mapOf("link" to ComponentConfig(text = "here", permission = "no.permission"))
        msg = MessageParser.parse(sender, text, components)
        assert(msg.children.isNotEmpty()) { "Unexpected number of children: ${msg.children.size}" }
        assertEquals("Click ", msg.children[0].rawText, "Unexpected rawText")
    }

    @Test
    fun `should replace placeholders`() {
        mockStatic(PlaceholderManager::class.java).use { pmMockStatic ->
            pmMockStatic.`when`<String?> { PlaceholderManager.parsePlaceholder(sender, "player_name") }
                .thenReturn("Player")
            val text = "Hello {player_name}"
            val msg = MessageParser.parse(sender, text)

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
            val msg = MessageParser.parse(sender, text)

            assertEquals(1, msg.children.size, "Unexpected number of children")
            assertEquals("Hello ", msg.children.first().rawText, "Unexpected rawText")
        }
    }
}