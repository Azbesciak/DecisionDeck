package pl.poznan.put.topsis

import pl.poznan.put.xmcda.Utils
import pl.poznan.put.xmcda.XMCDA2Reader
import pl.poznan.put.xmcda.XmcdaMapping


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val parsed = Utils.parseCmdLineArguments(args)
        val reader = XMCDA2Reader(
                parsed.inputDirectory,
                XmcdaMapping("alternatives"),
                XmcdaMapping("criteria", listOf("criteria", "criteriaScales")),
                XmcdaMapping("performanceTable")
        )
        val xmcda = reader.read()
        println(xmcda)
    }
}