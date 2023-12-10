package dev.mtib.aoc23.day

import dev.mtib.aoc23.day.Day10.Position.Companion.DOWN
import dev.mtib.aoc23.day.Day10.Position.Companion.LEFT
import dev.mtib.aoc23.day.Day10.Position.Companion.RIGHT
import dev.mtib.aoc23.day.Day10.Position.Companion.UP
import dev.mtib.aoc23.utils.AbstractDay
import dev.mtib.aoc23.utils.MiscRunner
import org.koin.core.annotation.Single

@Single
class Day10 : AbstractDay(10), MiscRunner {
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
        /**
         * i.e. bottom right
         */
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

        fun getAdjacentAbsolutePositions(): List<AbsolutePosition> {
            return listOf(
                AbsolutePosition(x, y, input),
                AbsolutePosition(x - 1, y, input),
                AbsolutePosition(x - 1, y - 1, input),
                AbsolutePosition(x, y - 1, input),
            ).filter { it.y >= 0 && it.x >= 0 && it.y < input.size && it.x < input[0].length }
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

    data class Enclosed(
        val enclosed: Set<AbsolutePosition>,
        val unenclosed: Set<AbsolutePosition>,
    )

    fun enclosedNodes(input: Array<String>, pathNodes: List<AbsolutePosition>): Enclosed {
        data class BreakCheck(
            val c1: AbsolutePosition?,
            val d1: DifferentialPosition,
            val c2: AbsolutePosition?,
            val d2: DifferentialPosition,
            val d: DifferentialPosition,
        )

        val pathNodesSet = pathNodes.toSet()
        val subgridPositions = buildList {
            for (x in 1..input[0].length step 2) {
                for (y in 1..input.size step 2) {
                    val consider = SubGridPosition(x, y, input)
                    if (consider.getAdjacentAbsolutePositions().all { it !in pathNodesSet }) {
                        add(SubGridPosition(x, y, input))
                    }
                }
            }
        }
        val knownEnclosed = mutableSetOf<SubGridPosition>()
        val knownUnenclosed =
            subgridPositions.filter { it.isEdgeNode() }.toMutableSet()

        subgridPositions.forEach {
            if (it in knownEnclosed || it in knownUnenclosed) {
                return@forEach
            }
            val visited = mutableSetOf<SubGridPosition>()
            val toVisit = mutableSetOf(it)
            while (toVisit.isNotEmpty()) {
                val current = toVisit.random()
                if (current.isEdgeNode() || current in knownEnclosed || current in knownEnclosed) {
                    visited.add(current)
                    break
                }
                toVisit.remove(current)
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
                            if (it !in visited && it != current) {
                                toVisit.add(it)
                            }
                        }
                    }
                }
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
                            val squeeze = current + it.d
                            if (squeeze !in visited) {
                                toVisit.add(squeeze)
                            }
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

        return Enclosed(
            enclosed = knownEnclosed.map { it.toAbsolutePosition() }
                .filter { it !in pathNodes }.toSet(),
            unenclosed = knownUnenclosed.map { it.toAbsolutePositionOrNull() }.filterNotNull().toSet(),
        )
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
        return enclosedNodes(input, pathNodes.keys.toList()).enclosed.size
    }

    fun prettyPrint(
        input: Array<String>,
        path: Collection<AbsolutePosition> = emptyList(),
        part1Solution: AbsolutePosition? = null,
        part2Solution: Set<AbsolutePosition> = emptySet(),
    ) {
        // box drawing characters
        val replacements = mapOf(
            'S' to "S",
            '7' to "┐",
            'F' to "┌",
            'J' to "┘",
            'L' to "└",
            '|' to "│",
            '-' to "─",
            '.' to " ",
        )
        input.forEachIndexed { lineNum, line ->
            println(
                buildString {
                    append(
                        replacements.entries.fold(line) { acc, entry -> acc.replace(entry.key.toString(), entry.value) }
                            .mapIndexed { charNum, char ->
                                val position = AbsolutePosition(charNum, lineNum, input)
                                when {
                                    char == 'S' -> "\u001b[1;31mS\u001b[0m"
                                    position == part1Solution -> "\u001b[1;31m$char\u001b[0m"
                                    position in part2Solution -> "\u001b[41m$char\u001b[0m"
                                    position in path -> "\u001b[1;33m$char\u001b[0m"
                                    else -> char.toString()
                                }
                            }.joinToString("")
                    )
                    val poi = mutableListOf<String>()
                    if (line.contains('S')) {
                        poi.add("has start")
                    }
                    if (part1Solution?.y == lineNum) {
                        poi.add("part 1 solution")
                    }
                    if (part2Solution.any { it.y == lineNum }) {
                        poi.add("part 2 (${part2Solution.count { it.y == lineNum }} enclosed)")
                    }
                    if (poi.isNotEmpty()) {
                        append("  ")
                        append(poi.joinToString(", "))
                    }
                }
            )
        }
    }

    override fun misc(input: Array<String>) {
        val startPos = input.withIndex().map { (sy, it) ->
            val sx = it.indexOf('S')
            if (sx != -1) {
                AbsolutePosition(sx, sy, input)
            } else {
                null
            }
        }.firstNotNullOf { it }
        val fill = floodFill(input, startPos)
        val pathNodes = fill.distances.filterKeys { fill.entrances[it]!!.size == 2 }.keys.toList()
        val maxNode = fill.distances.filterKeys { fill.entrances[it]!!.size == 2 }.maxBy { it.value }.key
        val enclosed = enclosedNodes(input, pathNodes)
        prettyPrint(input, pathNodes, maxNode, enclosed.enclosed)
    }
}

