package pl.poznan.put.topsis

import pl.poznan.put.xmcda.XmcdaLoaderBase

object IdealAlternativesCLI {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        XmcdaLoaderBase.load(args, IdealAlternativesCliXMCDAv2::main, IdealAlternativesCliXMCDAv3::main)
    }
}
