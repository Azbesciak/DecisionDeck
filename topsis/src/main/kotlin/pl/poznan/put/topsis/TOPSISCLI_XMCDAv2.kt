package pl.poznan.put.topsis


import org.xmcda.ProgramExecutionResult
import org.xmcda.XMCDA
import org.xmcda.converters.v2_v3.XMCDAConverter
import org.xmcda.parsers.xml.xmcda_v2.XMCDAParser
import pl.poznan.put.xmcda.Utils
import pl.poznan.put.xmcda.Utils.loadXMCDAv2

import java.io.File
import kotlin.system.exitProcess

/**
 *
 */
object TOPSISCLI_XMCDAv2 {
    /**
     * @param args
     */
    @Throws(Utils.InvalidCommandLineException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Parsing the options
        val params = Utils.parseCmdLineArguments(args)

        val indir = params.inputDirectory
        val outdir = params.outputDirectory

        val prgExecResultsFile = File(outdir, "messages.xml")
        prgExecResultsFile.parentFile.mkdirs()

        val executionResult = ProgramExecutionResult()
        val xmcda: XMCDA
        val xmcda_v2 = loadXmcdaV2(indir, executionResult)
        if (!(executionResult.isOk || executionResult.isWarning)) {
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XmcdaVersion.v2)
        }
        try {
            xmcda = XMCDAConverter.convertTo_v3(xmcda_v2)
        } catch (t: Throwable) {
            executionResult.addError(Utils.getMessage("Could not convert inputs to XMCDA v3, reason: ", t))
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XmcdaVersion.v2)
            return  // just to make the compiler happy about xmcda being final and potentially not initialized below
        }
        val inputs: Inputs
        try {
            inputs = TopsisInputsHandler.checkAndExtractInputs(xmcda)
        } catch (t: Throwable) {
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XmcdaVersion.v2)
            exitProcess(-1)
        }
        val results: TopsisRanking
        try {
            results = Topsis(inputs.alternatives, inputs.criteria).calculate()
        } catch (t: Throwable) {
            executionResult.addError(Utils.getMessage("The calculation could not be performed, reason: ", t))
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XmcdaVersion.v2)
            return  // just to make the compiler happy about results being final and potentially not initialized below
        }

        // Fine, now let's put the results into XMCDA structures
        val x_results = TopsisOutputsHandler.convert(results)

        // and finally, write them onto the appropriate files
        var results_v2: org.xmcda.v2.XMCDA?
        for (outputName in x_results.keys) {
            val outputFile = File(outdir, "$outputName.xml")
            try {
                results_v2 = XMCDAConverter.convertTo_v2(x_results[outputName])
                if (results_v2 == null)
                    throw IllegalStateException("Conversion from v3 to v2 returned a null value")
            } catch (t: Throwable) {
                val err = "Could not convert $outputName into XMCDA_v2, reason: "
                executionResult.addError(Utils.getMessage(err, t))
                continue // try to convert & save as much as we can
            }

            try {
                XMCDAParser.writeXMCDA(results_v2, outputFile, TopsisOutputsHandler.xmcdaV2Tag(outputName))
            } catch (t: Throwable) {
                val err = "Error while writing $outputName.xml, reason: "
                executionResult.addError(Utils.getMessage(err, t))
                outputFile.delete()
            }

        }

        Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XmcdaVersion.v2)
    }

    private fun loadXmcdaV2(indir: String?, executionResult: ProgramExecutionResult): org.xmcda.v2.XMCDA {
        val xmcda_v2 = org.xmcda.v2.XMCDA()
        loadXMCDAv2(xmcda_v2, File(indir, "alternatives.xml"), true,
                executionResult, "alternatives")
        loadXMCDAv2(xmcda_v2, File(indir, "criteria.xml"), true,
                executionResult, "criteria")
        loadXMCDAv2(xmcda_v2, File(indir, "performance.xml"), true,
                executionResult, "performanceTable")
        loadXMCDAv2(xmcda_v2, File(indir, "weights.xml"), true,
                executionResult, "criteriaValues")
        return xmcda_v2
    }

}

