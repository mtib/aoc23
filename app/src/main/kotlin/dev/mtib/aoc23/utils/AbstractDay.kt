package dev.mtib.aoc23.utils

import com.andreapivetta.kolor.Color
import com.andreapivetta.kolor.Kolor

abstract class AbstractDay(private val dayNumber: Int) : DaySolver, DayRunner {
    private val bufferedInput: Array<String>

    init {
        val lines = getLines()
        bufferedInput = Array(lines.size) { lines[it] }
    }

    private fun getLines(): List<String> = readLines("app/src/main/resources/day${dayNumber}.txt")

    override fun solvePart1(input: Array<String>): String? {
        return null
    }

    override fun solvePart2(input: Array<String>): String? {
        return null
    }

    override fun runPart1() {
        printSolution(solvePart1(bufferedInput))
    }

    override fun runPart2() {
        printSolution(solvePart2(bufferedInput))
    }

    private fun printSolution(solution: String?) {
        when (solution) {
            null -> println(Kolor.foreground("No solution found.", Color.RED))
            else -> println("\u001b[1m${Kolor.foreground(solution, Color.YELLOW)}\u001B[0m")
        }
    }
}
