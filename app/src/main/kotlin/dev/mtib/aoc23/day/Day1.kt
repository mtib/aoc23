package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single

@Single
class Day1 : AbstractDay(1) {

    override fun solvePart1(input: Array<String>): String? {
        return input.sumOf {
            it.find { it.isDigit() }!!.digitToInt() * 10 + it.findLast { it.isDigit() }!!.digitToInt()
        }.toString()
    }

    override fun solvePart2(input: Array<String>): String? {
        val spelledDigits = listOf(
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine"
        )

        val regexp = Regex("(${spelledDigits.joinToString("|")}|[0-9])")
        val reversedRegexp = Regex("(${spelledDigits.joinToString("|") { it.reversed() }}|[0-9])")

        fun digitToValue (digit: String): Int {
            return when {
                digit in spelledDigits -> spelledDigits.indexOf(digit) + 1
                digit.length == 1 -> digit[0].digitToInt()
                else -> throw IllegalArgumentException("Invalid digit $digit")
            }
        }

        return input.sumOf { digitToValue(regexp.find(it)!!.groupValues[1]) * 10 + digitToValue(reversedRegexp.find(it.reversed())!!.groupValues[1].reversed()) }
            .toString()
    }
}
