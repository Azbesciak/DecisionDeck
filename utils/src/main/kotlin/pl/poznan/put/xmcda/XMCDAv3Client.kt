package pl.poznan.put.xmcda

import org.xmcda.ProgramExecutionResult
import org.xmcda.XMCDA
import org.xmcda.parsers.xml.xmcda_v3.XMCDAParser
import java.io.File
import kotlin.system.exitProcess

abstract class XMCDAv3Client<Input, Output>{
    @Throws(InvalidCommandLineException::class)
    protected fun run(args: Array<String>) {
        val (indir, outdir, executionResult, prgExecResults) = prepare(args)
        val xmcda = loadXmcda(indir, executionResult, prgExecResults)
        val inputs = parseInput(xmcda, prgExecResults, executionResult)
        val results = calculateResults(inputs, executionResult, prgExecResults)
        val xmcdaResults = manager.convert(results)
        writeResults(xmcdaResults, outdir, executionResult, prgExecResults)
    }
    protected abstract val files: Array<XmcdaFile>
    protected abstract val manager: ComputationManager<Input, Output>

    private fun writeResults(x_results: Map<String, XMCDA>, outdir: String?, executionResult: ProgramExecutionResult, prgExecResults: File) {
        val parser = XMCDAParser()
        x_results.keys.forEach { key ->
            val outputFile = File(outdir, "$key.xml")
            try {
                parser.writeXMCDA(x_results[key], outputFile, manager.xmcdaV3Tag(key))
            } catch (throwable: Throwable) {
                val err = "Error while writing $key.xml, reason: "
                executionResult.addError(getMessage(err, throwable))
                outputFile.delete()
            }
        }
        writeProgramExecutionResultsAndExit(prgExecResults, executionResult, XmcdaVersion.v3)
    }

    private fun parseInput(xmcda: XMCDA, prgExecResults: File, executionResult: ProgramExecutionResult) =
            try {
                manager.checkAndExtractInputs(xmcda)
            } catch (t: Throwable) {
                writeErrorMessageAndExit(t, "Could not parse inputs to program parameters, reason: ", prgExecResults, executionResult)
            }

    private fun calculateResults(inputs: Input, executionResult: ProgramExecutionResult, prgExecResults: File) =
            try {
                manager.compute(inputs)
            } catch (t: Throwable) {
                writeErrorMessageAndExit(t, "The calculation could not be performed, reason: ", prgExecResults, executionResult)
            }

    private fun writeErrorMessageAndExit(t: Throwable, message: String, prgExecResults: File, executionResult: ProgramExecutionResult): Nothing {
        executionResult.addError(getMessage(message, t))
        writeProgramExecutionResultsAndExit(prgExecResults, executionResult, XmcdaVersion.v3)
        exitProcess(-1)
    }

    private fun loadXmcda(indir: String, executionResult: ProgramExecutionResult, prgExecResults: File): XMCDA {
        val xmcda = XMCDA()
        files.forEach {
            loadXMCDAv3(xmcda, File(indir, "${it.name}.xml"), it.mandatory, executionResult, it.tag)
        }
        // We have problems with the inputs, its time to stop
        if (!(executionResult.isOk || executionResult.isWarning)) {
            writeProgramExecutionResultsAndExit(prgExecResults, executionResult, XmcdaVersion.v3)
        }
        return xmcda
    }


}
