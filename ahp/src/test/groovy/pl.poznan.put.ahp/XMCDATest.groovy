package pl.poznan.put.ahp

import org.xmcda.ProgramExecutionResult
import org.xmcda.v2.XMCDA
import pl.poznan.put.xmcda.Utils
import spock.lang.Specification

class XMCDATest extends Specification {
    def "should load tests for fruits"() {
        given:
        def path = "tests/in1"
        def xmcda = new XMCDA()
        def results = new ProgramExecutionResult()
        when:
        ["preference", "hierarchy", "alternatives"]
                .collect { new File("$path/${it}.xml") }
                .forEach { Utils.loadXMCDAv2(xmcda, it, true, results) }
        then:
        xmcda.projectReferenceOrMethodMessagesOrMethodParameters.size() == 5
        xmcda.projectReferenceOrMethodMessagesOrMethodParameters
                .findAll { ("alternativesComparisons" == it.name.localPart)}
                .size() == 2
        results.isOk()
    }
}
