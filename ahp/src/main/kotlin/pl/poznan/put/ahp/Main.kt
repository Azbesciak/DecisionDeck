package pl.poznan.put.ahp

import org.xmcda.ProgramExecutionResult
import org.xmcda.parsers.xml.xmcda_v3.XMCDAParser
import org.xmcda.v2.*
import org.xmcda.v2.Node
import pl.poznan.put.ahp.Alternative.Companion.ranking
import pl.poznan.put.xmcda.*
import pl.poznan.put.xmcda.Utils.*
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val arguments = parseCmdLineArguments(args)
        val result = processV2(arguments.inputDirectory)
        writeResult(arguments.outputDirectory, result)
    }

    private fun processV2(inputDir: String): Result<org.xmcda.XMCDA> {
        val xmcdA2Reader = XMCDA2Reader(inputDir,
                XmcdaMapping("alternatives"),
                XmcdaMapping("criteria"),
                XmcdaMapping("hierarchy"),
                XmcdaMapping("criteria_comparisons", listOf("criteriaComparisons")),
                XmcdaMapping("preference", listOf("alternativesComparisons"))
        )
        val result = xmcdA2Reader.read()
        if (result.executionResult.isError)
            return Result(result.executionResult)
        return try {
            val ranking = V2Parser(result.xmcda!!).ranking()
            val xmcdaRanking = RankingParser.convert(ranking)
            Result(result.executionResult, xmcdaRanking)
        } catch (e: Throwable) {
            result.executionResult.add(e)
            Result(result.executionResult)
        }
    }

    private fun writeResult(outputDir: String, ranking: Result<org.xmcda.XMCDA>) {
        File(outputDir).mkdirs()
        try {
            ranking.xmcda?.apply {
                val rankingFile = File(outputDir, "ranking.xml")
                XMCDAParser().writeXMCDA(this, rankingFile)
            }
        } catch (t: Throwable) {
            ranking.executionResult.add(t)
        }
        Utils.writeProgramExecutionResultsAndExit(File(outputDir, "messages.xml"), ranking.executionResult, XmcdaVersion.v2)
    }

    class V2Parser(private val xmcda: XMCDA) {
        private inline fun <reified T> String.forEachValue(f: T.() -> Unit) {
            xmcda.getTags(this).forEach {
                f((it.value as? T) ?: throw IllegalArgumentException("$it is not instance of ${T::class.java}"))
            }
        }

        private inline fun <reified T, reified X> String.mapBy(f: T.() -> Iterable<X>) =
                xmcda.getTags(this).flatMap { f(it.value as T) }

        fun ranking() = build {
            alternatives = "alternatives".mapBy<Alternatives, Alternative> {
                descriptionOrAlternative.map { Alternative((it as org.xmcda.v2.Alternative).id) }
            }.sortedBy { it.name }
            criteria = "criteria".mapBy<Criteria, String> { criterion.map { (it as Criterion).id } }.sorted()
            "hierarchy".forEachValue<Hierarchy> { flatten() }
            "criteriaComparisons".forEachValue<CriteriaComparisons> { fetch() }
            "alternativesComparisons".forEachValue<AlternativesComparisons> { fetch() }
        }

        private inline fun build(f: AhpBuilder.() -> Unit) =
                AhpBuilder().run {
                    f()
                    build()
                }
    }

    private fun ProgramExecutionResult.add(t: Throwable) {
        addError(getMessage(t))
    }

    class AhpBuilder {
        var criteria = listOf<CrytId>()
        var relations = Relations()
        var alternativesPreferences = mutableMapOf<CrytId, List<RelationValue>>()
        var criteriaComp = mutableMapOf<CrytId, List<RelationValue>>()
        var alternatives = listOf<Alternative>()
        var topNode: CrytId? = null

        fun Hierarchy.flatten() {
            require(node.size == 1) {
                "Exactly one root required!, found ${node.map {
                    it.criterionID ?: "unknown ID"
                }}"
            }
            require(topNode == null) { "root hierarchy already set" }
            topNode = node.first().criterionID
            node.forEach { it.flatten() }
        }

        private fun Node.flatten() {
            node.forEach { it.flatten() }
            requireNotNull(criterionID) { "criterion id not set in hierarchy" }
            require(relations.contains(criterionID).not()) { "criteria $criterionID duplicated in hierarchy outside of the context" }
            relations[criterionID] = node.map { it.criterionID }.sorted()
        }

        fun CriteriaComparisons.fetch() {
            requireNotNull(id) { "Missing parent criteria comparision id " }
            require(criteriaComp.contains(id).not()) { "duplicated criteria comparisons for criterion $id" }
            criteriaComp[id] = pairs.pair.map { it.relation }
        }

        fun AlternativesComparisons.fetch() {
            requireNotNull(id) { "Missing criteriaID for alternatives comparision" }
            require(alternativesPreferences.contains(id).not()) { "duplicated alternatives comparision on criterion $id" }
            alternativesPreferences[id] = pairs.pair.map { it.relation }
        }

        private val CriteriaPair.relation
            get() = RelationValue(
                    initial.criterionID,
                    terminal.criterionID,
                    value()
            )
        private val AlternativePair.relation
            get() = RelationValue(
                    initial.alternativeID,
                    terminal.alternativeID,
                    value()
            )

        private fun CriteriaPair.value() = valueOrValues.value()
        private fun AlternativePair.value() = value.value()
        private fun List<Any>.value() = (first() as Value).run { real ?: integer.toDouble() }.also {
            require(it > 0) { "Value must be positive real value, got $it" }
        }

        fun build(): Ranking {
            val criteriaFromPreference = getCriteriaFromPreference()
            val leafCriteria = relations.leafs
            require(criteriaFromPreference == leafCriteria) {
                """
                |leaf criteria must be the same in hierarchy and in preferences.
                |   criteria from hierarchy: $leafCriteria
                |   preferences: $criteriaFromPreference
            """.trimMargin()
            }
            val criteriaPreferences = CriteriaPreferences(criteriaComp.toMatrix { k -> relations.of(k) }.toMutableMap())
            createLeafsWithAlternatives(leafCriteria)
                    .asLeafs()
                    .topCategory(relations, criteriaPreferences)
            return alternatives.ranking()
        }

        private fun getCriteriaFromPreference() =
                alternativesPreferences.run {
                    require(isNotEmpty()) { "preferences not found" }
                    forEach { k, y -> require(y.isNotEmpty()) { "preferences on $k are not set" } }
                    keys.sorted()
                }

        private fun createLeafsWithAlternatives(leafCriteria: List<CrytId>): Map<CrytId, Category> {
            val sameAlternativesRelations = alternatives.map { it.relation() }
            val altPreferencesOnCriteria = alternativesPreferences.toMatrix { sameAlternativesRelations }
            return leafCriteria.map { it to Category(it, alternatives, altPreferencesOnCriteria[it]!!) }.toMap()
        }

        private inline fun Map<String, List<RelationValue>>.toMatrix(diagonalSup: (key: String) -> List<RelationValue>) =
                mapValues { (k, v) ->
                    if (v.isEmpty()) return emptyMap<String, List<List<Double>>>()
                    val diagonal = diagonalSup(k)
                    val matrix = (v.flatMap { listOf(it, it.reversed()) } + diagonal)
                            .sortedWith(compareBy({ it.firstID }, { it.secondID }))
                    matrix.map { it.firstID to it.secondID }
                            .requireDistinct { "values of $it are duplicated comparision for $k" }
                    matrix.map { it.value }.chunked(diagonal.size).also {
                        require(it.size == it.last().size) { "dimensions of $k are not equal" }
                        require(it.size == diagonal.size) { "too few elements for $k" }
                    }
                }

        private fun <T> List<T>.requireDistinct(onError: (T) -> String) {
            val set = mutableSetOf<T>()
            forEach {
                require(set.contains(it).not()) { onError(it) }
                set += it
            }
        }
    }

    data class RelationValue(
            val firstID: String,
            val secondID: String,
            val value: Double
    ) {
        fun reversed() = RelationValue(secondID, firstID, 1 / value)
    }

    private fun Alternative.relation() = name.relation()
    private fun String.relation() = RelationValue(this, this, 1.0)

    data class Relations(
            private val map: MutableMap<CrytId, List<CrytId>> = mutableMapOf()
    ) : MutableMap<CrytId, List<CrytId>> by map {
        val leafs get() = filter { it.value.isEmpty() }.keys.sorted()
        fun of(criterion: CrytId) = get(criterion)!!.map { it.relation() }
    }

    data class CriteriaPreferences(
            private val map: MutableMap<CrytId, List<List<Double>>> = mutableMapOf()
    ) : MutableMap<CrytId, List<List<Double>>> by map

    data class Leafs(
            private val map: MutableMap<CrytId, Category>
    ) : MutableMap<CrytId, Category> by map {
        private val single
            get(): Category {
                require(size == 1) { "expected only one head of hierarchy, got $size: ${map { it.key }}" }
                return map { it.value }.first()
            }

        fun topCategory(
                relations: Relations,
                criteriaPreferences: CriteriaPreferences
        ) = buildCat(relations, criteriaPreferences, this).single

        private tailrec fun buildCat(
                relations: Relations,
                criteriaPreferences: CriteriaPreferences,
                leafs: Leafs
        ): Leafs {
            leafs.forEach { relations.remove(it.key) }
            if (relations.isEmpty()) return leafs
            val newLeafs = matchingRelationsToCategories(relations, leafs, criteriaPreferences)
            return buildCat(relations, criteriaPreferences, newLeafs)
        }

        private fun matchingRelationsToCategories(relations: Relations, leafs: Leafs, criteriaPreferences: CriteriaPreferences) =
                relations
                        .mapValues { (k, v) -> matchRelationsWithCategories(k, v, leafs) }
                        .filter { it.value.isNotEmpty() }
                        .mapValues { (k, v) -> Category(k, v, criteriaPreferences.remove(k)!!) }
                        .asLeafs()

        private fun matchRelationsWithCategories(k: CrytId, v: List<CrytId>, leafs: Leafs) =
                v.mapNotNull { leafs.remove(it) }.also {
                    require(it.isEmpty() || it.size == v.size) { "not all categories matched for $k" }
                }
    }

    fun Map<CrytId, Category>.asLeafs() = Leafs(toMutableMap())
}

typealias AltID = String
typealias CrytId = String
typealias AlternativePair = AlternativesComparisons.Pairs.Pair
typealias CriteriaPair = CriteriaComparisons.Pairs.Pair