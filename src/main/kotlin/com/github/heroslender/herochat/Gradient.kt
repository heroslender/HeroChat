package com.github.heroslender.herochat

import java.awt.Color

sealed class Gradient {
    companion object {
        private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

        protected fun rgbToHex(r: Int, g: Int, b: Int): String {
            val chars = CharArray(7)
            chars[0] = '#'
            chars[1] = HEX_DIGITS[(r shr 4) and 0xF]
            chars[2] = HEX_DIGITS[r and 0xF]
            chars[3] = HEX_DIGITS[(g shr 4) and 0xF]
            chars[4] = HEX_DIGITS[g and 0xF]
            chars[5] = HEX_DIGITS[(b shr 4) and 0xF]
            chars[6] = HEX_DIGITS[b and 0xF]
            return String(chars)
        }
    }

    /**
     * Returns the color at the specific index as a Hex String (e.g. "#FF0000").
     */
    abstract fun getColorAt(index: Int, totalLength: Int): String

    object Rainbow : Gradient() {
        override fun getColorAt(index: Int, totalLength: Int): String {
            val hue = if (totalLength > 1) index.toFloat() / (totalLength - 1) else 0f
            val rgb = Color.HSBtoRGB(hue, 1f, 1f)
            return String.format("#%06X", (0xFFFFFF and rgb))
        }
    }

    class Linear(startHex: String, endHex: String) : Gradient() {
        private val r1: Int = startHex.substring(1, 3).toInt(16)
        private val g1: Int = startHex.substring(3, 5).toInt(16)
        private val b1: Int = startHex.substring(5, 7).toInt(16)

        // Pre-calculate deltas
        private val dr: Int = endHex.substring(1, 3).toInt(16) - r1
        private val dg: Int = endHex.substring(3, 5).toInt(16) - g1
        private val db: Int = endHex.substring(5, 7).toInt(16) - b1

        override fun getColorAt(index: Int, totalLength: Int): String {
            if (totalLength <= 1) return rgbToHex(r1, g1, b1)

            val ratio = index.toFloat() / (totalLength - 1)

            val r = r1 + (dr * ratio).toInt()
            val g = g1 + (dg * ratio).toInt()
            val b = b1 + (db * ratio).toInt()

            return rgbToHex(r, g, b)
        }
    }
}
