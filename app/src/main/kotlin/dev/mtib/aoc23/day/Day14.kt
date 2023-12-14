package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single

@Single
class Day14 : AbstractDay(14) {
    override fun solvePart1(input: Array<String>): Any? {
        return input.withIndex().sumOf { (row, line) ->
            line.withIndex().sumOf { (column, c) ->
                val above = input.getOrNull(row - 1)?.getOrNull(column)
                if (above == '.' || above == 'O' || c == '#') {
                    0
                } else {
                    val rollingStones =
                        input.slice(row..<input.size).map { it[column] }.takeWhile { it != '#' }.count() { it == 'O' }
                    val startVal = input.size - row
                    val endVal = startVal - rollingStones + 1
                    rollingStones * (startVal + endVal) / 2
                }
            }
        }
    }

    override fun solvePart2(input: Array<String>): Any? {
        return super.solvePart2(input)
    }
}