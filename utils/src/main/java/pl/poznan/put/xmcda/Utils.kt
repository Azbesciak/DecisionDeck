@file:JvmName("Utils")

package pl.poznan.put.xmcda

import org.xmcda.ProgramExecutionResult
import org.xmcda.ProgramExecutionResult.Status
import org.xmcda.XMCDA
import org.xmcda.converters.v2_v3.XMCDAConverter
import org.xmcda.parsers.xml.xmcda_v2.XMCDAParser
import org.xml.sax.SAXException

import javax.xml.bind.JAXBElement
import javax.xml.bind.JAXBException
import java.io.File
import java.io.IOException
import java.util.Arrays

/**
 * Copyright Sébastien Bigaret, Patrick Meyer, 2016
 *
 *
 * This software is an implementation in Java of the weighted sum, using the
 * XMCDA-java library.
 *
 *
 * It is licenced under the European Union Public Licence (EUPL) v1.1.
 *
 *
 * You'll find in this directory the English version and the French version of
 * the licence.
 *
 *
 * The EUPL is available in 22 official languages of the European Union;
 * for more information about the EUPL, please refer to its website:
 * https://joinup.ec.europa.eu/community/eupl/home
 */
enum class XmcdaVersion private constructor(internal val versionFlag: String) {
    v2("--v2"), v3("--v3");

    override fun toString(): String {
        return "$name($versionFlag)"
    }
}

/**
 * Gathers the arguments for the command line: input and output directories.
 *
 * @see .parseCmdLineArguments
 */
data class Arguments(
        val inputDirectory: String,
        val outputDirectory: String
)

/**
 * Raised when the command line is invalid
 *
 * @see .parseCmdLineArguments
 */
class InvalidCommandLineException(message: String) : Exception(message) {
    companion object {
        private val serialVersionUID = 3991185595688176975L
    }
}

/**
 * Parses the command-line and search for the input directory (options `-i` or `--input-directory`)
 * and for the output directory (options `-o` or `--output-directory`).
 *
 * @param args the arguments of the command-line (its length must be equal to 4).
 * @return an [argument][Arguments] with non-null fields `inputDirectory` and `outputDirectory`
 * @throws InvalidCommandLineException in one or both input/output directories are not present in the command line.
 */
@Throws(InvalidCommandLineException::class)
fun parseCmdLineArguments(args: Array<String>): Arguments {
    // Let's make it dead simple
    if (args.size != 4)
        throw InvalidCommandLineException("Invalid number of arguments (required: 4, provided: " + args.size + ")")
    var inputDir: String? = null
    var outputDir: String? = null
    var index = 0
    while (index <= 2) {
        val arg = args[index]
        if ("-i" == arg || "--input-directory" == arg)
            inputDir = args[index + 1]
        else if ("-o" == arg || "--output-directory" == arg)
            outputDir = args[index + 1]
        index += 2
    }

    return Arguments(
            inputDir ?: throw InvalidCommandLineException("Missing input directory parameter"),
            outputDir ?: throw InvalidCommandLineException("Missing output directory parameter")
    )
}

@Throws(InvalidCommandLineException::class)
private fun getXmcdaVersion(versionString: String): XmcdaVersion {
    return Arrays.stream(XmcdaVersion.values())
            .filter { s -> s.versionFlag == versionString }
            .findFirst()
            .orElseThrow {
                InvalidCommandLineException(
                        "invalid XMCDA version flag: '" + versionString +
                                "', allowed are " + Arrays.toString(XmcdaVersion.values()))
            }
}

fun loadXMCDAv3(xmcda: XMCDA, file: File, mandatory: Boolean,
                x_execution_results: ProgramExecutionResult, vararg load_tags: String) {
    val parser = org.xmcda.parsers.xml.xmcda_v3.XMCDAParser()
    val baseFilename = file.name
    if (!file.exists()) {
        if (mandatory) {
            x_execution_results.addError("Could not find the mandatory file $baseFilename")
            return
        } else
        // x_execution_results.addInfo("Optional file %s absent" % basefilename)
            return
    }
    try {
        parser.readXMCDA(xmcda, file, *load_tags)
    } catch (throwable: Throwable) {
        val msg = String.format("Unable to read & parse the file %s, reason: ", baseFilename)
        x_execution_results.addError(getMessage(msg, throwable))
    }

}

fun loadXMCDAv2(xmcda_v2: org.xmcda.v2.XMCDA, file: File, mandatory: Boolean,
                x_execution_results: ProgramExecutionResult, vararg load_tags: String) {
    val baseFilename = file.name
    if (!file.exists()) {
        if (mandatory)
            x_execution_results.addError("Could not find the mandatory file $baseFilename")
        return
    }
    try {
        readXMCDAv2_and_update(xmcda_v2, file, *load_tags)
    } catch (throwable: Throwable) {
        val msg = String.format("Unable to read & parse the file %s, reason: ", baseFilename)
        x_execution_results.addError(getMessage(msg, throwable))
    }

}

@Throws(IOException::class, JAXBException::class, SAXException::class)
fun readXMCDAv2_and_update(xmcda_v2: org.xmcda.v2.XMCDA, file: File, vararg load_tags: String) {
    val new_xmcda = XMCDAParser.readXMCDA(file, *load_tags)
    val new_content = new_xmcda.projectReferenceOrMethodMessagesOrMethodParameters
    xmcda_v2.projectReferenceOrMethodMessagesOrMethodParameters.addAll(new_content)
}

/**
 * Simply returns [throwable.getMessage()][Throwable.getMessage], or `"unknown"` if it is @{code null}.
 *
 * @param throwable a non-null [Throwable]
 * @return the throwable's message, or "unknown" if it is null
 */
fun getMessage(throwable: Throwable) =
        throwable.message ?: throwable.cause?.message ?: "unknown"

operator fun ProgramExecutionResult.plusAssign(t: Throwable) {
    addError(getMessage("The calculation could not be performed, reason: ", t))
}


/**
 * Simply return the provided String with [.getMessage].
 *
 * @param throwable
 * @return the concatenated String
 */
fun getMessage(message: String, throwable: Throwable): String {
    return message + getMessage(throwable)
}

/**
 * Writes the XMCDA file containing the information provided to build the XMCDA tag "`programExecutionResult`"
 * in XMCDA v3, or "`methodMessages`" in XMCDA v2.x.
 *
 * @param prgExecResultsFile the file to write
 * @param errors             a [ProgramExecutionResult] object
 * @param xmcdaVersion       indicates which [XmcdaVersion] to use when writing the file
 */
@Throws(Throwable::class)
fun writeProgramExecutionResults(
        prgExecResultsFile: File, errors: ProgramExecutionResult,
        xmcdaVersion: XmcdaVersion) {
    val parser = org.xmcda.parsers.xml.xmcda_v3.XMCDAParser()
    val prgExecResults = XMCDA()
    prgExecResults.programExecutionResultsList.add(errors)
    when (xmcdaVersion) {
        XmcdaVersion.v3 -> parser.writeXMCDA(prgExecResults, prgExecResultsFile, "programExecutionResult")
        XmcdaVersion.v2 -> {
            val xmcda_v2 = XMCDAConverter.convertTo_v2(prgExecResults)
            XMCDAParser.writeXMCDA(xmcda_v2, prgExecResultsFile, "methodMessages")
        }
    }
}

/**
 * Calls [.writeProgramExecutionResults] then exits. The return status code
 * is [errors.getStatus().getExitStatus][Status.exitStatus], or [Status.ERROR]'s `exitStatus()` in case
 * of failure when writing the execution results.
 *
 * @param prgExecResultsFile
 * @param errors
 * @param xmcdaVersion
 */
fun writeProgramExecutionResultsAndExit(prgExecResultsFile: File, errors: ProgramExecutionResult,
                                        xmcdaVersion: XmcdaVersion) {
    try {
        writeProgramExecutionResults(prgExecResultsFile, errors, xmcdaVersion)
    } catch (t: Throwable) {
        // Last resort, print something on the stderr and exit
        // We choose here not to clean up the file, in case some valuable information were successfully written
        // before a throwable is raised.
        System.err.println(getMessage("Could not write messages.xml, reason: ", t))
        System.exit(Status.ERROR.exitStatus())
    }

    System.exit(errors.status.exitStatus())
}
