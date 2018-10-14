package pl.poznan.put.xmcda

import org.xmcda.v2.XMCDA
import pl.poznan.put.xmcda.Utils.loadXMCDAv2

class XMCDA2Reader(
        directoryPath: String,
        vararg files: XmcdaMapping
) : XmcdaReader<XMCDA>(directoryPath, XMCDA(), *files) {
    override fun FileTag.processFile() {
        loadXMCDAv2(xmcda, file, true, executionResult, tag)
    }
}