package pl.poznan.put.xmcda

import org.xmcda.XMCDA

interface InputsHandler<T> {
    /**
     * @param xmcda
     * @param xmcda_exec_results
     * @return
     */
    fun checkAndExtractInputs(xmcda: XMCDA): T
}