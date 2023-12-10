package dev.mtib.aoc23.day

import dev.mtib.aoc23.day.Day10.Position.Companion.DOWN
import dev.mtib.aoc23.day.Day10.Position.Companion.LEFT
import dev.mtib.aoc23.day.Day10.Position.Companion.RIGHT
import dev.mtib.aoc23.day.Day10.Position.Companion.UP
import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single

@Single
class Day10 : AbstractDay(10) {
    abstract class Position(val x: Int, val y: Int) {
        companion object {
            val UP = DifferentialPosition(0, -1)
            val RIGHT = DifferentialPosition(1, 0)
            val DOWN = DifferentialPosition(0, 1)
            val LEFT = DifferentialPosition(-1, 0)
            fun cardinalDirections(): List<DifferentialPosition> {
                return listOf(UP, RIGHT, DOWN, LEFT)
            }
        }

        override fun toString(): String {
            return "($x,$y)"
        }

        override fun equals(other: Any?): Boolean {
            if (other is Position) {
                return other.x == x && other.y == y
            }
            return super.equals(other)
        }

        override fun hashCode(): Int {
            return x + 10000 * y
        }
    }

    class DifferentialPosition(x: Int, y: Int) : Position(x, y) {
        operator fun unaryMinus(): DifferentialPosition {
            return DifferentialPosition(-x, -y)
        }
    }

    class AbsolutePosition(x: Int, y: Int, val input: Array<String>) : Position(x, y) {
        fun symbol(): Char {
            return input[y][x]
        }

        override fun toString(): String {
            return "'${symbol()}'@($x,$y)"
        }

        operator fun plus(other: DifferentialPosition): AbsolutePosition {
            return AbsolutePosition(x + other.x, y + other.y, input)
        }

        operator fun minus(other: DifferentialPosition): AbsolutePosition {
            return AbsolutePosition(x - other.x, y - other.y, input)
        }

        fun potentialNeighborDirections(): List<DifferentialPosition> {
            return when (symbol()) {
                'S' -> cardinalDirections()
                '-' -> listOf(LEFT, RIGHT)
                '|' -> listOf(UP, DOWN)
                'F' -> listOf(DOWN, RIGHT)
                '7' -> listOf(LEFT, DOWN)
                'J' -> listOf(LEFT, UP)
                'L' -> listOf(UP, RIGHT)
                '.' -> emptyList()
                else -> throw IllegalArgumentException("Invalid symbol ${symbol()}")
            }
        }

        fun connectedNeighbors(): List<AbsolutePosition> {
            val potentialDirections = potentialNeighborDirections()
            return potentialDirections.filter {
                try {
                    (this + it).potentialNeighborDirections().contains(-it)
                } catch (e: StringIndexOutOfBoundsException) {
                    false
                } catch (e: ArrayIndexOutOfBoundsException) {
                    false
                }
            }
                .map { this + it }
        }

        fun toSubGridPositions(): Array<SubGridPosition> {
            return arrayOf(
                SubGridPosition(x, y, input),
                SubGridPosition(x + 1, y, input),
                SubGridPosition(x + 1, y + 1, input),
                SubGridPosition(x, y + 1, input),
            )
        }

        fun isEdgeNode(): Boolean {
            return x <= 0 || y <= 0 || x >= input[0].length - 1 || y >= input.size - 1
        }
    }

    class SubGridPosition(x: Int, y: Int, val input: Array<String>) : Position(x, y) {
        fun toAbsolutePosition(): AbsolutePosition {
            if (x < 0 || y < 0 || x >= input[0].length || y >= input.size) {
                throw IllegalArgumentException("Invalid subgrid position ($x,$y)")
            }
            return AbsolutePosition(x, y, input)
        }

        fun toAbsolutePositionOrNull(): AbsolutePosition? {
            return try {
                toAbsolutePosition()
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        fun isEdgeNode(): Boolean {
            return x <= 0 || y <= 0 || x >= input[0].length || y >= input.size
        }

        operator fun plus(other: DifferentialPosition): SubGridPosition {
            return SubGridPosition(x + other.x, y + other.y, input)
        }

        companion object {
            fun fromAbsolutePosition(position: AbsolutePosition): SubGridPosition {
                return SubGridPosition(position.x, position.y, position.input)
            }
        }
    }

    data class FloodFill(
        val distances: Map<AbsolutePosition, Int>,
        val entrances: Map<AbsolutePosition, Set<AbsolutePosition>>,
        val visited: Int
    )

    fun floodFill(input: Array<String>, startPosition: AbsolutePosition): FloodFill {
        val visited = mutableSetOf<AbsolutePosition>()
        val toVisit = mutableListOf(startPosition)
        val distanceMap = mutableMapOf<AbsolutePosition, Int>()
        val enteredFrom = mutableMapOf<AbsolutePosition, MutableSet<AbsolutePosition>>()
        distanceMap[startPosition] = 0
        while (toVisit.isNotEmpty()) {
            val current = toVisit.removeFirst()
            val currentDistance = distanceMap[current]!!

            for (neighbor in current.connectedNeighbors()) {
                distanceMap.compute(neighbor) { _, distance ->
                    if (distance == null || distance > currentDistance + 1) {
                        currentDistance + 1
                    } else {
                        distance
                    }
                }
                enteredFrom.compute(neighbor) { _, list ->
                    if (list == null) {
                        mutableSetOf(current)
                    } else {
                        list.add(current)
                        list
                    }
                }

                if (neighbor !in visited) {
                    toVisit.add(neighbor)
                }
            }
            visited.add(current)
        }

        return FloodFill(
            distanceMap,
            enteredFrom,
            visited.size
        )
    }

    fun enclosedNodes(input: Array<String>, pathNodes: List<AbsolutePosition>): Int {
        val subgridPositions = buildList {
            for (x in -1..input[0].length) {
                for (y in -1..input.size) {
                    add(SubGridPosition(x, y, input))
                }
            }
        }
        val knownEnclosed = mutableSetOf<SubGridPosition>()
        val knownUnenclosed =
            subgridPositions.filter { it.isEdgeNode() && it.toAbsolutePositionOrNull() !in pathNodes }.toMutableSet()

        subgridPositions.forEach {
            if (it in knownEnclosed || it in knownUnenclosed) {
                return@forEach
            }
            val visited = mutableSetOf<SubGridPosition>()
            val toVisit = mutableListOf(it)
            fun MutableList<SubGridPosition>.addIfNotVisited(position: SubGridPosition) {
                if (position !in visited && position !in toVisit) {
                    add(position)
                }
            }
            while (toVisit.isNotEmpty()) {
                val current = toVisit.removeFirst()
                if (current.isEdgeNode()) {
                    visited.add(current)
                    continue
                }
                val bottomRight = current.toAbsolutePositionOrNull()
                val topRight = (current + UP).toAbsolutePositionOrNull()
                val bottomLeft = (current + LEFT).toAbsolutePositionOrNull()
                val topLeft = (current + UP + LEFT).toAbsolutePositionOrNull()

                val closeFullGridCells = listOf(
                    bottomRight,
                    topRight,
                    bottomLeft,
                    topLeft
                )
                closeFullGridCells.forEach {
                    if (it != null && it !in pathNodes) {
                        it.toSubGridPositions().forEach {
                            toVisit.addIfNotVisited(it)
                        }
                    }
                }
                data class BreakCheck(
                    val c1: AbsolutePosition?,
                    val d1: DifferentialPosition,
                    val c2: AbsolutePosition?,
                    val d2: DifferentialPosition,
                    val d: DifferentialPosition,
                )
                listOf(
                    BreakCheck(bottomLeft, RIGHT, bottomRight, LEFT, DOWN),
                    BreakCheck(topLeft, RIGHT, topRight, LEFT, UP),
                    BreakCheck(topRight, DOWN, bottomRight, UP, RIGHT),
                    BreakCheck(topLeft, DOWN, bottomLeft, UP, LEFT),
                ).forEach {
                    if (it.c1 != null && it.c2 != null && it.c1 in pathNodes && it.c2 in pathNodes) {
                        if (!it.c1.potentialNeighborDirections().contains(it.d1) && !it.c2.potentialNeighborDirections()
                                .contains(it.d2)
                        ) {
                            toVisit.addIfNotVisited(current + it.d)
                        }
                    }
                }
                visited.add(current)
            }

            if (visited.any {
                    it.isEdgeNode() || it in knownUnenclosed
                }) {
                knownUnenclosed.addAll(visited)
            } else {
                knownEnclosed.addAll(visited)
            }
        }

        return knownEnclosed.map { it.toAbsolutePosition() }
            .filter { it !in pathNodes }.size
    }

    override fun solvePart1(input: Array<String>): Any? {
        val startPos = input.withIndex().map { (sy, it) ->
            val sx = it.indexOf('S')
            if (sx != -1) {
                AbsolutePosition(sx, sy, input)
            } else {
                null
            }
        }.firstNotNullOf { it }
        val fill = floodFill(input, startPos)
        val pathNodes = fill.distances.filterKeys { fill.entrances[it]!!.size == 2 }
        return pathNodes.values.maxOrNull()
    }

    override fun solvePart2(input: Array<String>): Any? {
        val startPos = input.withIndex().map { (sy, it) ->
            val sx = it.indexOf('S')
            if (sx != -1) {
                AbsolutePosition(sx, sy, input)
            } else {
                null
            }
        }.firstNotNullOf { it }
        val fill = floodFill(input, startPos)
        val pathNodes = fill.distances.filterKeys { fill.entrances[it]!!.size == 2 }
        return enclosedNodes(input, pathNodes.keys.toList())
    }
}

fun Int.asDx(): Day10.DifferentialPosition {
    return Day10.DifferentialPosition(this, 0)
}

fun Int.asDy(): Day10.DifferentialPosition {
    return Day10.DifferentialPosition(0, this)
}

