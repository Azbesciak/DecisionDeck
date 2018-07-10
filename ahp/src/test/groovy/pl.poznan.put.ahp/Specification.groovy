package pl.poznan.put.ahp

import spock.lang.Specification

class FirstSpecification extends Specification {
//    http://www.baeldung.com/groovy-spock
    def "one plus one should equal two"() {
        expect:
        1 + 1 == 2
    }
}
