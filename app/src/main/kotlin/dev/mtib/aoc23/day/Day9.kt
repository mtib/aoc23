package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single

@Single
class Day9 : AbstractDay(9) {

    fun extrapolateForwards(report: Array<Long>): Long {
        val sequences = buildList<Array<Long>> {
            add(report)

            while (last().any { it != 0L }) {
                val lastSequence = last()
                add(buildList<Long> {
                    for (i in 0 until lastSequence.size - 1) {
                        add(lastSequence[i + 1] - lastSequence[i])
                    }
                }.toTypedArray())
            }
        }
        val lastDiagonal = buildList<Long> {
            add(0L)
            for (i in 1 until sequences.size) {
                add(sequences[sequences.size - i - 1].last() + last())
            }
        }
        return lastDiagonal.last()
    }

    fun extrapolateBackwards(report: Array<Long>): Long {
        val sequences = buildList<Array<Long>> {
            add(report)

            while (last().any { it != 0L }) {
                val lastSequence = last()
                add(buildList<Long> {
                    for (i in 0 until lastSequence.size - 1) {
                        add(lastSequence[i + 1] - lastSequence[i])
                    }
                }.toTypedArray())
            }
        }
        val firstDiagonal = buildList<Long> {
            add(0L)
            for (i in 1 until sequences.size) {
                add(sequences[sequences.size - i - 1].first() - last())
            }
        }
        return firstDiagonal.last()
    }

    override fun solvePart1(input: Array<String>): Long {
        val oasisReports = input.filter { it.isNotBlank() }.map { it.split(" ").map { it.toLong() }.toTypedArray() }
        return oasisReports.sumOf { extrapolateForwards(it) }
    }

    override fun solvePart2(input: Array<String>): Long {
        val oasisReports = input.filter { it.isNotBlank() }.map { it.split(" ").map { it.toLong() }.toTypedArray() }
        return oasisReports.sumOf { extrapolateBackwards(it) }
    }
}