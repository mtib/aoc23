package dev.mtib.aoc23.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.lang.Exception
import java.nio.file.NoSuchFileException

abstract class AbstractDay(val dayNumber: Int, val yearNumber: Int = 2023) : DaySolver, DayRunner {
    val bufferedInput: Array<String>? by lazy { getLines()?.toTypedArray() }
    private val printedLines: MutableList<String> = mutableListOf()

    protected fun log(line: String) {
        printedLines.add(line)
    }

    private fun getLines(): List<String>? {
        val inputPath = "app/src/main/resources/day${dayNumber}.txt"
        return try {
            readLines(inputPath).filter { it.isNotBlank() }
        } catch (e: NoSuchFileException) {
            println("No input file found for day $dayNumber, fetching from adventofcode.com")
            val token = System.getenv("AOC_SESSION")
            if (token == null || token == "") {
                println("No AOC_SESSION environment variable found, please set it to your session token")
                return null
            }
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://adventofcode.com/${yearNumber}/day/${dayNumber}/input")
                .get()
                .addHeader("Cookie", "session=${token}")
                .build()
            try {
                val response = client.newCall(request).execute()
                val body = response.body!!.string()
                File(inputPath).writeText(body)
                body.lines().filter { it.isNotBlank() }
            } catch(e: Exception) {
                println("Error while fetching input file for day $dayNumber: ${e.javaClass.simpleName} ${e.message}")
                null
            }
        } catch (e : Exception) {
            println("Error while reading input file for day $dayNumber: ${e.javaClass.simpleName} ${e.message}")
            null
        }
    }

    override fun solvePart1(input: Array<String>): String? {
        return null
    }

    override fun solvePart2(input: Array<String>): String? {
        return null
    }

    final override fun runPart1() {
        synchronized(printedLines) {
            printedLines.clear()
            if (bufferedInput == null) {
                println("\u001b[31mNo input file found for day $dayNumber\u001b[0m")
                return
            }
            printSolution(solvePart1(bufferedInput!!))
        }
    }

    final override fun runPart2() {
        synchronized(printedLines) {
            printedLines.clear()
            if (bufferedInput == null) {
                println("\u001b[31mNo input file found for day $dayNumber\u001b[0m")
                return
            }
            printSolution(solvePart2(bufferedInput!!))
        }
    }

    private fun printSolution(solution: String?) {
        if (printedLines.isNotEmpty()) {
            println(printedLines.joinToString("\n") { "| $it" })
        }
        when (solution) {
            null -> println("\u001b[31mNo solution found\u001b[0m")
            else -> println("\u001b[1;33m${solution}\u001b[0m")
        }
    }
}
