package pl.poznan.put.topsis

import pl.poznan.put.xmcda.ranking.RankEntry
import pl.poznan.put.xmcda.ranking.Ranking
import kotlin.math.pow
import kotlin.math.sqrt

class DistanceCalculator(
        alternatives: List<TopsisAlternative>,
        criteria: List<Criterion>,
        private val idealAlternatives: IdealAlternatives
) : AlternativesCalculator<TopsisRanking>(alternatives, criteria) {
    override fun calculate(): TopsisRanking {
        val negIdealVector = idealAlternatives.negative.extractCriteriaValues()
        val posIdealVector = idealAlternatives.positive.extractCriteriaValues()
        val distanceToNeg = calculateDistanceToIdealSolution(negIdealVector)
        val distanceToPos = calculateDistanceToIdealSolution(posIdealVector)
        val coef = calculateClosenessCoefficient(distanceToPos, distanceToNeg)
        return TopsisRanking(alternatives, coef)
    }

    private fun calculateDistanceToIdealSolution(idealSol: DoubleArray): DoubleArray {
        val distanceToIdealSol = DoubleArray(alternativeNo)
        for (row in 0 until alternativeNo) {
            var temp = 0.0
            for (col in 0 until criteriaNo) {
                temp += (decisionMatrix[row][col] - idealSol[col]).pow(2)
            }
            distanceToIdealSol[row] = sqrt(temp)
        }
        return distanceToIdealSol
    }

    private fun calculateClosenessCoefficient(distToPosIdealSol: DoubleArray, distToNegIdealSol: DoubleArray) =
            distToNegIdealSol
                    .zip(distToPosIdealSol)
                    .map { (neg, pos) -> neg / (neg + pos) }
}

data class TopsisRankEntry(
        override val alternative: TopsisAlternative,
        override val value: Double
) : RankEntry<TopsisAlternative> {
    override fun toString() = "${alternative.name} - $value"
}

class TopsisRanking private constructor(
        private val ranking: List<TopsisRankEntry>
) : Ranking<TopsisAlternative>, List<RankEntry<TopsisAlternative>> by ranking {
    constructor(alternatives: List<TopsisAlternative>, coef: List<Double>) : this(
            alternatives.zip(coef)
                    .asSequence()
                    .sortedByDescending { it.second }
                    .map { TopsisRankEntry(it.first, it.second) }
                    .toList()
    )
}
