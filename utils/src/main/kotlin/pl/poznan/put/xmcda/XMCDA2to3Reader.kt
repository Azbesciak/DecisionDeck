package pl.poznan.put.xmcda

import org.xmcda.converters.v2_v3.XMCDAConverter
import org.xmcda.v2.XMCDA

class XMCDA2to3Reader(
        directoryPath: String,
        vararg files: XmcdaMapping
) : XmcdaReader<org.xmcda.XMCDA>(directoryPath, org.xmcda.XMCDA(), *files) {
    override fun FileTag.processFile() {
        val v2 = XMCDA()
        loadXMCDAv2(v2, file, true, executionResult, tag)
        tryExecute(
                task = { XMCDAConverter.convertTo_v3(v2, xmcda) },
                onError = { "Could not convert ${file.name} to XMCDA v3: $it" }
        )
    }
}
