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

        fun addToStringBuilder(stringBuilder: StringBuilder, indent: String = "  ") {
            toScad().lines().filter { it.isNotBlank() }.forEach {
                stringBuilder.appendLine("$indent$it")
            }
        }
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
    private val modules = mutableListOf<Module>()

    fun addShape(shape: Shape) = shapes.add(shape)

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
            if (x == 0 && y == 0 && z == 0) {
                shapes.forEach {
                    it.addToStringBuilder(this, "")
                }
                return@buildString
            }
            appendLine("translate([$x, $y, $z]) {")
            shapes.forEach {
                it.addToStringBuilder(this, "  ")
            }
            appendLine("}")
        }
    }

    fun translate(x: Number, y: Number, z: Number, block: ScadBuilder.() -> Unit) {
        val scadBuilder = ScadBuilder()
        scadBuilder.block()
        shapes.add(Translate(x, y, z, scadBuilder.shapes))
    }

    class Rotate(
        val degrees: Number,
        val shapes: List<Shape>
    ) : Shape() {
        override fun toScad(): String = buildString {
            if (degrees == 0) {
                shapes.forEach {
                    it.addToStringBuilder(this, "")
                }
                return@buildString
            }
            appendLine("rotate([0, 0, $degrees]) {")
            shapes.forEach {
                it.addToStringBuilder(this, "  ")
            }
            appendLine("}")
        }
    }

    fun rotate(degrees: Number, block: ScadBuilder.() -> Unit) {
        val scadBuilder = ScadBuilder()
        scadBuilder.block()
        shapes.add(Rotate(degrees, scadBuilder.shapes))
    }

    class HexColor(
        val hexString: String,
        val shapes: List<Shape>
    ) : Shape() {
        override fun toScad(): String = buildString {
            appendLine("color(\"#$hexString\") {")
            shapes.forEach {
                it.addToStringBuilder(this, "  ")
            }
            appendLine("}")
        }
    }

    fun hexColor(hexString: String, block: ScadBuilder.() -> Unit) {
        val scadBuilder = ScadBuilder()
        scadBuilder.block()
        shapes.add(HexColor(hexString, scadBuilder.shapes))
    }

    class Module(
        val shapes: List<Shape>,
        val name: String
    ) : Shape() {
        fun toModel(): String = buildString {
            appendLine("module $name() {")
            shapes.forEach {
                it.addToStringBuilder(this, "  ")
            }
            appendLine("}")
        }

        override fun toScad(): String = buildString {
            appendLine("$name();")
        }
    }

    fun module(block: ScadBuilder.() -> Unit): Module {
        val scadBuilder = ScadBuilder()
        scadBuilder.block()
        val module = Module(scadBuilder.shapes, "model${modules.size}")
        modules.add(module)
        return module
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
            modules.forEach {
                appendLine(it.toModel())
            }
            appendLine("union() {")
            shapes.forEach { shape ->
                shape.addToStringBuilder(this, "  ")
            }
            appendLine("}")
        }
    }

    fun saveToDisk(name: String = "output") {
        val tempfile = createTempFile(suffix = ".scad", prefix = "aoc_scad_full_")
        tempfile.writeText(toScad())
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