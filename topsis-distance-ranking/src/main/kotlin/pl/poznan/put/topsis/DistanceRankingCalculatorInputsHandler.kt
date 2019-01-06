package pl.poznan.put.topsis

import org.xmcda.XMCDA
import pl.poznan.put.xmcda.InputsHandler

object DistanceRankingCalculatorInputsHandler : InputsHandler<DistanceCalculatorInputs> {

    private val names = listOf("main", "positive ideal", "negative ideal")
    /**
     * This class contains every element which are needed to compute
     * TOPSIS.<br></br>
     * It is populated by [TopsisInputsHandler.checkAndExtractInputs].
     */
    /**
     * @param xmcda
     * @return
     */

    override fun checkAndExtractInputs(xmcda: XMCDA) =
            TopsisInputsHandler(names, NoWeightFactory) { alt, criteria ->
                alt[1] validateIdealAlt names[1]
                alt[2] validateIdealAlt names[2]
                val idealAlternatives = IdealAlternatives(
                        positive = alt[1].first(),
                        negative = alt[2].first()
                )
                DistanceCalculatorInputs(alt.first(), criteria, idealAlternatives)

            }.checkAndExtractInputs(xmcda)

    private infix fun List<TopsisAlternative>.validateIdealAlt(name: String) =
            require(size == 1) { "require exactly one $name ideal alternative" }
}

data class DistanceCalculatorInputs(
        val alternatives: List<TopsisAlternative>,
        val criteria: List<Criterion>,
        val idealAlternatives: IdealAlternatives
)
