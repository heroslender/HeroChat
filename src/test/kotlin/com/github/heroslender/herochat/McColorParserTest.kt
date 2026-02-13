package com.github.heroslender.herochat

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class McColorParserTest {

    @Test
    fun `should parse plain text without colors`() {
        val text = "Hello World"

        val msg = McColorParser.parse(text)

        assertEquals(text, msg, "Text not equal")
    }

    @Test
    fun `should parse text with colors`() {
        val text = "&aHello &eWorld"

        val msg = McColorParser.parse(text)

        assertEquals("{#55FF55}Hello {#FFFF55}World", msg, "Text not equal")
    }

    @Test
    fun `should parse text with style`() {
        val text = "&oHello &lWorld"

        val msg = McColorParser.parse(text)

        assertEquals("{italic}Hello {bold}World", msg, "Text not equal")
    }

    @Test
    fun `should parse text with colors and style`() {
        val text = "&a&oHello &c&lWorld"

        val msg = McColorParser.parse(text)

        assertEquals("{#55FF55}{italic}Hello {#FF5555}{bold}World", msg, "Text not equal")
    }
}