package pl.poznan.put.ahp

import org.xmcda.ProgramExecutionResult
import org.xmcda.converters.v2_v3.XMCDAConverter
import org.xmcda.v2.XMCDA
import pl.poznan.put.xmcda.Utils
import java.io.File

class XMCDA2Reader(
        private val directoryPath: String,
        private val files: List<String>
) {
    private val xmcda = XMCDA()
    val executionResult = ProgramExecutionResult()

    fun read(): org.xmcda.XMCDA {
        files.map { File("$directoryPath/$it.xml") }
                .forEach { Utils.loadXMCDAv2(xmcda, it, true, executionResult) }
        return XMCDAConverter.convertTo_v3(xmcda)
    }
}