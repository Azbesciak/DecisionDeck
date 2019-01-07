package pl.poznan.put.topsis

import pl.poznan.put.xmcda.XmcdaLoaderBase

object TopsisCli {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        XmcdaLoaderBase.load(args, TopsisCliXMCDAv2::main, TopsisCliXMCDAv3::main)
    }
}
