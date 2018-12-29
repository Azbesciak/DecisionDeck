package pl.poznan.put.topsis

import pl.poznan.put.xmcda.XMCDAv2Client


object IdealAlternativesCliXMCDAv2 : XMCDAv2Client<TopsisInputs, IdealAlternatives>() {
    override val files = v2Base
    override val manager = idealAlternativesComputationManager

    @JvmStatic
    fun main(args: Array<String>) = run(args)
}

