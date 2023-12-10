package dev.mtib.aoc23.utils

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
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
        @Contextual
        val upperBound: Long? = null,
        @Contextual
        val lowerBound: Long? = null,
        @Contextual
        val solution: Long? = null,
    )

    companion object {
        private val knowledgeData by lazy { KnowledgeFile.load() }
        fun get(year: Year, day: Day, part: Part): DayKnowledge? {
            return knowledgeData.value[year.toString()]?.get(day.toString())?.get(part.toString())
        }

        fun check(year: Year, day: Day, part: Part, solution: Any?): CheckResult {
            val dayKnowledge = get(year, day, part) ?: return Unknown()
            val daySolution = dayKnowledge.solution

            val numericSolution = solution.toString().toLongOrNull()

            if (daySolution != null) {
                return if (numericSolution.toString() != daySolution.toString()) {
                    Incorrect(daySolution)
                } else {
                    Correct()
                }
            }

            val dayUpperBound = dayKnowledge.upperBound
            if (dayUpperBound != null && numericSolution != null && numericSolution >= dayUpperBound) {
                return TooBig(dayUpperBound)
            }

            val dayLowerBound = dayKnowledge.lowerBound
            if (dayLowerBound != null && numericSolution != null && numericSolution <= dayLowerBound) {
                return TooSmall(dayLowerBound)
            }

            return Unknown()
        }
    }


    @JvmInline
    @Serializable
    value class KnowledgeFile(val value: Map<YearString, Map<DayString, Map<PartString, DayKnowledge>>>) {
        companion object {
            private const val KNOWLEDGE_FILE = "knowledge.json"
            fun load(): KnowledgeFile {
                val file = File(KNOWLEDGE_FILE)
                if (!file.exists()) {
                    return KnowledgeFile(emptyMap())
                }
                return Json.decodeFromString(file.readText())
            }

            fun createDay(
                year: Year,
                day: Day,
                part: Part,
                solution: Long? = null,
                upperBound: Long? = null,
                lowerBound: Long? = null
            ) {
                val file = load()
                val lastDayKnowledge = file.value[year.toString()]?.get(day.toString())?.get(part.toString())
                val dayKnowledge = DayKnowledge(
                    upperBound ?: lastDayKnowledge?.upperBound,
                    lowerBound ?: lastDayKnowledge?.lowerBound,
                    solution ?: lastDayKnowledge?.solution
                )
                val years = file.value.toMutableMap()
                val days = years.getOrPut(year.toString()) { emptyMap() }.toMutableMap()
                val parts = days.getOrPut(day.toString()) { emptyMap() }.toMutableMap()
                parts[part.toString()] = dayKnowledge
                days[day.toString()] = parts
                years[year.toString()] = days
                KnowledgeFile(years).save()
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun save() {
            val format = Json { prettyPrint = true; encodeDefaults = true; explicitNulls = true }
            val file = File(KNOWLEDGE_FILE)
            file.writeText(format.encodeToString(this.value))
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

