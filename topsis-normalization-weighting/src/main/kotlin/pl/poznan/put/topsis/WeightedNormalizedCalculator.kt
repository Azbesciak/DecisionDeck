package pl.poznan.put.topsis

import kotlin.math.pow
import kotlin.math.sqrt

class WeightedNormalizedCalculator(
        alternatives: List<TopsisAlternative>,
        criteria: List<Criterion>
) : AlternativesCalculator<WeightedNormalizedAlternatives>(alternatives, criteria) {
    private val weights = criteria.map { it.weight }
    override fun calculate(): WeightedNormalizedAlternatives {
        val normalized = calculateNormalizedDecisionMatrix()
        val weighted = calculateWeightedNormalizedDecisionMatrix(normalized)
        val weightedAlternatives = weighted.mapIndexed { ai, v ->
            val criteriaValues = criteria.zip(v.toList()).map { it.revertOriginalValueSign() }.toMap()
            TopsisAlternative(alternatives[ai].name, criteriaValues)
        }
        return WeightedNormalizedAlternatives(weightedAlternatives)
    }

    private fun calculateNormalizedDecisionMatrix(): Array<DoubleArray> {

        val normDecMat = Array(alternativeNo) { DoubleArray(criteriaNo) }
        (0 until criteriaNo).forEach { col ->
            var sumPow = 0.0
            (0 until alternativeNo).forEach { row ->
                sumPow += decisionMatrix[row][col].pow(2)
            }
            val sumPowSqrt = sqrt(sumPow)
            (0 until alternativeNo).forEach { row ->
                normDecMat[row][col] = decisionMatrix[row][col] / sumPowSqrt
            }
        }
        return normDecMat
    }

    private fun calculateWeightedNormalizedDecisionMatrix(normalizedDecisionMatrix: Array<DoubleArray>): Array<DoubleArray> {
        val weightedNormalizedDecisionMatrix = Array(alternativeNo) { DoubleArray(criteriaNo) }
        (0 until criteriaNo).forEach { col ->
            (0 until alternativeNo).forEach { row ->
                weightedNormalizedDecisionMatrix[row][col] = normalizedDecisionMatrix[row][col] * weights[col]
            }
        }
        return weightedNormalizedDecisionMatrix
    }
}

data class WeightedNormalizedAlternatives(
        val alternatives: List<TopsisAlternative>
)