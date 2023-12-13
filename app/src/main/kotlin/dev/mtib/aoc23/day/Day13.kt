package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single
import kotlin.math.max

@Single
class Day13 : AbstractDay(13) {

    fun iterFindMirror(input: Array<String>) {

    }

    abstract class Mirror(val position: Int) {
        abstract fun value(): Int
    }

    data class HorizontalMirror(val x1: Int) : Mirror(x1) {
        override fun value(): Int {
            return x1 + 1
        }
    }

    data class VerticalMirror(val y1: Int) : Mirror(y1) {
        override fun value(): Int {
            return 100 * (y1 + 1)
        }
    }

    class Room(val data: List<String>) {
        override fun toString(): String {
            return buildString {
                appendLine("Room")
                data.forEach {
                    appendLine(it)
                }
            }
        }

        fun printWithMirror(numSmudges: Int = 0) {
            val mirror = findMirror(numSmudges)
            if (mirror is HorizontalMirror) {
                data.forEach { line ->
                    println(buildString {
                        append(line.subSequence(0..mirror.x1))
                        append('|')
                        append(line.subSequence(mirror.x1 + 1..line.length - 1))
                    })
                }
            } else if (mirror is VerticalMirror) {
                data.forEachIndexed { index, s ->
                    println(s)
                    if (index == mirror.y1) {
                        println("-".repeat(s.length))
                    }
                }
            }
        }

        fun findMirror(numSmudges: Int = 0): Mirror {
            val horizontal = 0
            val vertical = 1

            val maxLength = max(data.size, data[0].length)
            directionCheck@ for (direction in listOf(horizontal, vertical)) {
                val directionLength = when (direction) {
                    horizontal -> data[0].length
                    vertical -> data.size
                    else -> throw IllegalStateException("Unknown direction")
                }
                val crossDirectionLength = when (direction) {
                    horizontal -> data.size
                    vertical -> data[0].length
                    else -> throw IllegalStateException("Unknown direction")
                }
                mirrorPosition@ for (j in 0..<directionLength - 1) {
                    var smudges = 0
                    reflectionCheck@ for (i in 1..maxLength) {
                        when (direction) {
                            horizontal -> {
                                for (k in 0..<crossDirectionLength) {
                                    val left = data.getOrNull(k)?.getOrNull(j - (i - 1))
                                    val right = data.getOrNull(k)?.getOrNull(j + i)
                                    if (left == null || right == null) {
                                        break
                                    }
                                    if (left == right) {
                                        continue
                                    }
                                    smudges++
                                    if (smudges > numSmudges) {
                                        continue@mirrorPosition
                                    }
                                }
                            }

                            vertical -> {
                                for (k in 0..<crossDirectionLength) {
                                    val top = data.getOrNull(j - (i - 1))?.getOrNull(k)
                                    val bottom = data.getOrNull(j + i)?.getOrNull(k)
                                    if (top == null || bottom == null) {
                                        break
                                    }
                                    if (top == bottom) {
                                        continue
                                    }
                                    smudges++
                                    if (smudges > numSmudges) {
                                        continue@mirrorPosition
                                    }
                                }
                            }
                        }
                    }
                    if (smudges == numSmudges) {
                        return when (direction) {
                            horizontal -> HorizontalMirror(j)
                            vertical -> VerticalMirror(j)
                            else -> throw IllegalStateException("Unknown direction")
                        }
                    }
                }
            }
            throw IllegalStateException("No mirror found")
        }

        companion object {
            fun fromList(input: Array<String>): List<Room> {
                return buildList {
                    var lastStart = 0
                    input.forEachIndexed { index, s ->
                        if (s == "") {
                            add(Room(input.slice(lastStart until index)))
                            lastStart = index + 1
                        }
                    }
                    if (lastStart < input.size) {
                        add(Room(input.slice(lastStart until input.size)))
                    }
                }
            }
        }
    }

    override fun solvePart1(input: Array<String>): Any? {
        val rooms = Room.fromList(input)
        debug {
            rooms.forEach {
                it.printWithMirror()
            }
        }
        return rooms.sumOf { it.findMirror().value() }
    }

    override fun solvePart2(input: Array<String>): Any? {
        val rooms = Room.fromList(input)
        debug {
            rooms.forEach {
                it.printWithMirror(1)
            }
        }
        return rooms.sumOf { it.findMirror(1).value() }
    }
}