package dev.mtib.aoc23.utils

import kotlin.io.path.Path
import kotlin.io.path.readLines

fun readLines(path: String): List<String> = Path(path).readLines()
