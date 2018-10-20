package pl.poznan.put.topsis

import pl.poznan.put.xmcda.Utils
import pl.poznan.put.xmcda.XMCDA2to3Reader
import pl.poznan.put.xmcda.XmcdaMapping


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val parsed = Utils.parseCmdLineArguments(args)
        val reader = XMCDA2to3Reader(
                parsed.inputDirectory,
                XmcdaMapping("alternatives"),
                XmcdaMapping("criteria"),
                XmcdaMapping("weights", listOf("criteriaValues")),
                XmcdaMapping("performance")
        )
        val res = reader.read()
        println(res.xmcda)
    }
}