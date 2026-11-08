package com.example.util

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest

object HackerTools {

    // --- 1. MORSE CODE ---
    private val charToMorse = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.",
        'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
        'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.",
        'S' to "...", 'T' to "-", 'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '/' to "-..-.", ' ' to "/"
    )
    private val morseToChar = charToMorse.entries.associate { (k, v) -> v to k }

    fun textToMorse(text: String): String {
        return text.uppercase().map { char ->
            charToMorse[char] ?: "?"
        }.joinToString(" ")
    }

    fun morseToText(morse: String): String {
        val tokens = morse.trim().split(Regex("\\s+"))
        val sb = StringBuilder()
        for (token in tokens) {
            if (token == "/") {
                sb.append(" ")
            } else {
                sb.append(morseToChar[token] ?: "?")
            }
        }
        return sb.toString()
    }

    // --- 2. ENCODERS & DECODERS ---
    fun base64Encode(input: String): String {
        return try {
            java.util.Base64.getEncoder().encodeToString(input.toByteArray(Charsets.UTF_8))
        } catch (t: Throwable) {
            Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    fun base64Decode(input: String): String {
        return try {
            val bytes = try {
                java.util.Base64.getDecoder().decode(input)
            } catch (t: Throwable) {
                Base64.decode(input, Base64.DEFAULT)
            }
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "ERR: Invalid Base64 String"
        }
    }

    fun hexEncode(input: String): String {
        return input.toByteArray(Charsets.UTF_8).joinToString(" ") { "%02X".format(it) }
    }

    fun hexDecode(input: String): String {
        return try {
            val clean = input.replace("\\s+".toRegex(), "").replace("0x", "")
            val bytes = ByteArray(clean.length / 2)
            for (i in bytes.indices) {
                val index = i * 2
                bytes[i] = clean.substring(index, index + 2).toInt(16).toByte()
            }
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "ERR: Invalid Hex Payload"
        }
    }

    fun binaryEncode(input: String): String {
        return input.toByteArray(Charsets.UTF_8).joinToString(" ") { byte ->
            String.format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0')
        }
    }

    fun binaryDecode(input: String): String {
        return try {
            val tokens = input.trim().split(Regex("\\s+"))
            val bytes = tokens.map { it.toInt(2).toByte() }.toByteArray()
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "ERR: Invalid Binary Sequence"
        }
    }

    fun urlEncode(input: String): String {
        return URLEncoder.encode(input, "UTF-8")
    }

    fun urlDecode(input: String): String {
        return try {
            URLDecoder.decode(input, "UTF-8")
        } catch (e: Exception) {
            "ERR: Invalid URL Encoding"
        }
    }

    // --- 3. CIPHERS ---
    fun caesarCipher(input: String, shift: Int): String {
        val s = (shift % 26 + 26) % 26
        return input.map { char ->
            when {
                char.isUpperCase() -> ((char - 'A' + s) % 26 + 'A'.code).toChar()
                char.isLowerCase() -> ((char - 'a' + s) % 26 + 'a'.code).toChar()
                else -> char
            }
        }.joinToString("")
    }

    fun caesarBruteForce(input: String): List<Pair<Int, String>> {
        return (1..25).map { shift ->
            val shifted = caesarCipher(input, 26 - shift)
            shift to shifted
        }
    }

    fun xorCipher(input: String, key: String): String {
        if (key.isEmpty()) return input
        val keyBytes = key.toByteArray(Charsets.UTF_8)
        val inBytes = input.toByteArray(Charsets.UTF_8)
        val outBytes = ByteArray(inBytes.size)
        for (i in inBytes.indices) {
            outBytes[i] = (inBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }
        return try {
            java.util.Base64.getEncoder().encodeToString(outBytes)
        } catch (t: Throwable) {
            Base64.encodeToString(outBytes, Base64.NO_WRAP)
        }
    }

    fun xorDecrypt(base64Input: String, key: String): String {
        if (key.isEmpty()) return base64Input
        return try {
            val inBytes = try {
                java.util.Base64.getDecoder().decode(base64Input)
            } catch (t: Throwable) {
                Base64.decode(base64Input, Base64.DEFAULT)
            }
            val keyBytes = key.toByteArray(Charsets.UTF_8)
            val outBytes = ByteArray(inBytes.size)
            for (i in inBytes.indices) {
                outBytes[i] = (inBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            String(outBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "ERR: Failed to decrypt XOR"
        }
    }

    fun hashSha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun hashMd5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
