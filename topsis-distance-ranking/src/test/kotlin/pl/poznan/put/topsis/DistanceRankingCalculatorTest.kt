package pl.poznan.put.topsis

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.function.Executable
import pl.poznan.put.xmcda.ranking.RankEntry


class DistanceRankingCalculatorTest {
    // https://www.slideshare.net/pranavmishra22/topsis-a-multicriteria-decision-making-approach
    @Test
    fun test() {
        val criteria = listOf(
                Criterion("Entertainment", 4.0, CriteriaType.PROFIT),
                Criterion("Facilities", 2.0, CriteriaType.PROFIT),
                Criterion("TravelCost", 6.0, CriteriaType.COST),
                Criterion("Accommodation", 8.0, CriteriaType.COST))
        val alternatives = listOf(
                TopsisAlternative("Hogwarts", criteria.zip(listOf(2.5846, 1.0999, 2.9104, 5.0909)).toMap()),
                TopsisAlternative("Hogsmeade", criteria.zip(listOf(2.2975, 1.0999, 4.3656, 4.3636)).toMap()),
                TopsisAlternative("Azkaban", criteria.zip(listOf(2.0103, 1.2571, 2.9104, 4.3636)).toMap())
        )
        val idealAlternatives = IdealAlternatives(
                positive = TopsisAlternative("positive", criteria.zip(listOf(2.5846, 1.2571, 2.9104, 4.3636)).toMap()),
                negative = TopsisAlternative("negative", criteria.zip(listOf(2.0103, 1.0999, 4.3656, 5.0909)).toMap())
        )
        val ranking = DistanceRankingCalculator(alternatives, criteria, idealAlternatives).calculate()
        val expected = listOf(
                TopsisRankEntry(alternatives[2], 0.74),
                TopsisRankEntry(alternatives[0], 0.68),
                TopsisRankEntry(alternatives[1], 0.34)
        )
        expected validate ranking

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