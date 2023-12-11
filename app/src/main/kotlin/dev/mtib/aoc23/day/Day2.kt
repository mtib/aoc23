package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single
import kotlin.math.max

@Single
class Day2 : AbstractDay(2) {

    data class Game(val number: Int, val turns: List<Turn>) {

    }

    data class Turn(val result: Map<Cube, Int>) {

    }

    enum class Cube {
        Blue,
        Red,
        Green;

        companion object {
            fun fromString(s: String): Cube = when (s[0]) {
                'b' -> Blue
                'r' -> Red
                'g' -> Green
                else -> throw IllegalArgumentException("Invalid cube: $s")
            }
        }
    }

    val gameRegex = Regex("""Game (\d+): (.*)""")
    val turnRegex = Regex("""(\d+) ([a-z]+)""")

    fun parseLine(line: String): Game {
        val gameMatch = gameRegex.matchEntire(line) ?: throw IllegalArgumentException("Invalid line: $line")
        val gameNumber = gameMatch.groupValues[1].toInt()
        val turnStrings = gameMatch.groupValues[2].split(";")
        val cubeMatches = turnStrings.map {
            Turn(buildMap {
                turnRegex.findAll(it).forEach { put(Cube.fromString(it.groupValues[2]), it.groupValues[1].toInt()) }
            })
        }
        return Game(
            gameNumber, cubeMatches
        )
    }

    override fun solvePart1(input: Array<String>): String? {
        val games = input.map { s -> parseLine(s) }

        val sumOfPossibleGamesIds = games.filter {
            it.turns.all { turn ->
                turn.result.all { (cube, count) ->
                    when (cube) {
                        Cube.Red -> count <= 12
                        Cube.Green -> count <= 13
                        Cube.Blue -> count <= 14
                    }
                }
            }
        }.sumOf { it.number }

        return sumOfPossibleGamesIds.toString()
    }

    override fun solvePart2(input: Array<String>): String? {
        val games = input.map { s -> parseLine(s) }

        val result = games.sumOf {
            var acc: Map<Cube, Int>? = buildMap {
                for (cube in Cube.entries) {
                    put(cube, 0)
                }
            }
            for (turn in it.turns) {
                acc = acc?.map { (cube, count) ->
                    cube to max(count, turn.result[cube] ?: 0)
                }?.toMap() ?: turn.result
            }
            acc!!.values.reduce { productAcc, i -> productAcc * i }
        }
        return result.toString()
    }
}
