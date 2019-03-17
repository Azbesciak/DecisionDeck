package pl.poznan.put.ahp

import pl.poznan.put.xmcda.XMCDAv3Client

internal object AhpCriteriaCliXMCDAv3 : XMCDAv3Client<AhpResult, AhpRanking>() {
    override val files = criteriaV3files
    override val manager = ahpCriteriaComputationManager

    @JvmStatic
    fun main(args: Array<String>) = run(args)
}
