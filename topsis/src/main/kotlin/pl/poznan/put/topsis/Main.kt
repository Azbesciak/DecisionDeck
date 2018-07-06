package pl.poznan.put.topsis

import pl.poznan.pl.xmcda.ParseUtils


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val parsed = ParseUtils.parseCmdLineArguments(args)
    }
}