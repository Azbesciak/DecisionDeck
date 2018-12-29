package pl.poznan.put.topsis

import pl.poznan.put.xmcda.XMCDAv2Client


object DistanceRankingCalculatorCliXMCDAv2 : XMCDAv2Client<DistanceCalculatorInputs, TopsisRanking>() {
    override val manager = distanceCalcComputationManager
    override val files = v2Base + idealAlts

    @JvmStatic
    fun main(args: Array<String>) = run(args)
}

