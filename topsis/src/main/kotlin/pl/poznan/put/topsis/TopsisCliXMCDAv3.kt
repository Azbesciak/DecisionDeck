package pl.poznan.put.topsis

import pl.poznan.put.xmcda.XMCDAv3Client

object TopsisCliXMCDAv3 : XMCDAv3Client<TopsisInputs, TopsisRanking>() {
    override val files = v3Base + weightsFile
    override val manager = topsisComputationManager
    @JvmStatic
    fun main(args: Array<String>) = run(args)
}
