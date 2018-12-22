package pl.poznan.put.xmcda

import org.xmcda.v2.XMCDA

class XMCDA2Reader(
        directoryPath: String,
        vararg files: XmcdaMapping
) : XmcdaReader<XMCDA>(directoryPath, XMCDA(), *files) {
    override fun FileTag.processFile() {
        loadXMCDAv2(xmcda, file, true, executionResult, tag)
    }
}