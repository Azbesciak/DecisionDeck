package pl.poznan.put.xmcda

import org.xmcda.XMCDA

interface OutputsHandler<T> {
    /**
     * Returns the xmcda v3 tag for a given output
     *
     * @param outputName the output's name
     * @return the associated XMCDA v2 tag
     * @throws NullPointerException     if outputName is null
     * @throws IllegalArgumentException if outputName is not known
     */
    infix fun xmcdaV3Tag(outputName: String): String

    /**
     * Returns the xmcda v2 tag for a given output
     *
     * @param outputName the output's name
     * @return the associated XMCDA v2 tag
     * @throws NullPointerException     if outputName is null
     * @throws IllegalArgumentException if outputName is not known
     */
    infix fun xmcdaV2Tag(outputName: String): String

    /**
     * Converts the results of the computation step into XMCDA objects.
     *
     * @param values
     * @return a map with keys being xmcda objects' names and values their corresponding XMCDA object
     */
    infix fun convert(values: T): Map<String, XMCDA>
}