package pl.poznan.put.topsis


abstract class AlternativesCalculator<T>(
        protected val alternatives: List<TopsisAlternative>,
        protected val criteria: List<Criterion>
) {
    protected val alternativeNo: Int
    protected val criteriaNo: Int
    protected val decisionMatrix: Array<DoubleArray>

    init {
        require(criteria.isNotEmpty()) { "criteria are missing" }
        require(alternatives.isNotEmpty()) { "alternatives are missing" }
        decisionMatrix = alternatives
                .map { alt -> alt.extractCriteriaValues()}
                .toTypedArray()
        alternativeNo = alternatives.size
        criteriaNo = alternatives[0].criteriaValues.size
    }

    protected fun TopsisAlternative.extractCriteriaValues() =  criteria.map { cr ->
        val value = requireNotNull(criteriaValues[cr]) {
            "value for criterion ${cr.name} not found for alternative $name"
        }
        if (cr.type == CriteriaType.COST) -value
        else value
    }.toDoubleArray()

    abstract fun calculate(): T

    fun Pair<Criterion, Double>.revertOriginalValueSign() = run {
        first to when (first.type) {
            CriteriaType.COST -> -second
            else -> second
        }
    }
}