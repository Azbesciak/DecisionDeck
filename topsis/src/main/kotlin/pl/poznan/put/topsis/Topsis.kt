package pl.poznan.put.topsis

class Topsis(private val alternatives: List<TopsisAlternative>, private val criteria: List<Criterion>) {
    fun calculate(): TopsisRanking {
        val weightedNormalized = NormalizationWeightingCalculator(alternatives, criteria).calculate()
        val idealAlternatives = IdealAlternativeCalculator(weightedNormalized.alternatives, criteria).calculate()
        return DistanceRankingCalculator(weightedNormalized.alternatives, criteria, idealAlternatives).calculate()
    }
}