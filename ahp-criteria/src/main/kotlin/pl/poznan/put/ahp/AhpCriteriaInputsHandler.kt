package pl.poznan.put.ahp

import org.xmcda.*
import pl.poznan.put.ahp.AhpAlternative.Companion.ranking
import pl.poznan.put.ahp.MatrixMaker.matrix
import pl.poznan.put.xmcda.InputsHandler

/**
 * @author
 */
internal object AhpCriteriaInputsHandler : InputsHandler<AhpResult> {
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
            criteria = xmcda.criteria.activeCriteria.map { it.id() }.sorted()
            val criteriaMatricesList = xmcda.criteriaMatricesList
            require(criteriaMatricesList.size == 1) { "exactly one criteria matrix required" }
            val criteriaMat = criteriaMatricesList.first()
            if (criteriaMat.id() == null)
                criteriaMat.setId("criteria preference")
            criteriaMat.fetch()
        }

        private inline fun build(f: AhpBaseBuilder.() -> Unit) =
                AhpCriteriaBuilder().run {
                    f()
                    build()
                }
    }

    private class AhpCriteriaBuilder : AhpBaseBuilder() {
        override fun build(): AhpResult {
            val criteriaFromPreference = getCriteriaFromPreference()
            val (criteriaAlts, invalidNode) = computeWeights(criteriaFromPreference)
            return AhpResult(criteriaAlts.ranking(), invalidNode)
        }

        private fun computeWeights(criteriaFromPreference: List<CrytId>): Pair<List<AhpAlternative>, List<InvalidNode>> {
            val key = criteriaFromPreference.first()
            val criteriaAlts = criteria.map { AhpAlternative(it) }
            val diagonal = criteriaAlts.map { it.relation() }
            val mat = matrix(criteriaComp) { diagonal }.toMutableMap()
            val cat = Category(key, criteriaAlts, mat[key]!!)
            val invalidNode = cat.checkValidity()
            return criteriaAlts to invalidNode
        }

        private fun getCriteriaFromPreference() =
                criteriaComp.fetchIdWithAssertion("criteria preferences")
    }
}



