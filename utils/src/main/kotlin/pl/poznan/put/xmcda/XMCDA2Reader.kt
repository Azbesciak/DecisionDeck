package pl.poznan.put.xmcda

import org.xmcda.ProgramExecutionResult
import org.xmcda.converters.v2_v3.XMCDAConverter
import org.xmcda.v2.XMCDA
import pl.poznan.put.xmcda.Utils.*
import java.io.File

class XMCDA2Reader(
        private val directoryPath: String,
        private vararg val files: XmcdaMapping
) {
    val executionResult = ProgramExecutionResult()

    fun read(): org.xmcda.XMCDA {
        val v3 = org.xmcda.XMCDA()
        files.flatMap { it.tags.map { tag -> tag to File("$directoryPath/${it.fileName}.xml") } }
                .forEach {(tag, file) -> extractTagFromFile(file, tag, v3)}
        return v3
    }

    private fun extractTagFromFile(file: File, tag: String, v3: org.xmcda.XMCDA) {
        val v2 = XMCDA()
        loadXMCDAv2(v2, file, true, executionResult, tag)
        try {
            XMCDAConverter.convertTo_v3(v2, v3)
        } catch (t: Throwable) {
            executionResult.addError("Could not convert ${file.name} to XMCDA v3: ${getMessage(t)}")
        }
    }
}

data class XmcdaMapping(
        val fileName: String,
        val tags: List<String> = listOf(fileName)
)
