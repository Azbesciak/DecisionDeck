package pl.poznan.put.topsis

import org.xmcda.XMCDA
import pl.poznan.put.xmcda.InputsHandler

object NormalizationWeightingInputsHandler : InputsHandler<TopsisInputs> {
    private val handler = singleInputTopsisHandler(CriteriaWeightsFactory)
    /**
     * This class contains every element which are needed to compute
     * TOPSIS.<br></br>
     * It is populated by [TopsisInputsHandler.checkAndExtractInputs].
     */
    /**
     * @param xmcda
     * @return
     */
    override fun checkAndExtractInputs(xmcda: XMCDA) = handler.checkAndExtractInputs(xmcda)
}