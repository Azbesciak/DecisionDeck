package pl.poznan.put.topsis

import org.xmcda.*
import org.xmcda.Scale.PreferenceDirection.*
import pl.poznan.put.xmcda.InputsHandler

class TopsisInputsHandler<T>(
        private val tableNames: List<String>,
        private val weightsFactory: WeightsFactory,
        private val inputSupplier: (List<List<TopsisAlternative>>, List<Criterion>) -> T
) : InputsHandler<T> {
    /**
     * This class contains every element which are needed to compute
     * TOPSIS.<br></br>
     * It is populated by [TopsisInputsHandler.checkAndExtractInputs].
     */
    /**
     * @param xmcda
     * @return
     */
    override fun checkAndExtractInputs(xmcda: XMCDA): T {
        // compilation error fix
        xmcda.run {
            require(criteria.isNotEmpty()) { "Criteria not provided" }
            require(criteriaScalesList.isNotEmpty()) { "Criteria scales not provided" }
            require(alternatives.isNotEmpty()) { "Alternatives not provided" }
        }
        xmcda.run {
            val criteriaTypes = getTypesOfCriteria()
            val criteria = createCriteria(criteriaTypes)
            validatePerformanceTablesNo()
            val alternatives = extractAlternatives(criteria)
            return inputSupplier(alternatives, criteria.unzip().second)
        }
    }

    private fun XMCDA.extractAlternatives(criteria: List<Pair<org.xmcda.Criterion, Criterion>>) =
            performanceTablesList.zip(tableNames).map { it.first.extractFor(criteria, it.second) }

    private fun XMCDA.validatePerformanceTablesNo() {
        require(performanceTablesList.size == tableNames.size) {
            "Expected exactly ${tableNames.size} performanceTable${if (tableNames.size == 1) "" else "s"}, got ${performanceTablesList.size}"
        }
    }

    private fun XMCDA.createCriteria(criteriaTypes: Map<org.xmcda.Criterion, CriteriaType>): List<Pair<org.xmcda.Criterion, Criterion>> {
        val weights = weightsFactory.provide(this)
        return criteria.map {
            val weight = weights(it)
            val criterionType = requireNotNull(criteriaTypes[it]) { "criteria type for criterion ${it.id()} not found" }
            it to Criterion(it.id(), weight, criterionType)
        }
    }

    private fun XMCDA.getTypesOfCriteria() =
            criteriaScalesList.first().mapValues {
                require(it.value.size == 1) { "Expected only one criterion scale for ${it.key.id()}, got ${it.value.size}" }
                val preferenceDirection = requireNotNull((it.value.first() as QuantitativeScale<*>).preferenceDirection) {
                    "PreferenceDirection not set for ${it.key.id()}"
                }
                when (preferenceDirection) {
                    MIN -> CriteriaType.COST
                    MAX -> CriteriaType.PROFIT
                }
            }

    private fun PerformanceTable<*>.extractFor(criteria: List<Pair<org.xmcda.Criterion, Criterion>>, name: String): List<TopsisAlternative> {
        require(!hasMissingValues()) { "'$name' performanceTable has missing values" }
        return alternatives.map { alt ->
            val criteriaMap = criteria.map { (xmcdaCryt, cryt) -> cryt to getValue(alt, xmcdaCryt).toString().toDouble() }.toMap()
            TopsisAlternative(alt.id(), criteriaMap)
        }
    }
}

data class TopsisInputs(
        val alternatives: List<TopsisAlternative>,
        val criteria: List<Criterion>
)

fun singleInputTopsisHandler(weightsFactory: WeightsFactory) =
        TopsisInputsHandler(listOf("main"), weightsFactory) { alt, crs ->
            TopsisInputs(alt.first(), crs)
        }

fun TopsisAlternative.asPerformance() = listOf(this).asPerformance()

fun List<TopsisAlternative>.asPerformance() = XMCDA().apply {
    performanceTablesList += performanceFrom(this@asPerformance)
}

private fun performanceFrom(alternatives: List<TopsisAlternative>) =
        PerformanceTable<Double>().apply {
            alternatives.forEach { plusAssign(it) }
        }

private operator fun PerformanceTable<Double>.plusAssign(alt: TopsisAlternative) {
    val xmcdaAlt = Alternative(alt.name)
    alt.criteriaValues.forEach {
        put(xmcdaAlt, Criterion(it.key.name), it.value)
    }
}

interface WeightsFactory {
    fun provide(xmcda: XMCDA): (org.xmcda.Criterion) -> Double
}

object CriteriaWeightsFactory : WeightsFactory {
    override fun provide(xmcda: XMCDA): (org.xmcda.Criterion) -> Double {
        require(xmcda.criteriaValuesList.size == 1) { "Expected criteriaValue to contain exactly one list, got ${xmcda.criteriaValuesList.size}" }
        val weights = xmcda.criteriaValuesList.first()
        return {
            val weightValue = requireNotNull(weights[it]) { "weight for criterion ${it.id()} not found" }
            weightValue.first().value.toString().toDouble()
        }
    }
}

object NoWeightFactory : WeightsFactory {
    override fun provide(xmcda: XMCDA): (org.xmcda.Criterion) -> Double = { 1.0 }
}
