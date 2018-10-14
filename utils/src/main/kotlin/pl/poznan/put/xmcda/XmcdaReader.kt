package pl.poznan.put.xmcda

import org.xmcda.ProgramExecutionResult
import org.xmcda.v2.XMCDA
import pl.poznan.put.xmcda.Utils.*
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

abstract class XmcdaReader<T>(
        private val directoryPath: String,
        protected val xmcda: T,
        private vararg val files: XmcdaMapping
        ) {
    private val readed = AtomicBoolean(false)
    protected val executionResult = ProgramExecutionResult()
    fun read(): ReadResult<T> {
        processFiles()
        return ReadResult(executionResult, xmcda)
    }

    private fun processFiles() {
        if (readed.getAndSet(true)) {
            executionResult.addError("reading was already started")
            return
        }
        files.flatMap { it.tags.map { tag -> FileTag(tag, File("$directoryPath/${it.fileName}.xml")) } }
                .forEach {it.processFile() }
    }

    protected abstract fun FileTag.processFile()


    protected fun tryExecute(task: () -> Unit, onError: (errorMessage: String) -> String) {
        try {
            task()
        } catch (t: Throwable) {
            executionResult.addError(onError(getMessage(t)))
        }
    }
}

data class FileTag(
        val tag: String,
        val file: File
)

data class XmcdaMapping(
        val fileName: String,
        val tags: List<String> = listOf(fileName)
)

data class ReadResult<T>(
        val executionResult: ProgramExecutionResult,
        val xmcda: T
)

val XMCDA.tagValues get() = projectReferenceOrMethodMessagesOrMethodParameters

fun XMCDA.getTags(tag: String) = tagValues.filter { it.name.localPart == tag }
