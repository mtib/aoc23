/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package dev.mtib.aoc23

import dev.mtib.aoc23.day.DayModule
import dev.mtib.aoc23.utils.AbstractDay
import dev.mtib.aoc23.utils.DayRunner
import org.koin.core.context.startKoin
import org.koin.core.error.InstanceCreationException
import org.koin.ksp.generated.module
import java.lang.reflect.InvocationTargetException
import java.nio.file.NoSuchFileException
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val koin = startKoin {
        modules(DayModule().module)
    }.koin

    val dayNumber = args.getOrNull(0)?.toInt() ?: 1
    println("Running day $dayNumber\n")

    val day = try {
        koin.getAll<AbstractDay>().find { it.dayNumber == dayNumber } ?: throw ClassNotFoundException()
    } catch (e: InstanceCreationException) {
        val cause = e.cause
        if (cause is NoSuchFileException) {
            println("\u001b[31mNo input file found for day $dayNumber: ${cause.message}\u001b[0m")
        } else {
            println("\u001b[31mFailed to instantiation runner for day $dayNumber: ${cause?.toString() ?: e.toString()}\u001b[0m")
        }
        exitProcess(1)
    } catch (e: ClassNotFoundException) {
        println("\u001b[31mNo solution found for day $dayNumber\u001b[0m")
        exitProcess(2)
    }

    runPart(day, 1)
    runPart(day, 2)
}

private fun runPart(day: DayRunner, part: Int) {
    println("\u001b[1mPart $part:\u001b[0m")
    // time runtime
    val start = System.currentTimeMillis()
    when (part) {
        1 -> day.runPart1()
        2 -> day.runPart2()
        else -> throw IllegalArgumentException("Part $part is not a valid part.")
    }
    val end = System.currentTimeMillis()
    println("\u001b[36mRuntime: ${end - start}ms\u001b[0m\n")
}
