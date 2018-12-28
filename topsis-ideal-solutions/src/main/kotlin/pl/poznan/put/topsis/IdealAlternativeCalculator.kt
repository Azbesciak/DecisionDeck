package pl.poznan.put.topsis

class IdealAlternativeCalculator(
        alternatives: List<TopsisAlternative>,
        criteria: List<Criterion>
) : AlternativesCalculator<IdealAlternatives>(alternatives, criteria) {

    override fun calculate(): IdealAlternatives {
        val posIdealSol = calculateIdealSolution("positive") { a, b -> a > b }
        val negIdealSol = calculateIdealSolution("negative") { a, b -> a < b }
        return IdealAlternatives(posIdealSol, negIdealSol)
    }

    private inline fun calculateIdealSolution(
            name: String,
            comp: (v1: Double, v2: Double) -> Boolean
    ): TopsisAlternative {
        val idealCriteriaValues = criteria
                .mapIndexed { i, criterion ->
                    (criterion to calculateIdealCriterionValue(i, comp)).revertOriginalValueSign()
                }.toMap()
        return TopsisAlternative(name, idealCriteriaValues)
    }

    private inline fun calculateIdealCriterionValue(
            criterionIndex: Int,
            comp: (v1: Double, v2: Double) -> Boolean
    ) = decisionMatrix
            .map { it[criterionIndex] }
            .reduce { best, value -> if (comp(best, value)) best else value }
}

data class IdealAlternatives(
        val positive: TopsisAlternative,
        val negative: TopsisAlternative
)