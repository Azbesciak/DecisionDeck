package pl.poznan.put.topsis

import pl.poznan.put.xmcda.XMCDAv2Client


object TopsisCliXMCDAv2 : XMCDAv2Client<TopsisInputs, TopsisRanking>() {
    override val files = v2Base + weightsFile
    override val manager = topsisComputationManager

    @JvmStatic
    fun main(args: Array<String>) = run(args)

}

