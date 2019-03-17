package pl.poznan.put.ahp

import org.xmcda.*
import pl.poznan.put.ahp.AhpAlternative.Companion.ranking
import pl.poznan.put.ahp.MatrixMaker.matrix
import pl.poznan.put.xmcda.InputsHandler

/**
 * @author
 */
internal object AhpInputsHandler : InputsHandler<AhpResult> {
    /**
     * This class contains every element which are needed to compute
     * AHP.<br></br>
     * It is populated by [InputsHandler.checkAndExtractInputs].
     */
    /**
     * @param xmcda
     * @return
     */
    override fun checkAndExtractInputs(xmcda: XMCDA) = HierarchyParser(xmcda).ranking()

    private class HierarchyParser(private val xmcda: XMCDA) {
        fun ranking() = build {
            alternatives = xmcda.alternatives.activeAlternatives.map { AhpAlternative(it.id()) }.sortedBy { it.name }
            criteria = xmcda.criteria.activeCriteria.map { it.id() }.sorted()
            xmcda.criteriaHierarchiesList.forEach { it.flatten() }
            xmcda.criteriaMatricesList.forEach { it.fetch() }
            xmcda.alternativesMatricesList.forEach { it.fetch() }
        }

        private inline fun build(f: AhpBuilder.() -> Unit) =
                AhpBuilder().run {
                    f()
                    build()
                }
    }

    private class AhpBuilder : AhpBaseBuilder() {
        var alternatives = listOf<AhpAlternative>()
        private var relations = Relations()
        private var alternativesPreferences = mutableMapOf<CrytId, List<RelationValue>>()
        private var topNode: CrytId? = null

        fun CriteriaHierarchy.flatten() {
            require(rootNodes.size == 1) {
                "Exactly one root required!, found ${rootNodes.map {
                    it.criterion.id() ?: "unknown ID"
                }}"
            }
            require(topNode == null) { "root hierarchy already set" }
            topNode = rootNodes.first().criterion.id()
            rootNodes.forEach { it.flatten() }
        }

        private fun CriterionHierarchyNode.flatten() {
            children.forEach { it.flatten() }
            val critId = criterion.id()
            requireNotNull(critId) { "criterion id not set in hierarchy" }
            require(critId !in relations) { "criteria $critId duplicated in hierarchy outside of the context" }
            relations[critId] = children.map { it.criterion.id() }.sorted()
        }

        fun AlternativesMatrix<*>.fetch() {
            requireNotNull(id()) { "Missing criteriaID for alternatives comparision" }
            require(id() !in alternativesPreferences) { "duplicated alternatives comparision on criterion '${id()}'" }
            alternativesPreferences[id()] = map { it.relation }
        }

        override fun build(): AhpResult {
            val criteriaFromPreference = getCriteriaFromPreference()
            val leafCriteria = relations.leafs
            require(criteriaFromPreference == leafCriteria) {
                """
                |leaf criteria must be the same in hierarchy and in preferences.
                |   criteria from hierarchy: $leafCriteria
                |   preferences: $criteriaFromPreference
            """.trimMargin()
            }

            val criteriaPreferences = CriteriaPreferences(matrix(criteriaComp) { relations.of(it) }.toMutableMap())
            val invalidNodes = createLeafsWithAlternatives(leafCriteria)
                    .asLeafs()
                    .topCategory(relations, criteriaPreferences)
                    .checkValidity()
            return AhpResult(alternatives.ranking(), invalidNodes)
        }

        private fun getCriteriaFromPreference() =
                alternativesPreferences.fetchIdWithAssertion("preferences")

        private fun createLeafsWithAlternatives(leafCriteria: List<CrytId>): Map<CrytId, Category> {
            val sameAlternativesRelations = alternatives.map { it.relation() }
            val altPreferencesOnCriteria = matrix(alternativesPreferences) { sameAlternativesRelations }
            return leafCriteria.map { it to Category(it, alternatives, altPreferencesOnCriteria[it]!!) }.toMap()
        }


    }
}
