package pl.poznan.put.topsis

import pl.poznan.put.xmcda.InvalidCommandLineException
import pl.poznan.put.xmcda.XMCDAv3Client

/**
 *
 */
object DistanceRankingCalculatorCliXMCDAv3 : XMCDAv3Client<DistanceCalculatorInputs, TopsisRanking>() {
    override val files = v3Base + idealAlts
    override val manager = distanceCalcComputationManager

    @Throws(InvalidCommandLineException::class)
    @JvmStatic
    fun main(args: Array<String>) = run(args)
}
