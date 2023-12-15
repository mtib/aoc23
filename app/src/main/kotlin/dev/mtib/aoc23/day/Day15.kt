package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single

@Single
class Day15 : AbstractDay(15) {

    private fun String.hash(): Int {
        return this.fold(0) { acc, it -> ((acc + it.code) * 17) % 256 }
    }


    override fun solvePart1(input: Array<String>): Any? {
        debug {
            require("HASH".hash() == 52) { "Hashing algorithm is broken on HASH" }
            require(
                "rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7".split(",")
                    .sumOf { it.hash() } == 1320) { "Hashing algorithm is broken on example" }
        }
        return input[0].split(",").sumOf { it.hash() }
    }

    override fun solvePart2(input: Array<String>): Any? {
        val lensDescriptions = input[0].split(",")

        class Lens(val label: String, val focalLength: Int)
        class Box(val lenses: MutableList<Lens>) {
            /**
             * Needs to be multiplied with the box position
             */
            fun relativeFocusingPower(): Int {
                return lenses.withIndex().sumOf { (position, lens) -> (position + 1) * lens.focalLength }
            }
        }

        val boxes = Array(256) { Box(mutableListOf()) }
        lensDescriptions.forEach { lensText ->
            when {
                '-' in lensText -> {
                    val label = lensText.slice(0..<lensText.length - 1)
                    val boxNum = label.hash()
                    boxes[boxNum].lenses.removeIf { it.label == label }
                }

                '=' in lensText -> {
                    val (label, focalLength) = lensText.split("=")
                    val newLens = Lens(label, focalLength.toInt())
                    val boxNum = label.hash()
                    var replaced = false
                    boxes[boxNum].lenses.forEachIndexed { index, lens ->
                        if (lens.label == label) {
                            boxes[boxNum].lenses[index] = newLens
                            replaced = true
                        }
                    }
                    if (!replaced) {
                        boxes[boxNum].lenses.add(newLens)
                    }
                }

                else -> throw IllegalArgumentException("Invalid lens description: $lensText")
            }
        }
        return boxes.withIndex().sumOf { (position, box) -> (position + 1) * box.relativeFocusingPower() }
    }
}