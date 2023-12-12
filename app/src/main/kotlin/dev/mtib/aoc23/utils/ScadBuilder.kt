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
        abstract fun toScad(): String
    }

    open class Box(
        val l: Number,
        val w: Number,
        val h: Number
    ) : Shape() {
        override fun toScad(): String =
            "cube([$l, $w, $h]);"
    }

    class TranslatedBox(
        val x: Number,
        val y: Number,
        val z: Number,
        l: Number,
        w: Number,
        h: Number
    ) : Box(l, w, h) {
        override fun toScad(): String =
            "translate([$x, $y, $z]) { cube([$l, $w, $h]); }"
    }

    private val shapes = mutableListOf<Shape>()

    fun addBox(l: Number, w: Number, h: Number) {
        shapes.add(Box(l, w, h))
    }

    fun addTranslatedBox(x: Number, y: Number, z: Number, l: Number, w: Number, h: Number) {
        shapes.add(TranslatedBox(x, y, z, l, w, h))
    }

    class Translate(
        val x: Number,
        val y: Number,
        val z: Number,
        val shapes: List<Shape>
    ) : Shape() {
        override fun toScad(): String = buildString {
            appendLine("translate([$x, $y, $z]) {")
            shapes.forEach {
                appendLine(it.toScad())
            }
            appendLine("}")
        }
    }

    fun translate(x: Number, y: Number, z: Number, block: ScadBuilder.() -> Unit) {
        val scadBuilder = ScadBuilder()
        scadBuilder.block()
        shapes.add(Translate(x, y, z, scadBuilder.shapes))
    }

    companion object {
        fun build(block: ScadBuilder.() -> Unit): ScadBuilder {
            val scadBuilder = ScadBuilder()
            scadBuilder.block()
            return scadBuilder
        }
    }

    fun toScad(): String {
        return buildString {
            shapes.forEach { shape ->
                appendLine(shape.toScad())
            }
        }
    }

    fun saveToDisk(name: String = "output") {
        val tempfile = createTempFile(suffix = ".scad", prefix = "aoc_scad_full_")
        tempfile.writeText(buildString {
            appendLine("union() {")
            appendLine(toScad())
            appendLine("}")
        })
        println("Saved to ${tempfile.toAbsolutePath()}")
        val openscadCommand = System.getenv("OPENSCAD_PATH") ?: "openscad"
        val fullCommand =
            "$openscadCommand --enable=fast-csg --export-format binstl -o $name.stl ${tempfile.toAbsolutePath()}"
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