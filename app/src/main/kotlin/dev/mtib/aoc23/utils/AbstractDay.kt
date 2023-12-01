package dev.mtib.aoc23.utils

abstract class AbstractDay(private val dayNumber: Int) : DaySolver, DayRunner {
    private val bufferedInput: Array<String>
    private val printedLines: MutableList<String> = mutableListOf()

    init {
        val lines = getLines()
        bufferedInput = Array(lines.size) { lines[it] }
    }

    protected fun log(line: String) {
        printedLines.add(line)
    }

    private fun getLines(): List<String> = readLines("app/src/main/resources/day${dayNumber}.txt").filter { it.isNotBlank() }

    override fun solvePart1(input: Array<String>): String? {
        return null
    }

    override fun solvePart2(input: Array<String>): String? {
        return null
    }

    final override fun runPart1() {
        synchronized(printedLines) {
            printedLines.clear()
            printSolution(solvePart1(bufferedInput))
        }
    }

    final override fun runPart2() {
        synchronized(printedLines) {
            printedLines.clear()
            printSolution(solvePart2(bufferedInput))
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
