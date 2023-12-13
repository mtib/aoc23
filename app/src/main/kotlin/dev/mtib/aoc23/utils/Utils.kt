package dev.mtib.aoc23.utils

import kotlin.io.path.Path
import kotlin.io.path.readLines

fun readLines(path: String): List<String> = Path(path).readLines()

fun <T> Collection<T>.split(predicate: (item: T) -> Boolean): Collection<Collection<T>> {
    val result = mutableListOf<MutableList<T>>()
    var current = mutableListOf<T>()
    this.forEach {
        if (predicate(it)) {
            result.add(current)
            current = mutableListOf()
        } else {
            current.add(it)
        }
    }
    result.add(current)
    return result
}

fun <T> Sequence<T>.split(predicate: (item: T) -> Boolean): Sequence<Sequence<T>> = sequence {
    val iterable = this@split.iterator()
    while (iterable.hasNext()) {
        yield(sequence {
            while (iterable.hasNext()) {
                val next = iterable.next()
                if (predicate(next)) {
                    yield(next)
                } else {
                    return@sequence
                }
            }
        })

    }
}
