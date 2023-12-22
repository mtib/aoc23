package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import dev.mtib.aoc23.utils.ScadBuilder
import org.koin.core.annotation.Single
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.random.Random

@Single
class Day22 : AbstractDay(22) {
    data class Point3D(val x: Long, val y: Long, val z: Long) {
        companion object {
            fun fromString(s: String): Point3D {
                val (x, y, z) = s.split(",").map { it.toLong() }
                return Point3D(x, y, z)
            }
        }
    }

    data class Brick(val start: Point3D, val end: Point3D) {
        companion object {
            fun fromString(s: String): Brick {
                val (start, end) = s.split("~")
                return Brick(Point3D.fromString(start), Point3D.fromString(end))
            }
        }

        operator fun contains(p: Point3D): Boolean {
            return p.x in start.x..end.x && p.y in start.y..end.y && p.z in start.z..end.z
        }

        fun overlapsInColumn(other: Brick): Boolean {
            val maxStartX = start.x.coerceAtLeast(other.start.x)
            val minEndX = end.x.coerceAtMost(other.end.x)

            val maxStartY = start.y.coerceAtLeast(other.start.y)
            val minEndY = end.y.coerceAtMost(other.end.y)

            return maxStartX <= minEndX && maxStartY <= minEndY
        }

        val grounded: Boolean
            get() = start.z == 1L

        fun floating(other: Collection<Brick>): Boolean {
            for (x in start.x..end.x) {
                for (y in start.y..end.y) {
                    other.any { it.contains(Point3D(x, y, start.z - 1)) }
                    return false
                }
            }
            return true
        }

        fun floatingDistance(other: Collection<Brick>): Long {
            var distance = start.z - 1
            val inColumn = other.filter { it.overlapsInColumn(this) && it.end.z < start.z }
            for (brick in inColumn) {
                distance = distance.coerceAtMost(start.z - brick.end.z - 1)
            }
            return distance
        }

        fun stabilisers(other: Collection<Brick>): Set<Brick> {
            val stabilizing = other.filter { it.overlapsInColumn(this) && it.end.z == start.z - 1 }
            return stabilizing.toSet()
        }
    }

    fun settleBricks(bricks: List<Brick>): List<Brick> {
        val heightMap = bricks.groupBy { it.start.z }
        val settled = mutableListOf<Brick>()
        heightMap.entries.sortedBy { it.key }.forEach {
            it.value.forEach { brick ->
                if (brick.grounded) {
                    settled.add(brick)
                } else {
                    val floatingDistance = brick.floatingDistance(settled)
                    if (floatingDistance > 0) {
                        settled.add(
                            Brick(
                                Point3D(brick.start.x, brick.start.y, brick.start.z - floatingDistance),
                                Point3D(brick.end.x, brick.end.y, brick.end.z - floatingDistance)
                            )
                        )
                    } else {
                        settled.add(brick)
                    }
                }
            }
        }
        return settled
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun Collection<Brick>.toScad(name: String) {
        val builder = ScadBuilder.build {
            hexColor("#ddd") {
                addTranslatedBox(
                    minOf { it.start.x },
                    minOf { it.start.y },
                    0,
                    maxOf { it.end.x } + 1,
                    maxOf { it.end.y } + 1,
                    1,
                )
            }
            forEach { brick ->
                hexColor((Random.nextLong()).toHexString().subSequence(0, 6).toString()) {
                    addTranslatedBox(
                        brick.start.x.toDouble(),
                        brick.start.y.toDouble(),
                        brick.start.z.toDouble(),
                        brick.end.x.toDouble() - brick.start.x.toDouble() + 1,
                        brick.end.y.toDouble() - brick.start.y.toDouble() + 1,
                        brick.end.z.toDouble() - brick.start.z.toDouble() + 1
                    )
                }
            }
        }
        Path("./$name.scad").writeText(builder.toScad())
    }

    override fun solvePart1(input: Array<String>): Any? {
        val bricks = input.map { Brick.fromString(it) }


        debug {
            println(bricks)
            bricks.toScad("22_input")
        }

        val resolved = settleBricks(bricks)

        debug {
            resolved.toScad("22_settled")
        }

        val soloStabilisers = mutableSetOf<Brick>()

        for (brick in resolved) {
            val stabilisers = brick.stabilisers(resolved)
            debug {
                require(stabilisers.isNotEmpty() || brick.start.z == 1L) { "No stabilisers for $brick" }
            }
            if (stabilisers.size == 1) {
                soloStabilisers.add(stabilisers.first())
            }
        }

        return bricks.size - soloStabilisers.size
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun solvePart2(input: Array<String>): Any? {
        val bricks = input.map { Brick.fromString(it) }

        val resolved = settleBricks(bricks)

        val stabilizerMap = resolved.associateWith { brick -> brick.stabilisers(resolved) }

        val chainReaction = resolved.associateWith { brick ->
            val broken = mutableSetOf<Brick>()
            val breaking = mutableSetOf<Brick>(brick)
            while (breaking.isNotEmpty()) {
                broken.addAll(breaking)
                breaking.clear()
                stabilizerMap.entries.filter { it.key !in broken }.filter {
                    it.value.isNotEmpty() && it.value.all { it in broken }
                }.forEach { breaking.add(it.key) }
            }
            broken - brick
        }
        debug {
            for (block in resolved) {
                val scad = ScadBuilder.build {
                    hexColor("ddd") {
                        addTranslatedBox(
                            resolved.minOf { it.start.x },
                            resolved.minOf { it.start.y },
                            0,
                            resolved.maxOf { it.end.x } + 1,
                            resolved.maxOf { it.end.y } + 1,
                            1,
                        )
                    }
                    resolved.forEach { brick ->
                        val chainLength = run {
                            var length = 0
                            val reachable = mutableSetOf<Brick>()
                            val reachableNext = mutableSetOf<Brick>(block)
                            while (reachableNext.isNotEmpty() && brick !in reachable) {
                                reachable.addAll(reachableNext)
                                reachableNext.clear()
                                length += 1
                                stabilizerMap.entries.filter { it.key !in reachable }.filter {
                                    it.value.isNotEmpty() && it.value.all { it in reachable }
                                }.forEach { reachableNext.add(it.key) }
                            }
                            if (brick in reachable) length else -1
                        }
                        hexColor(
                            when {
                                brick == block -> "f00"
                                chainLength >= 0 -> {
                                    (200 - chainLength).toHexString().substring(6, 8).padEnd(6, '0')
                                }

                                chainLength < 0 -> {
                                    (200 + Random.nextInt(56)).toHexString().substring(6, 8).repeat(3)
                                }

                                else -> throw Exception("Should not happen")
                            }
                        ) {
                            addTranslatedBox(
                                brick.start.x.toDouble(),
                                brick.start.y.toDouble(),
                                brick.start.z.toDouble(),
                                brick.end.x.toDouble() - brick.start.x.toDouble() + 1,
                                brick.end.y.toDouble() - brick.start.y.toDouble() + 1,
                                brick.end.z.toDouble() - brick.start.z.toDouble() + 1
                            )
                        }
                    }
                }.toScad()
                Path("./day22_${resolved.indexOf(block).toString().padStart(4, '0')}_supports.scad").writeText(scad)
            }
        }
        return chainReaction.values.sumOf { it.size }
    }
}