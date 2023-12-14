package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single

@Single
class Day14 : AbstractDay(14) {

    private fun computeLoad(input: Array<String>): Int {
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

    override fun solvePart1(input: Array<String>): Int {
        return computeLoad(input)
    }

    private val cycles = 1000000000L

    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private fun tilt(input: Array<Array<Char>>, direction: Direction): Array<Array<Char>> {
        val nextMap = Array(input.size) { Array(input[0].size) { '?' } }
        input.withIndex().forEach { (row, line) ->
            line.withIndex().forEach charHandler@{ (column, c) ->
                when (c) {
                    '.' -> {
                        if (nextMap[row][column] == '?') {
                            nextMap[row][column] = '.'
                        }
                    }

                    'O' -> {
                        class Position(val row: Int, val column: Int, val char: Char)

                        val next = sequence<Position> {
                            var dist = 1
                            while (true) {
                                val nextRow = when (direction) {
                                    Direction.UP -> row - dist
                                    Direction.DOWN -> row + dist
                                    else -> row
                                }
                                val nextColumn = when (direction) {
                                    Direction.LEFT -> column - dist
                                    Direction.RIGHT -> column + dist
                                    else -> column
                                }
                                if (nextRow < 0 || nextRow >= input.size || nextColumn < 0 || nextColumn >= input[0].size) {
                                    break
                                }
                                val oldNextChar = input[nextRow][nextColumn]
                                if (oldNextChar == '#') {
                                    break
                                }
                                val updatedNextChar = nextMap[nextRow][nextColumn]
                                val nextChar = if (updatedNextChar == '?') {
                                    oldNextChar
                                } else {
                                    updatedNextChar
                                }
                                yield(Position(nextRow, nextColumn, nextChar))
                                dist++
                            }
                        }
                        val moveTo = next.findLast { it.char == '.' }
                        if (moveTo != null) {
                            nextMap[moveTo.row][moveTo.column] = 'O'
                            nextMap[row][column] = '.'
                        } else {
                            nextMap[row][column] = 'O'
                        }
                    }

                    '#' -> {
                        nextMap[row][column] = '#'
                    }
                }
            }
        }
        return nextMap
    }

    fun Array<Array<Char>>.hash(): String {
        return this.joinToString("") { it.joinToString("") }
    }

    override fun solvePart2(input: Array<String>): Any? {

        fun printMap(map: Array<Array<Char>>) {
            println("+" + "-".repeat(map[0].size) + "+")
            map.forEach { println("|${it.joinToString("")}|") }
            println("+" + "-".repeat(map[0].size) + "+")
        }

        debug {
            val example = listOf(
                "#.O",
                ".#.",
                "...",
                "OOO"
            ).map { it.toCharArray().toTypedArray() }.toTypedArray()

            Direction.entries.forEach {
                require(
                    tilt(example, it).contentDeepEquals(
                        tilt(
                            tilt(example, it),
                            it
                        )
                    )
                ) { "Tilting twice in the same direction should be the same as tilting once. (broke for $it)" }
                require(
                    tilt(example, it).sumOf { it.count { it == 'O' } } == example.sumOf { it.count { it == 'O' } }
                ) { "Tilting should not change the number of rolling stones. (broke for $it)" }
            }

            printMap(example)
            printMap(tilt(example, Direction.UP))
            printMap(tilt(tilt(example, Direction.UP), Direction.DOWN))
        }

        var map = input.map { it.toCharArray().toTypedArray() }.toTypedArray()
        debug {
            printMap(map)
        }
        val seenMaps = mutableListOf<String>(map.hash())
        for (it in 1..cycles) {
            map = tilt(map, Direction.UP)
            map = tilt(map, Direction.LEFT)
            map = tilt(map, Direction.DOWN)
            map = tilt(map, Direction.RIGHT)
            if (map.hash() in seenMaps) {
                val cycleLength = it - seenMaps.lastIndexOf(map.hash())
                debug {
                    println("Found cycle at index $it (size ${cycleLength})")
                }
                val remainingCycles = (cycles - it) % cycleLength
                for (i in 1..remainingCycles) {
                    map = tilt(map, Direction.UP)
                    map = tilt(map, Direction.LEFT)
                    map = tilt(map, Direction.DOWN)
                    map = tilt(map, Direction.RIGHT)
                }
                return computeLoad(map.map { it.joinToString("") }.toTypedArray())
            }
            seenMaps.add(map.hash())
        }
        return null
    }
}