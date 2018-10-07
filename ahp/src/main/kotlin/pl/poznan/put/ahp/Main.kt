package pl.poznan.put.ahp

import pl.poznan.put.xmcda.Utils

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val parsed = Utils.parseCmdLineArguments(args)
        val xmcdA2Reader = XMCDA2Reader(parsed.inputDirectory, listOf("alternatives", "alternativesMatrix"))
        xmcdA2Reader.read()

        println(xmcdA2Reader.executionResult)
    }

}