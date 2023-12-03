package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single

@Single
class Day3 : AbstractDay(3) {


    private fun isAdjacentToSymbol(input: Array<String>, line: Int, start: Int): Boolean {
        val length = run {
            var i = 0
            while (i + start < input[line].length && input[line][i + start].isDigit()) {
                i++
            }
            i
        }
        return isAdjacentTo(input, line, start, length) { !(isDigit() || this == '.') }
    }

    private fun isAdjacentTo(
        input: Array<String>,
        line: Int,
        start: Int,
        length: Int,
        symbolCheck: Char.() -> Boolean
    ): Boolean {
        for (yOffset in -1..1) {
            for (xOffset in -1..length) {
                val x = start + xOffset
                val y = line + yOffset
                if (y < 0 || y >= input.size || x < 0 || x >= input[y].length) {
                    continue
                }
                if (!input[y][x].symbolCheck()) {
                    continue
                }
                return true
            }
        }
        return false
    }

    override fun solvePart1(input: Array<String>): String? {
        val numRegex = Regex("""(\d+)""")
        val sum = input.flatMapIndexed { lineIndex, line ->
            numRegex.findAll(line).map {
                object {
                    val number = it.groupValues[1].toInt()
                    val x = it.range.first
                    val y = lineIndex
                }
            }
        }.filter { isAdjacentToSymbol(input, line = it.y, start = it.x) }.sumOf { it.number }
        return sum.toString()
    }

    override fun solvePart2(input: Array<String>): String? {
        data class NumberMatch(
            val number: Int,
            val startIndex: Int,
            val lineIndex: Int,
            val length: Int
        )

        val numbers = input.flatMapIndexed { lineIndex, line ->
            Regex("""(\d+)""").findAll(line).map {
                NumberMatch(
                    it.groupValues[1].toInt(),
                    it.range.first,
                    lineIndex,
                    it.value.length
                )
            }
        }
        val gearSums = input.flatMapIndexed { lineIndex, line ->
            Regex("""\*""").findAll(line).map {
                object {
                    val x = it.range.first
                    val y = lineIndex
                }
            }
        }.mapNotNull {
            val adjacentNumbers = mutableSetOf<NumberMatch>()
            for (yOffset in -1..1) {
                for (xOffset in -1..1) {
                    val x = it.x + xOffset
                    val y = it.y + yOffset
                    if (y < 0 || y >= input.size || x < 0 || x >= input[y].length) {
                        continue
                    }
                    val number =
                        numbers.find { it.lineIndex == y && it.startIndex <= x && it.startIndex + it.length > x }
                            ?: continue
                    adjacentNumbers.add(number)
                }
            }
            when (adjacentNumbers.size) {
                2 -> adjacentNumbers.map { it.number }.reduce { acc, num -> acc * num }
                else -> null
            }
        }.sum()

        return gearSums.toString()
    }
}
