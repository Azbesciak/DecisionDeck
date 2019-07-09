package pl.poznan.put.xmcda

import org.xmcda.ProgramExecutionResult
import org.xmcda.converters.v2_v3.XMCDAConverter
import org.xmcda.parsers.xml.xmcda_v2.XMCDAParser
import org.xmcda.v2.XMCDA
import java.io.File
import kotlin.system.exitProcess

abstract class XMCDAv2Client<Input, Output> {
    protected abstract val files: Array<XmcdaFile>
    protected abstract val manager: ComputationManager<Input, Output>

    @Throws(InvalidCommandLineException::class)
    fun run(args: Array<String>) {
        // Parsing the options
        val (indir, outdir, executionResult, prgExecResultsFile) = prepare(args)
        val xmcda_v2 = loadXmcdaV2(indir, executionResult, prgExecResultsFile)

        val xmcda = convertV2ToV3(xmcda_v2, executionResult, prgExecResultsFile)
        val inputs = extractInputs(xmcda, prgExecResultsFile, executionResult)
        val results = computeInputs(inputs, executionResult, prgExecResultsFile)
        val v3Results = manager.convert(results)
        writeResults(v3Results, outdir, executionResult, prgExecResultsFile)
    }

    private fun writeResults(v3Results: Map<String, org.xmcda.XMCDA>, outdir: String, executionResult: ProgramExecutionResult, prgExecResultsFile: File) {
        for (outputName in v3Results.keys) {
            val outputFile = File(outdir, "$outputName.xml")
            val v2Results = try {
                requireNotNull(XMCDAConverter.convertTo_v2(v3Results[outputName])) {
                    "Conversion from v3 to v2 returned a null value"
                }
            } catch (t: Throwable) {
                val err = "Could not convert $outputName into XMCDA_v2, reason: "
                executionResult.addError(getMessage(err, t))
                continue // try to convert & save as much as we can
            }

            try {
                XMCDAParser.writeXMCDA(v2Results, outputFile, manager.xmcdaV2Tag(outputName))
            } catch (t: Throwable) {
                val err = "Error while writing $outputName.xml, reason: "
                executionResult.addError(getMessage(err, t))
                outputFile.delete()
            }
        }
        writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, XmcdaVersion.v2)
    }

    private fun computeInputs(inputs: Input, executionResult: ProgramExecutionResult, prgExecResultsFile: File): Output {
        return try {
            manager.compute(inputs)
        } catch (w: WarnException) {
            executionResult.addWarning(w.message)
            w.result as Output
        }
        catch (t: Throwable) {
            executionResult.addError(getMessage("The calculation could not be performed, reason: ", t))
            writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, XmcdaVersion.v2)
            exitProcess(-1)
        }
    }

    private fun extractInputs(xmcda: org.xmcda.XMCDA, prgExecResultsFile: File, executionResult: ProgramExecutionResult): Input {
        return try {
            manager.checkAndExtractInputs(xmcda)
        } catch (t: Throwable) {
            executionResult.addError(getMessage("Could not parse inputs to program parameters, reason: ", t))
            writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, XmcdaVersion.v2)
            exitProcess(-1)
        }
    }

    private fun convertV2ToV3(xmcda_v2: XMCDA, executionResult: ProgramExecutionResult, prgExecResultsFile: File) =
            try {
                XMCDAConverter.convertTo_v3(xmcda_v2)
            } catch (t: Throwable) {
                executionResult.addError(getMessage("Could not convert inputs to XMCDA v3, reason: ", t))
                writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, XmcdaVersion.v2)
                exitProcess(-1)
            }


    private fun loadXmcdaV2(indir: String?, executionResult: ProgramExecutionResult, prgExecResultsFile: File): org.xmcda.v2.XMCDA {
        val xmcda_v2 = org.xmcda.v2.XMCDA()
        files.forEach {
            loadXMCDAv2(xmcda_v2, File(indir, "${it.name}.xml"), it.mandatory, executionResult, it.tag)
        }
        if (!(executionResult.isOk || executionResult.isWarning)) {
            writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, XmcdaVersion.v2)
        }
        return xmcda_v2
    }

}
