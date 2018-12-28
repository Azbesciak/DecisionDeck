package pl.poznan.put.topsis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll


class IdealAlternativeTopsisTest {
    // https://www.slideshare.net/pranavmishra22/topsis-a-multicriteria-decision-making-approach
    @Test
    fun test() {
        val criteria = listOf(
                Criterion("Entertainment", 4.0, CriteriaType.PROFIT),
                Criterion("Facilities", 2.0, CriteriaType.PROFIT),
                Criterion("TravelCost", 6.0, CriteriaType.COST),
                Criterion("Accommodation", 8.0, CriteriaType.COST))
        val alternatives = listOf(
                TopsisAlternative("Hogwarts", criteria.zip(listOf(9.0, 7.0, 6.0, 7.0)).toMap()),
                TopsisAlternative("Hogsmeade", criteria.zip(listOf(8.0, 7.0, 9.0, 6.0)).toMap()),
                TopsisAlternative("Azkaban", criteria.zip(listOf(7.0, 8.0, 6.0, 6.0)).toMap())
        )
        IdealAlternativeCalculator(alternatives, criteria).calculate().run {
            assertAll(
                    { assertEquals(criteria.zip(listOf(9.0, 8.0, 6.0, 6.0)).toMap(), positive.criteriaValues) },
                    { assertEquals(criteria.zip(listOf(7.0, 7.0, 9.0, 7.0)).toMap(), negative.criteriaValues) }
            )
        }
    }
}