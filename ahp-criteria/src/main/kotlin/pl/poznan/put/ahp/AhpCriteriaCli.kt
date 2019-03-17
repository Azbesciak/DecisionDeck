package pl.poznan.put.ahp

import pl.poznan.put.xmcda.XmcdaLoaderBase

object AhpCriteriaCli {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        XmcdaLoaderBase.load(args, AhpCriteriaCliXMCDAv2::main, AhpCriteriaCliXMCDAv3::main)
    }
}
