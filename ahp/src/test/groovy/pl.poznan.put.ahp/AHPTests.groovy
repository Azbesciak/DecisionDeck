package pl.poznan.put.ahp

import org.ejml.simple.SimpleMatrix
import spock.lang.Specification

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class AHPTests extends Specification {
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
        def CATEGORY_NAME = "Fruits"
        def c = new Category(CATEGORY_NAME, [
                alt("Apple"),
                alt("Banana"),
                alt("Cherry")
        ], mat)

        expect:
        that c["Apple"][CATEGORY_NAME], closeTo(0.279, 0.001)
        that c["Banana"][CATEGORY_NAME], closeTo(0.6491, 0.001)
        that c["Cherry"][CATEGORY_NAME], closeTo(0.0719, 0.001)
    }

    //https://en.wikipedia.org/wiki/Analytic_hierarchy_process_%E2%80%93_car_example
    def "wikipedia ahp-leader example"() {
        def tom = alt("Tom")
        def dick = alt("Dick")
        def harry = alt("Harry")
        given:
        def alternatives = [tom, dick, harry]
        List<List<Double>> experience = [
                [1, 1 / 4, 4],
                [4, 1, 9],
                [1 / 4, 1 / 9, 1]
        ]

        List<List<Double>> education = [
                [1, 3, 1 / 5],
                [1 / 3, 1, 1 / 7],
                [5, 7, 1]
        ]

        List<List<Double>> charisma = [
                [1, 5, 9],
                [1 / 5, 1, 4],
                [1 / 9, 1 / 4, 1]
        ]

        List<List<Double>> age = [
                [1, 1 / 3, 5],
                [3, 1, 9],
                [1 / 5, 1 / 9, 1]
        ]

        def exp = "Experience"
        def edu = "Education"
        def chr = "Charisma"
        def ag = "Age"
        def categories = [
                new Category(exp, alternatives, experience),
                new Category(edu, alternatives, education),
                new Category(chr, alternatives, charisma),
                new Category(ag, alternatives, age)
        ]
        List<List<Double>> categoryPrefMat = [
                [1, 4, 3, 7],
                [1 / 4, 1, 1 / 3, 3],
                [1 / 3, 3, 1, 5],
                [1 / 7, 1 / 3, 1 / 5, 1]
        ]
        def goal = new Category("choose a Leader", categories, categoryPrefMat)

        expect:
        that tom[exp], closeTo(0.119, 0.001)
        that dick[exp], closeTo(0.392, 0.001)
        that harry[exp], closeTo(0.036, 0.001)

        that tom[edu], closeTo(0.024, 0.001)
        that dick[edu], closeTo(0.010, 0.001)
        that harry[edu], closeTo(0.093, 0.001)

        that tom[chr], closeTo(0.201, 0.001)
        that dick[chr], closeTo(0.052, 0.001)
        that harry[chr], closeTo(0.017, 0.001)

        that tom[ag], closeTo(0.015, 0.001)
        that dick[ag], closeTo(0.038, 0.001)
        that harry[ag], closeTo(0.004, 0.001)

        def ranking = Alternative.ranking(alternatives)
        ranking[0].first == dick.name
        that ranking[0].second, closeTo(0.492, 0.001)

        ranking[1].first == tom.name
        that ranking[1].second, closeTo(0.358, 0.001)

        ranking[2].first == harry.name
        that ranking[2].second, closeTo(0.149, 0.001)
    }

    def "tutorial example"() {
        def x = alt("X")
        def y = alt("Y")
        def z = alt("Z")
        given:
        def alternatives = [
                x, y, z
        ]

        List<List<Double>> categoriesPref = [
                [1, 3, 7, 9],
                [1 / 3, 1, 5, 7],
                [1 / 7, 1 / 5, 1, 3],
                [1 / 9, 1 / 7, 1 / 3, 1]
        ]

        List<List<Double>> prefA = [
                [1, 1, 7],
                [1, 1, 3],
                [1 / 7, 1 / 3, 1]
        ]

        List<List<Double>> prefB = [
                [1, 1 / 5, 1 / 2],
                [5, 1, 5],
                [2, 1 / 5, 1]
        ]

        List<List<Double>> prefC = [
                [1, 1, 1],
                [1, 1, 1],
                [1, 1, 1]
        ]

        List<List<Double>> prefD = [
                [1, 1, 1],
                [1, 1, 1],
                [1, 1, 1]
        ]

        def goal = new Category("Goal", [
                new Category("A", alternatives, prefA),
                new Category("B", alternatives, prefB),
                new Category("C", alternatives, prefC),
                new Category("D", alternatives, prefD)
        ], categoriesPref)
        expect:
        that goal["A"].cr, closeTo(0.069,0.001)
        that goal["A"].ci, closeTo(0.04,0.001)

        that goal["B"].cr, closeTo(0.046,0.001)
        that goal["B"].ci, closeTo(0.027,0.001)

        that goal.totalCR, closeTo(0.038,0.001)
    }

    def alt(name) {
        new Alternative(name, [:])
    }


}
