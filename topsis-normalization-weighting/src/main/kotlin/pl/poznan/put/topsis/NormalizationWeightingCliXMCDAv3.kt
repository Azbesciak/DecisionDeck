package pl.poznan.put.topsis

import pl.poznan.put.xmcda.InvalidCommandLineException
import pl.poznan.put.xmcda.XMCDAv3Client

object NormalizationWeightingCliXMCDAv3 : XMCDAv3Client<TopsisInputs, WeightedNormalizedAlternatives>() {
    override val files = v3Base + weightsFile
    override val manager = weightedNormComputationManager

    @Throws(InvalidCommandLineException::class)
    @JvmStatic
    fun main(args: Array<String>) = run(args)
}
