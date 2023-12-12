package dev.mtib.aoc23.utils

interface BeforeRunner {

    /**
     * Something to do before each run that shouldn't be timed, like clearing caches.
     */
    fun before()
}