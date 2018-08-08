package pl.poznan.put.xmcda;

import org.xmcda.ProgramExecutionResult;
import org.xmcda.ProgramExecutionResult.Status;
import org.xmcda.XMCDA;
import org.xmcda.converters.v2_v3.XMCDAConverter;
import org.xmcda.parsers.xml.xmcda_v2.XMCDAParser;
import org.xml.sax.SAXException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Copyright Sébastien Bigaret, Patrick Meyer, 2016
 *
 * This software is an implementation in Java of the weighted sum, using the
 * XMCDA-java library.
 *
 * It is licenced under the European Union Public Licence (EUPL) v1.1.
 *
 * You'll find in this directory the English version and the French version of
 * the licence.
 *
 * The EUPL is available in 22 official languages of the European Union;
 * for more information about the EUPL, please refer to its website:
 * https://joinup.ec.europa.eu/community/eupl/home
 */
public class Utils
{
    public enum XMCDA_VERSION
    {
        v2, v3
    }
    /**
     * Gathers the arguments for the command line: input and output directories.
     * @see #parseCmdLineArguments(String[])
     */
    public static class Arguments
    {
        public String inputDirectory;
        public String outputDirectory;
    }
    /**
     * Raised when the command line is invalid
     *
     * @see #parseCmdLineArguments(String[])
     */
    public static class InvalidCommandLineException extends Exception
    {
        private static final long serialVersionUID = 3991185595688176975L;
        public InvalidCommandLineException(String message)
        {
            super(message);
        }
    }
    /**
     * Parses the command-line and search for the input directory (options {@code -i} or {@code --input-directory})
     * and for the output directory (options {@code -o} or {@code --output-directory}).
     *
     * @param args the arguments of the command-line (its length must be equal to 4).
     * @return an {@link Arguments argument} with non-null fields {@code inputDirectory} and {@code outputDirectory}
     * @throws InvalidCommandLineException in one or both input/output directories are not present in the command line.
     */
    public static Arguments parseCmdLineArguments(String[] args) throws InvalidCommandLineException
    {
        // Let's make it dead simple
        if ( args.length != 4 )
            throw new InvalidCommandLineException("Invalid number of arguments");
        Arguments arguments = new Arguments();
        for ( int index = 0; index <= 2; index += 2 )
        {
            String arg = args[index];
            if ( "-i".equals(arg) || "--input-directory".equals(arg) )
                arguments.inputDirectory = args[index + 1];
            else if ( "-o".equals(arg) || "--output-directory".equals(arg) )
                arguments.outputDirectory = args[index + 1];
        }
        if ( arguments.inputDirectory == null || arguments.outputDirectory == null )
            throw new InvalidCommandLineException("Missing parameters");
        return arguments;
    }
    public static void loadXMCDAv3(XMCDA xmcda, final File file, boolean mandatory,
                                   ProgramExecutionResult x_execution_results, String ... load_tags)
    {
        final org.xmcda.parsers.xml.xmcda_v3.XMCDAParser parser = new org.xmcda.parsers.xml.xmcda_v3.XMCDAParser();
        final String baseFilename = file.getName();
        if ( ! file.exists())
        {
            if ( mandatory )
            {
                x_execution_results.addError("Could not find the mandatory file " + baseFilename);
                return;
            }
            else
                // x_execution_results.addInfo("Optional file %s absent" % basefilename)
                return;
        }
        try
        {
            parser.readXMCDA(xmcda, file, load_tags);
        }
        catch (Throwable throwable)
        {
            final String msg = String.format("Unable to read & parse the file %s, reason: ", baseFilename);
            x_execution_results.addError(getMessage(msg, throwable));
        }
    }
    public static void loadXMCDAv2(org.xmcda.v2.XMCDA xmcda_v2, File file, boolean mandatory,
                                   ProgramExecutionResult x_execution_results, String ... load_tags)
    {
        final String baseFilename = file.getName();
        if ( ! file.exists())
        {
            if ( mandatory )
                x_execution_results.addError("Could not find the mandatory file " + baseFilename);
            return;
        }
        try
        {
            readXMCDAv2_and_update(xmcda_v2, file, load_tags);
        }
        catch (Throwable throwable)
        {
            final String msg = String.format("Unable to read & parse the file %s, reason: ", baseFilename);
            x_execution_results.addError(getMessage(msg, throwable));
        }
    }
    public static void readXMCDAv2_and_update(org.xmcda.v2.XMCDA xmcda_v2, File file, String... load_tags)
            throws IOException, JAXBException, SAXException
    {
        final org.xmcda.v2.XMCDA new_xmcda = XMCDAParser.readXMCDA(file, load_tags);
        final List<JAXBElement<?>> new_content = new_xmcda.getProjectReferenceOrMethodMessagesOrMethodParameters();
        xmcda_v2.getProjectReferenceOrMethodMessagesOrMethodParameters().addAll(new_content);
    }
    /**
     * Simply returns {@link Throwable#getMessage() throwable.getMessage()}, or {@code "unknown"} if it is @{code null}.
     *
     * @param throwable a non-null {@link Throwable}
     * @return the throwable's message, or "unknown" if it is null
     */
    static String getMessage(Throwable throwable)
    {
        if ( throwable.getMessage() != null )
            return throwable.getMessage();
        // when handling XMCDA v2 files, errors may be embedded in a JAXBException
        if ( throwable.getCause() != null && throwable.getCause().getMessage() != null )
            return throwable.getCause().getMessage();
        return "unknown";
    }
    /**
     * Simply return the provided String with {@link #getMessage(Throwable)}.
     *
     * @param throwable
     * @return the concatenated String
     */
    static String getMessage(String message, Throwable throwable)
    {
        return message + getMessage(throwable);
    }
    /**
     * Writes the XMCDA file containing the information provided to build the XMCDA tag "{@code programExecutionResult}"
     * in XMCDA v3, or "{@code methodMessages}" in XMCDA v2.x.
     *
     * @param prgExecResultsFile the file to write
     * @param errors             a {@link ProgramExecutionResult} object
     * @param xmcdaVersion       indicates which {@link XMCDA_VERSION} to use when writing the file
     */
    public static void writeProgramExecutionResults(File prgExecResultsFile, ProgramExecutionResult errors,
                                                    XMCDA_VERSION xmcdaVersion)
            throws Throwable
    {
        org.xmcda.parsers.xml.xmcda_v3.XMCDAParser parser = new org.xmcda.parsers.xml.xmcda_v3.XMCDAParser();
        XMCDA prgExecResults = new XMCDA();
        prgExecResults.programExecutionResultsList.add(errors);
        switch (xmcdaVersion)
        {
            case v3:
                parser.writeXMCDA(prgExecResults, prgExecResultsFile, "programExecutionResult");
                break;
            case v2:
                org.xmcda.v2.XMCDA xmcda_v2 = XMCDAConverter.convertTo_v2(prgExecResults);
                XMCDAParser.writeXMCDA(xmcda_v2, prgExecResultsFile, "methodMessages");
                break;
            default:
                // in case the enum has some more values in the future and the new ones have not been added here
                throw new IllegalArgumentException("Unhandled XMCDA version " + xmcdaVersion.toString());
        }
    }
    /**
     * Calls {@link #writeProgramExecutionResults(File, ProgramExecutionResult, XMCDA_VERSION)} then exits. The return status code
     * is {@link Status#exitStatus() errors.getStatus().getExitStatus}, or {@link Status#ERROR}'s {@code exitStatus()} in case
     * of failure when writing the execution results.
     *
     * @param prgExecResultsFile
     * @param errors
     * @param xmcdaVersion
     */
    public static void writeProgramExecutionResultsAndExit(File prgExecResultsFile, ProgramExecutionResult errors,
                                                           XMCDA_VERSION xmcdaVersion)
    {
        try
        {
            writeProgramExecutionResults(prgExecResultsFile, errors, xmcdaVersion);
        }
        catch (Throwable t)
        {
            // Last resort, print something on the stderr and exit
            // We choose here not to clean up the file, in case some valuable information were successfully written
            // before a throwable is raised.
            System.err.println(getMessage("Could not write messages.xml, reason: ", t));
            System.exit(Status.ERROR.exitStatus());
        }
        System.exit(errors.getStatus().exitStatus());
    }
}
