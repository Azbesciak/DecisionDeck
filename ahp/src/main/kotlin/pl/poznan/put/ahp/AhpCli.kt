package pl.poznan.put.ahp

import pl.poznan.put.xmcda.XmcdaLoaderBase

object AhpCli {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        XmcdaLoaderBase.load(args, AhpCliXMCDAv2::main, AhpCliXMCDAv3::main)
    }
}