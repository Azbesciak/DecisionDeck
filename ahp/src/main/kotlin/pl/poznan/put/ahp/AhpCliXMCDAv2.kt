package pl.poznan.put.ahp

import pl.poznan.put.xmcda.XMCDAv2Client

internal object AhpCliXMCDAv2 : XMCDAv2Client<AhpResult, AhpRanking>() {
    override val files = v2files
    override val manager = ahpComputationManager

    @JvmStatic
    fun main(args: Array<String>) = run(args)
}
