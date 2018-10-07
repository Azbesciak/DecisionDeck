package pl.poznan.put.ahp


import spock.lang.Specification

class XMCDATest extends Specification {
    def "should load tests for fruits"() {
        given:
        def path = "tests/in1"
        def reader = new XMCDA2Reader(path, ["preference", "hierarchy", "alternatives"])
        when:
        def xmcda = reader.read()
        then:
        xmcda.alternatives.size() == 3
        xmcda.alternativesMatricesList.size() == 2
        reader.executionResult.isOk()
    }
}
