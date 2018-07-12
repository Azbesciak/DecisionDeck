package pl.poznan.put.ahp

import org.ejml.simple.SimpleMatrix
import spock.lang.Specification

import spock.lang.*
import static spock.util.matcher.HamcrestMatchers.closeTo

import static spock.util.matcher.HamcrestSupport.that

class FirstSpecification extends Specification {
//    http://www.baeldung.com/groovy-spock
    def "eigen test"() {
        given:
        double[][] mat = [
                [1, 1 / 3, 5],
                [3, 1, 7],
                [1 / 5, 1 / 7, 1]
        ]
        def matrix = new SimpleMatrix(mat)
        def eig = matrix.eig()
        def eigVec = eig.getEigenVector(2)

        expect:
        eig.numberOfEigenvalues == 3
        eigVec.matrix.numRows == 3
        eigVec.matrix.numCols == 1
    }

    def "eigen on Category"() {
        given:
        List<List<Double>> mat = [
                [1, 1 / 3, 5],
                [3, 1, 7],
                [1 / 5, 1 / 7, 1]
        ]
        def c = new Category("Fruits", [
                new Category("Apple", [], []),
                new Category("Banana", [], []),
                new Category("Cherry", [],[])
        ],mat)

        expect:
        that c.subcategories[0].preference, closeTo(0.279, 0.001)
        that c.subcategories[1].preference, closeTo(0.6491, 0.001)
        that c.subcategories[2].preference, closeTo(0.0719, 0.001)
    }
}
