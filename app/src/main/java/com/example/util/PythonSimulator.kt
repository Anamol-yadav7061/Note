package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PythonSimulator {

    data class ExecutionResult(
        val output: String,
        val executionTimeMs: Long,
        val isSuccess: Boolean
    )

    /**
     * Client-side Python simulation interpreter for developer workspace
     */
    fun execute(code: String): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val outputBuffer = StringBuilder()
        val lines = code.lines()

        val variables = mutableMapOf<String, Any>()
        // Pre-populate standard environment
        variables["USER"] = "Anamol"
        variables["APP"] = "Anmol Notes"
        variables["SYS_STATUS"] = "ONLINE"

        try {
            outputBuffer.appendLine(">>> [SYS.RUN] Initializing Python 3.12 Virtual Environment...")
            outputBuffer.appendLine(">>> [SYS.RUN] Operator: Anamol | Workspace: Anmol Notes")
            outputBuffer.appendLine("--------------------------------------------------")

            var inLoop = false
            var loopVar = ""
            var loopRange = 0
            val loopBody = mutableListOf<String>()

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

                // Check for loops
                if (trimmed.startsWith("for ") && trimmed.contains(" in range(") && trimmed.endsWith(":")) {
                    val match = Regex("for\\s+(\\w+)\\s+in\\s+range\\((\\d+)\\):").find(trimmed)
                    if (match != null) {
                        inLoop = true
                        loopVar = match.groupValues[1]
                        loopRange = match.groupValues[2].toIntOrNull() ?: 1
                        loopBody.clear()
                        continue
                    }
                }

                if (inLoop) {
                    if (line.startsWith("    ") || line.startsWith("\t")) {
                        loopBody.add(trimmed)
                        continue
                    } else {
                        // End of loop body, execute loop
                        executeLoop(loopRange, loopVar, loopBody, variables, outputBuffer)
                        inLoop = false
                    }
                }

                executeSingleStatement(trimmed, variables, outputBuffer)
            }

            if (inLoop) {
                executeLoop(loopRange, loopVar, loopBody, variables, outputBuffer)
            }

            outputBuffer.appendLine("--------------------------------------------------")
            outputBuffer.appendLine("[SYS.EXIT] Process finished with exit code 0")

            val duration = System.currentTimeMillis() - startTime
            return ExecutionResult(outputBuffer.toString(), duration, true)
        } catch (e: Exception) {
            outputBuffer.appendLine("\nTraceback (most recent call last):")
            outputBuffer.appendLine("  File '<stdin>', line 1, in <module>")
            outputBuffer.appendLine("Exception: ${e.message ?: "Runtime execution error"}")
            val duration = System.currentTimeMillis() - startTime
            return ExecutionResult(outputBuffer.toString(), duration, false)
        }
    }

    private fun executeLoop(
        rangeCount: Int,
        varName: String,
        body: List<String>,
        variables: MutableMap<String, Any>,
        outputBuffer: StringBuilder
    ) {
        val safeCount = rangeCount.coerceAtMost(50) // prevent runaway loops
        for (i in 0 until safeCount) {
            variables[varName] = i
            for (stmt in body) {
                executeSingleStatement(stmt, variables, outputBuffer)
            }
        }
    }

    private fun executeSingleStatement(
        statement: String,
        variables: MutableMap<String, Any>,
        outputBuffer: StringBuilder
    ) {
        when {
            statement.startsWith("print(") && statement.endsWith(")") -> {
                val arg = statement.removeSurrounding("print(", ")").trim()
                val evaluated = evaluateExpression(arg, variables)
                outputBuffer.appendLine(evaluated)
            }
            statement.contains("=") && !statement.contains("==") -> {
                val parts = statement.split("=", limit = 2)
                val varName = parts[0].trim()
                val expr = parts[1].trim()
                variables[varName] = evaluateExpression(expr, variables)
            }
            statement.startsWith("import ") -> {
                val mod = statement.removePrefix("import ").trim()
                outputBuffer.appendLine("[MODULE_LOADED] $mod (Native Simulator API)")
            }
            statement.startsWith("from ") -> {
                outputBuffer.appendLine("[MODULE_IMPORT] $statement")
            }
            else -> {
                outputBuffer.appendLine("[EVAL] => ${evaluateExpression(statement, variables)}")
            }
        }
    }

    private fun evaluateExpression(expr: String, variables: Map<String, Any>): String {
        val trimmed = expr.trim()

        // String literals
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length - 1)
        }

        // Variable lookup
        if (variables.containsKey(trimmed)) {
            return variables[trimmed].toString()
        }

        // F-string simulation e.g. f"Hello {USER}"
        if (trimmed.startsWith("f\"") && trimmed.endsWith("\"")) {
            var result = trimmed.substring(2, trimmed.length - 1)
            for ((k, v) in variables) {
                result = result.replace("{$k}", v.toString())
            }
            return result
        }

        // Mathematical operations
        if (trimmed.matches(Regex("\\d+\\s*[+\\-*/%^]\\s*\\d+"))) {
            return try {
                val parts = trimmed.split(Regex("[+\\-*/]"))
                val op = trimmed.first { it in "+-*/" }
                val a = parts[0].trim().toDouble()
                val b = parts[1].trim().toDouble()
                val res = when (op) {
                    '+' -> a + b
                    '-' -> a - b
                    '*' -> a * b
                    '/' -> if (b != 0.0) a / b else Double.NaN
                    else -> 0.0
                }
                if (res % 1 == 0.0) res.toLong().toString() else res.toString()
            } catch (e: Exception) {
                trimmed
            }
        }

        // Special system commands
        if (trimmed.equals("sys.version", ignoreCase = true)) {
            return "Python 3.12.2 (AnmolNotes:x86_64, Kotlin Compose Runtime)"
        }
        if (trimmed.equals("datetime.now()", ignoreCase = true) || trimmed.equals("date.today()", ignoreCase = true)) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }

        return trimmed
    }

    /**
     * Curated developer templates for Python Command Suite
     */
    fun getTemplates(): List<Pair<String, String>> {
        return listOf(
            "Matrix Digital Rain" to """
# Anmol Notes - Matrix Digital Stream Simulator
# Operator: Anamol
import random
import time

chars = "0101010101ABCDEFXYZANAMOL"
print(">>> INITIALIZING NEURAL CYBERPUNK FEED...")
for i in range(8):
    stream = "".join([random.choice(chars) for _ in range(25)])
    print(f"[STREAM {i}] => {stream}")
print(">>> CYBER MATRIX ONLINE: OPERATOR ANAMOL AUTHENTICATED")
            """.trimIndent(),

            "System Diagnostic Probe" to """
# Network & Security Scan Routine
import os, sys, datetime

target_host = "secure.anamol-notes.internal"
print(f"Target: {target_host}")
print("Scanning ports: 8085, 9099, 443, 22...")
print("[PORT 8085] CLOUD FIRESTORE: OPEN (SYNC ACTIVE)")
print("[PORT 9099] GOOGLE AUTH CREDENTIALS: VALIDATED")
print("[STATUS] ALL SERVICES GREEN - USER: ANAMOL")
            """.trimIndent(),

            "Caesar & Hash Tool" to """
# Cryptographic Hash & Cipher Helper
import hashlib

data = "AnmolNotes_Secret_Data_By_Anamol"
print(f"Plaintext: {data}")
print("SHA-256 Digest:")
print("7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069")
print("Status: Verified & Encrypted in Vault")
            """.trimIndent(),

            "Quick Sort Algorithm" to """
# Python QuickSort Implementation
def quicksort(arr):
    if len(arr) <= 1:
        return arr
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    return quicksort(left) + middle + quicksort(right)

sample = [9, 3, 7, 1, 8, 2, 5]
print(f"Original: {sample}")
print("Sorted Result:")
print([1, 2, 3, 5, 7, 8, 9])
            """.trimIndent()
        )
    }
}
