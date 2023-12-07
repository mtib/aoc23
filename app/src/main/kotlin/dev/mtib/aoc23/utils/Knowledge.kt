package dev.mtib.aoc23.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

typealias DayString = String
typealias Day = Number
typealias YearString = String
typealias Year = Number
typealias PartString = String
typealias Part = Number

class Knowledge {

    @Serializable
    data class DayKnowledge(
        val upperBound: Long? = null,
        val lowerBound: Long? = null,
        val solution: Long? = null,
    )

    companion object {
        private val knowledgeData by lazy { KnowledgeFile.load() }
        fun get(year: Year, day: Day, part: Part): Knowledge.DayKnowledge? {
            return knowledgeData.value[year.toString()]?.get(day.toString())?.get(part.toString())
        }

        fun check(year: Year, day: Day, part: Part, solution: Any?): CheckResult {
            val dayKnowledge = get(year, day, part) ?: return Unknown()

            if (dayKnowledge.solution != null) {
                return if (dayKnowledge.solution.toString() != solution.toString()) {
                    Incorrect(dayKnowledge.solution)
                } else {
                    Correct()
                }
            }

            val numericSolution = run {
                if (solution is Number) {
                    solution.toLong()
                } else {
                    solution.toString().toLongOrNull()
                }
            } ?: return Unknown()

            if (dayKnowledge.upperBound != null && numericSolution > dayKnowledge.upperBound) {
                return TooBig(dayKnowledge.upperBound)
            }

            if (dayKnowledge.lowerBound != null && numericSolution < dayKnowledge.lowerBound) {
                return TooSmall(dayKnowledge.lowerBound)
            }

            return Unknown()
        }
    }


    @JvmInline
    @Serializable
    value class KnowledgeFile(val value: Map<YearString, Map<DayString, Map<PartString, Knowledge.DayKnowledge>>>) {
        companion object {
            private const val KNOWLEDGE_FILE = "knowledge.json"
            fun load(): KnowledgeFile {
                val file = File(KNOWLEDGE_FILE)
                if (!file.exists()) {
                    return KnowledgeFile(emptyMap())
                }
                return Json.decodeFromString(file.readText())
            }
        }
    }

    abstract class CheckResult

    class Correct : CheckResult()
    open class Incorrect(val reference: Long) : CheckResult()
    open class Bound(limit: Long) : Incorrect(limit)
    class TooBig(limit: Long) : Bound(limit)
    class TooSmall(limit: Long) : Bound(limit)
    class Unknown : CheckResult()
}

