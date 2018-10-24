package pl.poznan.put.xmcda

import pl.poznan.put.xmcda.Utils.*

object XmcdaLoaderBase {
    inline fun load(args: Array<String>,
             onV2: (Array<String>) -> Unit,
             onV3: (Array<String>) -> Unit
    ) {
        val argsList = args.toMutableList()
        when {
            argsList.remove("--v2") -> onV2(argsList.toTypedArray())
            argsList.remove("--v3") -> onV3(argsList.toTypedArray())
            else -> throw InvalidCommandLineException("missing mandatory option --v2 or --v3")
        }
    }
}