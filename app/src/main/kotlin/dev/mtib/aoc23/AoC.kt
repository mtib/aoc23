package dev.mtib.aoc23

import dev.mtib.aoc23.day.DayModule
import dev.mtib.aoc23.utils.*
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.error.InstanceCreationException
import org.koin.ksp.generated.module
import kotlin.math.pow
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

fun main(args: Array<String>) {
    startKoin {
        modules(DayModule().module)
    }

    when (val arg = args.getOrNull(0)) {
        null -> runLastDay()
        "all" -> runAllDays()
        "timeall" -> timeAllDays()
        else -> runDay(arg.toInt())
    }
}

private fun runLastDay() {
    val day = GlobalContext.get().getAll<AbstractDay>().maxByOrNull { it.dayNumber } ?: throw ClassNotFoundException()
    runDay(day.dayNumber)
}

private fun timeAllDays() {
    val days = GlobalContext.get().getAll<AbstractDay>().sortedBy { it.dayNumber }
    val timings = days.associateWith { day ->
        println("Timing day ${day.dayNumber}\n")
        val dayTimings = (1..2).associateWith { part ->
            val result = timePart(day, part, day.bufferedInput ?: emptyArray())
            println("Part $part: $result")
            result
        }
        println("")
        dayTimings
    }
    val penalty = 15.seconds.inWholeMicroseconds.toDouble()
    val worstDay = timings.maxByOrNull { (_, dayTimings) ->
        dayTimings.values.sumOf { it?.averageMicroseconds ?: penalty }
    }!!
    val worstPart1 = timings.maxByOrNull { (_, dayTimings) ->
        dayTimings[1]?.averageMicroseconds ?: penalty
    }!!.let { (day, dayTimings) ->
        day to dayTimings[1]!!
    }
    val worstPart2 = timings.maxByOrNull { (_, dayTimings) ->
        dayTimings[2]?.averageMicroseconds ?: penalty
    }!!.let { (day, dayTimings) ->
        day to dayTimings[2]!!
    }
    val totalRuntime = timings.values.sumOf { dayTimings ->
        dayTimings.values.sumOf { it?.averageMicroseconds ?: penalty }
    }
    val bestDay = timings.minByOrNull { (_, dayTimings) ->
        dayTimings.values.sumOf { it?.averageMicroseconds ?: penalty }
    }!!.let { (day, dayTimings) ->
        day to dayTimings.values.sumOf { it?.averageMicroseconds ?: penalty }
    }

    println("Total runtime: ${(totalRuntime / 1000.0).toPrecision(1)}ms")
    println("Best day: ${bestDay.first.dayNumber} (${(bestDay.second).toPrecision(1)}µs)")
    println(
        "Worst day: ${worstDay.key.dayNumber} (${
            (worstDay.value.values.sumOf { it?.averageMicroseconds ?: penalty } / 1000.0.pow(
                1.0
            )).toPrecision(1)
        }ms)")
    println(
        "Worst part 1: ${worstPart1.first.dayNumber} (${
            (worstPart1.second.averageMicroseconds / 1000.0).toPrecision(
                1
            )
        }ms)"
    )
    println(
        "Worst part 2: ${worstPart2.first.dayNumber} (${
            (worstPart2.second.averageMicroseconds / 1000.0).toPrecision(
                1
            )
        }ms)"
    )
}

private fun runAllDays() {
    val days = GlobalContext.get().getAll<AbstractDay>().sortedBy { it.dayNumber }
    for (day in days) {
        runDay(day.dayNumber)
    }
}

private fun runDay(dayNumber: Int) {
    println("Running day $dayNumber\n")

    val day = try {
        GlobalContext.get().getAll<AbstractDay>().find { it.dayNumber == dayNumber } ?: throw ClassNotFoundException()
    } catch (e: InstanceCreationException) {
        val cause = e.cause
        println("\u001b[31mFailed to instantiation runner for day $dayNumber: ${cause?.toString() ?: e.toString()}\u001b[0m")
        exitProcess(1)
    } catch (e: ClassNotFoundException) {
        println("\u001b[31mNo solution found for day $dayNumber\u001b[0m")
        exitProcess(2)
    }

    for (part in 1..2) {
        runPart(day, part)
        Knowledge.KnowledgeFile.createDay(day.yearNumber, day.dayNumber, part.toLong())
        val runtime = timePart(day, part, day.bufferedInput ?: emptyArray())
        println(runtime)
        println("")
    }

    if (day is MiscRunner) {
        println("\u001b[1mMisc:\u001b[0m")
        day.misc(day.bufferedInput ?: emptyArray())
    }
}

private fun runPart(day: DayRunner, part: Int) {
    println("\u001b[1mPart $part:\u001b[0m")
    // time runtime
    when (part) {
        1 -> day.runPart1()
        2 -> day.runPart2()
        else -> throw IllegalArgumentException("Part $part is not a valid part.")
    }
}

val MIN_TIME_SPENT = 500.milliseconds
const val MIN_RUNS = 20
const val MAX_RUNS = 1000

data class RunResult(val averageMicroseconds: Double, val standardDeviationMicroseconds: Double, val runs: Int) {
    fun getAverageString(): String {
        return if (averageMicroseconds < 1000) {
            "${(averageMicroseconds).toPrecision(1)}µs"
        } else {
            "${(averageMicroseconds / 1000.0).toPrecision(1)}ms"
        }
    }

    fun getStandardDeviationString(): String {
        return if (standardDeviationMicroseconds < 1000) {
            "${(standardDeviationMicroseconds).toPrecision(2)}µs"
        } else {
            "${(standardDeviationMicroseconds / 1000.0).toPrecision(1)}ms"
        }
    }

    override fun toString(): String {
        return "\u001b[36mRuntime: ${getAverageString()}, σ: ${
            getStandardDeviationString()
        } (${runs} runs)\u001b[0m"
    }
}

private fun timePart(day: DaySolver, part: Int, input: Array<String>): RunResult? {
    try {
        buildList<Duration> {
            while (size < MIN_RUNS || reduce { acc, it -> acc + it } < MIN_TIME_SPENT) {
                add(measureTime {
                    when (part) {
                        1 -> day.solvePart1(input)
                        2 -> day.solvePart2(input)
                        else -> throw IllegalArgumentException("Part $part is not a valid part.")
                    }
                })
                if (size >= MAX_RUNS) break
            }
        }.let { durations ->
            val average = durations.sumOf { it.inWholeMicroseconds }.toDouble() / durations.size
            val standardDeviation =
                kotlin.math.sqrt(durations.sumOf { (it.inWholeMicroseconds - average).pow(2) } / durations.size)
            return RunResult(average, standardDeviation, durations.size)
        }
    } catch (e: Exception) {
        println("Error while timing part $part: ${e.javaClass.simpleName} ${e.message}")
    }
    return null
}

private fun Double.toPrecision(precision: Int): Double = (this * 10.0.pow(precision)).toInt() / 10.0.pow(precision)
