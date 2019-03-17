package pl.poznan.put.ahp

import pl.poznan.put.xmcda.XMCDAv2Client

internal object AhpCriteriaCliXMCDAv2 : XMCDAv2Client<AhpResult, AhpRanking>() {
    override val files = criteriaV2files
    override val manager = ahpCriteriaComputationManager

    @JvmStatic
    fun main(args: Array<String>) = run(args)
}
