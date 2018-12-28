package pl.poznan.put.topsis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll


class WeightedNormalizedTopsisTest {
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

        val expected = listOf(
                TopsisAlternative("Hogwarts", criteria.zip(listOf(2.5846, 1.0999, 2.9104, 5.0909)).toMap()),
                TopsisAlternative("Hogsmeade", criteria.zip(listOf(2.2975, 1.0999, 4.3656, 4.3636)).toMap()),
                TopsisAlternative("Azkaban", criteria.zip(listOf(2.0103, 1.2571, 2.9104, 4.3636)).toMap())
        )
        WeightedNormalizedCalculator(alternatives, criteria).calculate().run {
            val tests = expected.zip(this.alternatives)
                    .map { (exp, act) -> createAltsValidation(criteria, act, exp) }
                    .toTypedArray()
            assertAll(*tests)
        }
    }

    private fun createAltsValidation(criteria: List<Criterion>, act: TopsisAlternative, exp: TopsisAlternative) =
            {
                assertAll(
                        { assertEquals(criteria.toSet(), act.criteriaValues.keys) },
                        *exp.criteriaValues
                                .map { (c, v) -> createAltOnCriterionValidation(act, c, v) }
                                .toTypedArray()
                )
            }

    private fun createAltOnCriterionValidation(act: TopsisAlternative, c: Criterion, v: Double) = {
        val actualValue = act.criteriaValues[c]
        assertNotNull(actualValue) { "no criterion value for $act on criterion ${c.name}" }
        assertEquals(v, actualValue!!, 1e-4) { "invalid ${c.name} criterion value" }
    }
}