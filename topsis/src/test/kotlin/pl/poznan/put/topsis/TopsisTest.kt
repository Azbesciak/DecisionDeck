package pl.poznan.put.topsis

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.function.Executable
import pl.poznan.put.xmcda.ranking.RankEntry


class TopsisTest {
    // https://www.slideshare.net/pranavmishra22/topsis-a-multicriteria-decision-making-approach
    private val criteria = listOf(
            Criterion("Entertainment", 4.0, CriteriaType.PROFIT),
            Criterion("Facilities", 2.0, CriteriaType.PROFIT),
            Criterion("TravelCost", 6.0, CriteriaType.COST),
            Criterion("Accommodation", 8.0, CriteriaType.COST))
    private val alternatives = listOf(
            TopsisAlternative("Hogwarts", criteria.zip(listOf(9.0, 7.0, 6.0, 7.0)).toMap()),
            TopsisAlternative("Hogsmeade", criteria.zip(listOf(8.0, 7.0, 9.0, 6.0)).toMap()),
            TopsisAlternative("Azkaban", criteria.zip(listOf(7.0, 8.0, 6.0, 6.0)).toMap())
    )
    private val topsis = Topsis(alternatives, criteria)
    private val expected = listOf(
            TopsisRankEntry(alternatives[2], 0.74),
            TopsisRankEntry(alternatives[0], 0.68),
            TopsisRankEntry(alternatives[1], 0.34)
    )

    @Test
    fun integrationTest() {
        val weightedNormalized = NormalizationWeightingCalculator(alternatives, criteria).calculate()
        val idealAlternatives = IdealAlternativeCalculator(weightedNormalized.alternatives, criteria).calculate()
        val finalRanking = DistanceRankingCalculator(weightedNormalized.alternatives, criteria, idealAlternatives).calculate()
        expected validate finalRanking
    }
    private infix fun List<RankEntry<TopsisAlternative>>.validate(actual: List<RankEntry<TopsisAlternative>>) {
        val assertions = zip(actual).mapIndexed { i, (e, a) ->
            Executable {
                assertAll("position $i",
                        { assertEquals(a.alternative.name, e.alternative.name) },
                        { assertEquals(a.value, e.value, 1e-2) }
                )
            }
        }
        assertAll(listOf(Executable { assertEquals(size, actual.size) }) + assertions)
    }
}