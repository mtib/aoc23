package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single

@Single
class Day12 : AbstractDay(12) {

    companion object {
        fun String.asStates(): List<Row.State> {
            return this.map {
                when (it) {
                    '#' -> Row.State.Damaged
                    '.' -> Row.State.Operational
                    '?' -> Row.State.Unknown
                    else -> throw IllegalArgumentException("Unknown state $it")
                }
            }
        }
    }

    class Row(val stateList: List<State>, val damageGroups: List<Int>) {
        enum class State {
            Damaged, Unknown, Operational;

            override fun toString(): String {
                return when (this) {
                    Damaged -> "#"
                    Unknown -> "?"
                    Operational -> "."
                }
            }
        }

        override fun toString(): String {
            return stateList.joinToString("") + " " + damageGroups.joinToString(",")
        }

        /**
         * Optimisation ideas:
         * - use sublist
         * - lookup tails
         */
        fun countGroups(
            states: List<State> = stateList,
            groups: List<Int> = damageGroups,
        ): Long {
            if (groups.isEmpty()) {
                if (states.all { it != State.Damaged }) {
                    return 1L
                }
                return 0
            }
            if (states.isEmpty()) {
                return 0
            }
            return when (states[0]) {
                State.Operational -> countGroups(states.drop(1), groups)
                State.Unknown -> {
                    val nextGroup = groups.first()
                    val nextStates = states.take(nextGroup)
                    val followingState = states.getOrNull(nextGroup)
                    if (nextStates.all { it != State.Operational } && nextStates.size == nextGroup) {
                        val applyNow = if (followingState != State.Damaged) {
                            countGroups(
                                states.drop(nextGroup + if (followingState != null) 1 else 0),
                                groups.drop(1),
                            )
                        } else {
                            0L
                        }
                        val applyLater = countGroups(
                            states.drop(1),
                            groups,
                        )
                        return applyNow + applyLater
                    }
                    return countGroups(
                        states.drop(1),
                        groups,
                    )
                }

                State.Damaged -> {
                    val nextGroup = groups.first()
                    val nextStates = states.take(nextGroup)
                    val followingState = states.getOrNull(nextGroup)
                    if (nextStates.all { it != State.Operational } && followingState != State.Damaged && nextStates.size == nextGroup) {
                        return countGroups(
                            states.drop(nextGroup + if (followingState != null) 1 else 0),
                            groups.drop(1),
                        )
                    }
                    return 0L
                }
            }
        }

        fun iterateGroups(
            states: List<State> = stateList,
            groups: List<Int> = damageGroups,
            prefix: List<State> = emptyList()
        ): List<List<State>> {
            require(states.size + prefix.size == stateList.size) {
                "Expected ${stateList.size} (${
                    stateList.joinToString(
                        ""
                    )
                }) states, got ${states.size + prefix.size} (${states.joinToString("")} and ${prefix.joinToString("")})"
            }
            if (groups.isEmpty()) {
                if (states.all { it != State.Damaged }) {
                    return listOf(prefix + (1..states.size).map { State.Operational })
                }
                return emptyList()
            }
            if (states.isEmpty()) {
                return emptyList()
            }
            return when (states[0]) {
                State.Operational -> iterateGroups(states.drop(1), groups, prefix + State.Operational)
                State.Unknown -> {
                    val nextGroup = groups.first()
                    val nextStates = states.take(nextGroup)
                    val followingState = states.getOrNull(nextGroup)
                    if (nextStates.all { it != State.Operational } && nextStates.size == nextGroup) {
                        val applyNowPrefix = buildList {
                            addAll(prefix)
                            addAll((1..nextGroup).map { State.Damaged })
                            if (followingState != null) {
                                add(State.Operational)
                            }
                        }
                        val applyNow = if (followingState != State.Damaged) {
                            iterateGroups(
                                states.drop(nextGroup + if (followingState != null) 1 else 0),
                                groups.drop(1),
                                applyNowPrefix
                            )
                        } else {
                            emptyList()
                        }
                        val applyLater = iterateGroups(
                            states.drop(1),
                            groups,
                            prefix + State.Operational
                        )
                        return applyNow + applyLater
                    }
                    return iterateGroups(
                        states.drop(1),
                        groups,
                        prefix + State.Operational
                    )
                }

                State.Damaged -> {
                    val nextGroup = groups.first()
                    val nextStates = states.take(nextGroup)
                    val followingState = states.getOrNull(nextGroup)
                    if (nextStates.all { it != State.Operational } && followingState != State.Damaged && nextStates.size == nextGroup) {
                        val newPrefix = buildList {
                            addAll(prefix)
                            addAll((1..nextGroup).map { State.Damaged })
                            if (followingState != null) {
                                add(State.Operational)
                            }
                        }
                        return iterateGroups(
                            states.drop(nextGroup + if (followingState != null) 1 else 0),
                            groups.drop(1),
                            newPrefix
                        )
                    }
                    return emptyList()
                }
            }
        }

        companion object {
            operator fun invoke(input: String): Row {
                val (statesString, groupString) = input.split(" ")
                val states = statesString.asStates()
                val groups = groupString.split(",").map { it.toInt() }
                return Row(states, groups)
            }
        }
    }

    override fun solvePart1(input: Array<String>): Any? {
        val rows = input.map { Row(it) }

        debug {
            rows.forEach {
                println(
                    "$it -> ${it.iterateGroups().size} ${
                        it.iterateGroups().map { it.joinToString("") }
                    }"
                )
            }
        }

        debug {
            data class TestCase(val row: Row, val expected: List<List<Row.State>>)
            listOf(
                TestCase(
                    Row("#???#???#???.#?? 5,3,1,1,1"),
                    listOf(
                        "#####.###.#..#.#".asStates(),
                        "#####.###..#.#.#".asStates(),
                        "#####..###.#.#.#".asStates()
                    )
                ),
                TestCase(
                    Row("????.####.? 1,4"),
                    listOf(
                        "#....####..".asStates(),
                        ".#...####..".asStates(),
                        "..#..####..".asStates(),
                        "...#.####..".asStates()
                    )
                ),
                TestCase(
                    Row("?###???????? 3,2,1"),
                    listOf(
                        ".###.##.#...",
                        ".###.##..#..",
                        ".###.##...#.",
                        ".###.##....#",
                        ".###..##.#..",
                        ".###..##..#.",
                        ".###..##...#",
                        ".###...##.#.",
                        ".###...##..#",
                        ".###....##.#"
                    ).map { it.asStates() }
                ),
                TestCase(
                    Row("...?#?????.?? 5,1"),
                    listOf(
                        "...#####...#.",
                        "....#####..#.",
                        "...#####....#",
                        "....#####...#",
                        "...#####.#...",
                    ).map { it.asStates() }
                )
            ).forEach {
                val groups = it.row.iterateGroups()
                require(groups.size == it.expected.size) { "${it.row} | Expected ${it.expected.size} groups, got ${groups.size}" }
                it.expected.forEach { expectedGroup ->
                    require(groups.contains(expectedGroup)) { "${it.row} | Expected $expectedGroup in $groups" }
                }
                groups.forEach { group ->
                    require(it.expected.contains(group)) { "${it.row} | Expected $group in ${it.expected}" }
                }
                groups.forEach { resultStates ->
                    require(resultStates.size == it.row.stateList.size) { "${it.row} | Expected ${it.row.stateList.size} states, got ${resultStates.size}" }
                }
                groups.forEach { resultStates ->
                    it.row.stateList.forEachIndexed { index, state ->
                        if (resultStates[index] == Row.State.Damaged) {
                            require(
                                state in listOf(
                                    Row.State.Damaged,
                                    Row.State.Unknown
                                )
                            ) { "${it.row} | Expected $state to be Damaged or Unknown" }
                        }
                        if (resultStates[index] == Row.State.Operational) {
                            require(
                                state in listOf(
                                    Row.State.Operational,
                                    Row.State.Unknown
                                )
                            ) { "${it.row} | Expected $state to be Operational or Unknown" }
                        }
                    }
                }
                groups.forEach { resultStates ->
                    val desc = buildList<Int> {
                        var result = resultStates
                        while (result.isNotEmpty()) {
                            result = result.dropWhile { it == Row.State.Operational }
                            val range = result.takeWhile { it == Row.State.Damaged }.size
                            if (range != 0) {
                                add(range)
                            }
                            result = result.drop(range)
                        }
                    }
                    require(desc == it.row.damageGroups) { "${it.row} | Expected ${it.row.damageGroups}, got $desc" }
                }
                it.row.stateList
            }
        }

        debug {
            rows.forEach { row ->
                val groups = row.iterateGroups()

                require(groups.size.toLong() == row.countGroups()) { "${row} | Expected ${row.countGroups()} groups, got ${groups.size}" }

                groups.forEach { resultStates ->
                    require(resultStates.size == row.stateList.size) { "${row} | Expected ${row.stateList.size} states, got ${resultStates.size}" }
                }
                groups.forEach { resultStates ->
                    row.stateList.forEachIndexed { index, state ->
                        if (resultStates[index] == Row.State.Damaged) {
                            require(
                                state in listOf(
                                    Row.State.Damaged,
                                    Row.State.Unknown
                                )
                            ) { "${row} | Expected $state to be Damaged or Unknown" }
                        }
                        if (resultStates[index] == Row.State.Operational) {
                            require(
                                state in listOf(
                                    Row.State.Operational,
                                    Row.State.Unknown
                                )
                            ) { "${row} | Expected $state to be Operational or Unknown" }
                        }
                    }
                }
                groups.forEach { resultStates ->
                    val desc = buildList<Int> {
                        var result = resultStates
                        while (result.isNotEmpty()) {
                            result = result.dropWhile { it == Row.State.Operational }
                            val range = result.takeWhile { it == Row.State.Damaged }.size
                            if (range != 0) {
                                add(range)
                            }
                            result = result.drop(range)
                        }
                    }
                    require(desc == row.damageGroups) { "${row} | Expected ${row.damageGroups}, got $desc" }
                }
            }
        }


        return rows.sumOf { it.iterateGroups().size }
    }

    override fun solvePart2(input: Array<String>): Any? {
        val rows = input.map {
            val (states, report) = it.split(" ")
            Row((1..5).joinToString("?") { states } + " " + (1..5).joinToString(",") { report })
        }
        var finished = 0
        return runBlocking(Dispatchers.Default) {
            rows.map { row ->
                async {
                    row.countGroups().also {
                        finished++
                        println("$finished / ${rows.size} (row=$row value=${it})")
                    }
                }
            }.awaitAll().sum()
        }
    }
}