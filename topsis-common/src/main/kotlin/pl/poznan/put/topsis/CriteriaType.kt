package pl.poznan.put.topsis

import pl.poznan.put.xmcda.ranking.Alternative

enum class CriteriaType {
    COST, PROFIT
}

data class Criterion(val name: String, val weight: Double, val type: CriteriaType)
data class TopsisAlternative(override val name: String, val criteriaValues: Map<Criterion, Double>) : Alternative
