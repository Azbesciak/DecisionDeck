package pl.poznan.put.ahp

import pl.poznan.put.xmcda.Utils
import pl.poznan.put.xmcda.XMCDA2Reader
import pl.poznan.put.xmcda.XmcdaMapping

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val parsed = Utils.parseCmdLineArguments(args)
        val xmcdA2Reader = XMCDA2Reader(parsed.inputDirectory, XmcdaMapping("alternatives"), XmcdaMapping("alternativesMatrix"))
        xmcdA2Reader.read()

        println(xmcdA2Reader.executionResult)
    }

}