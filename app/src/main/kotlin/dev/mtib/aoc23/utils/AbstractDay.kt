package dev.mtib.aoc23.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.file.NoSuchFileException

typealias dbg = AbstractDay.() -> Unit

abstract class AbstractDay(val dayNumber: Int, val yearNumber: Int = 2023) : DaySolver, DayRunner {
    val bufferedInput: Array<String>? by lazy { getLines()?.toTypedArray() }
    private val printedLines: MutableList<String> = mutableListOf()

    fun log(line: String) {
        if (mode == Mode.RUNNING) {
            printedLines.addAll(line.lines())
        }
    }

    enum class Mode {
        RUNNING, TIMING
    }

    private var mode = Mode.TIMING

    fun debug(block: AbstractDay.() -> Unit) {
        if (mode == Mode.RUNNING) {
            block.invoke(this)
        }
    }

    private fun getLines(): List<String>? {
        val inputPath = "app/src/main/resources/day${dayNumber}.txt"
        return try {
            readLines(inputPath)
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
                body.lines()
            } catch (e: Exception) {
                println("Error while fetching input file for day $dayNumber: ${e.javaClass.simpleName} ${e.message}")
                null
            }
        } catch (e: Exception) {
            println("Error while reading input file for day $dayNumber: ${e.javaClass.simpleName} ${e.message}")
            null
        }
    }

    override fun solvePart1(input: Array<String>): Any? {
        return null
    }

    override fun solvePart2(input: Array<String>): Any? {
        return null
    }

    private fun runPart(part: Int) {
        synchronized(printedLines) {
            printedLines.clear()
            if (bufferedInput == null) {
                println("\u001b[31mNo input file found for day $dayNumber\u001b[0m")
                return
            }
            mode = Mode.RUNNING
            printSolution(
                try {
                    when (part) {
                        1 -> solvePart1(bufferedInput!!)
                        2 -> solvePart2(bufferedInput!!)
                        else -> throw IllegalArgumentException("Part $part is not a valid part.")
                    }
                } catch (e: Exception) {
                    println("Error while running part $part: ${e.stackTraceToString()}")
                    null
                },
                part
            )
            mode = Mode.TIMING
        }
    }

    final override fun runPart1() {
        runPart(1)
    }

    final override fun runPart2() {
        runPart(2)
    }

    private fun printSolution(solution: Any?, part: Int) {
        if (printedLines.isNotEmpty()) {
            println(printedLines.joinToString("\n") { "| $it" })
        }
        when (solution) {
            null -> println("\u001b[31mNo solution found\u001b[0m")
            else -> {
                println(
                    buildString {
                        append("\u001b[1;33m")
                        append(solution)
                        append("\u001b[0m")
                        when (val k = Knowledge.check(yearNumber, dayNumber, part, solution)) {
                            is Knowledge.Correct -> append(" \u001b[32m✓\u001b[0m")
                            is Knowledge.TooBig -> append(" \u001b[31m✗ (too big, must be < ${k.reference}) \u001b[0m")
                            is Knowledge.TooSmall -> append(" \u001b[31m✗ (too small, must be > ${k.reference}) \u001b[0m")
                            is Knowledge.Incorrect -> append(" \u001b[31m✗ (expected ${k.reference}) \u001b[0m")
                            is Knowledge.Unknown -> append(" \u001b[33m?\u001b[0m")
                        }
                    }
                )
            }
        }
    }
}
