package pl.poznan.put.ahp

import pl.poznan.put.xmcda.XMCDA2to3Reader
import pl.poznan.put.xmcda.XmcdaMapping
import spock.lang.Specification

class XMCDATest extends Specification {
    def "should load tests for fruits"() {
        given:
        def path = "tests/in1.v2"
        def reader = new XMCDA2to3Reader(path,
                new XmcdaMapping("preference", ["preference"]),
                new XmcdaMapping("hierarchy", ["hierarchy"]),
                new XmcdaMapping("alternatives", ["alternatives"])
        )
        when:
        def result = reader.read()
        then:
        result.xmcda.alternatives.size() == 3
        result.xmcda.alternativesMatricesList.size() == 2
        result.executionResult.isOk()
    }
}
