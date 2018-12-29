package pl.poznan.put.xmcda

import org.xmcda.ProgramExecutionResult
import java.io.File


data class XmcdaFile(val name: String, val mandatory: Boolean = true, val tag: String = name)

data class InitData(
        val inputDir: String,
        val outputDir: String,
        val programExecutionResult: ProgramExecutionResult,
        val execResFile: File
)

fun prepare(args: Array<String>): InitData {
    val params = parseCmdLineArguments(args)
    val indir = params.inputDirectory
    val outdir = params.outputDirectory
    val prgExecResultsFile = File(outdir, "messages.xml")
    prgExecResultsFile.parentFile.mkdirs()
    val executionResult = ProgramExecutionResult()
    return InitData(indir, outdir, executionResult, prgExecResultsFile)
}

data class ComputationManager<Input, Output>(
        private val inputsHandler: InputsHandler<Input>,
        private val outputsHandler: OutputsHandler<Output>,
        val compute: Input.() -> Output
) : InputsHandler<Input> by inputsHandler, OutputsHandler<Output> by outputsHandler