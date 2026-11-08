package com.example

import com.example.util.HackerTools
import com.example.util.PythonSimulator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun morseCodeTranslation_isCorrect() {
        val original = "ANAMOL"
        val morse = HackerTools.textToMorse(original)
        val decoded = HackerTools.morseToText(morse)
        assertEquals(".- -. .- -- --- .-..", morse)
        assertEquals("ANAMOL", decoded)
    }

    @Test
    fun base64AndHex_areCorrect() {
        val original = "AnmolNotes"
        val b64 = HackerTools.base64Encode(original)
        val decodedB64 = HackerTools.base64Decode(b64)
        assertEquals(original, decodedB64)

        val hex = HackerTools.hexEncode("ANAMOL")
        val decodedHex = HackerTools.hexDecode(hex)
        assertEquals("ANAMOL", decodedHex)
    }

    @Test
    fun caesarAndXorCiphers_areCorrect() {
        val plaintext = "SECRET"
        val caesar = HackerTools.caesarCipher(plaintext, 3)
        assertEquals("VHFUHW", caesar)
        val decryptedCaesar = HackerTools.caesarCipher(caesar, 23)
        assertEquals("SECRET", decryptedCaesar)

        val xor = HackerTools.xorCipher("HELLO", "KEY")
        val xorDecrypted = HackerTools.xorDecrypt(xor, "KEY")
        assertEquals("HELLO", xorDecrypted)
    }

    @Test
    fun pythonSimulator_executesBasicStatements() {
        val code = """
            name = "Anamol"
            print(f"Operator: {name}")
            x = 10 + 5
            print(x)
        """.trimIndent()
        val result = PythonSimulator.execute(code)
        assertTrue(result.isSuccess)
        assertTrue(result.output.contains("Operator: Anamol"))
        assertTrue(result.output.contains("15"))
    }

    @Test
    fun userSettings_holdsGeminiApiKeyAndAnamolName() {
        val settings = com.example.data.model.UserSettings(
            userId = "test_user",
            userName = "Anamol",
            geminiApiKey = "AIzaSyFakeTestKey123"
        )
        assertEquals("Anamol", settings.userName)
        assertEquals("AIzaSyFakeTestKey123", settings.geminiApiKey)
    }
}

