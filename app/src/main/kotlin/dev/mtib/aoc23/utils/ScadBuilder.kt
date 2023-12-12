package dev.mtib.aoc23.utils

import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.writeText

/**
 * <a href="https://en.wikibooks.org/wiki/OpenSCAD_User_Manual/Using_OpenSCAD_in_a_command_line_environment">OpenSCAD</a>
 *
 * ```
 * openscad -o output.stl input.scad
 * ```
 */
class ScadBuilder private constructor() {

    abstract class Shape {
        abstract fun toLine(variable: String): String
    }

    data class Box constructor(
        val x: Number,
        val y: Number,
        val z: Number,
        val l: Number,
        val w: Number,
        val h: Number
    ) : Shape() {
        override fun toLine(variable: String): String =
            "translate([$x, $y, $z]) { cube([$l, $w, $h]); }"
    }

    private val shapes = mutableListOf<Shape>()

    fun addBox(x: Number, y: Number, z: Number, l: Number, w: Number, h: Number) {
        shapes.add(Box(x, y, z, l, w, h))
    }

    companion object {
        fun build(block: ScadBuilder.() -> Unit): ScadBuilder {
            val scadBuilder = ScadBuilder()
            scadBuilder.block()
            return scadBuilder
        }
    }

    fun toScad(): String = buildString {
        shapes.forEachIndexed { index, shape ->
            appendLine(shape.toLine("shape$index"))
        }
    }

    fun saveToDisk(name: String = "output") {
        val tempfile = createTempFile(suffix = ".scad")
        tempfile.writeText(toScad())
        println("Saved to ${tempfile.toAbsolutePath()}")
        val openscadCommand = System.getenv("OPENSCAD_PATH") ?: "openscad"
        val fullCommand =
            "$openscadCommand -o $name.stl -o $name.png ${tempfile.toAbsolutePath()}"
        println(fullCommand)
        ProcessBuilder()
            .inheritIO()
            .command(fullCommand.split(" "))
            .start()
            .waitFor()
        println("done")
        tempfile.deleteExisting()
    }
}