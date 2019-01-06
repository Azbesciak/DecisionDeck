package pl.poznan.put.topsis

import pl.poznan.put.xmcda.XMCDAv2Client


object NormalizationWeightingCliXMCDAv2 : XMCDAv2Client<TopsisInputs, WeightedNormalizedAlternatives>() {
    override val files = v2Base + weightsFile
    override val manager = weightedNormComputationManager

    @JvmStatic
    fun main(args: Array<String>) = run(args)
}

