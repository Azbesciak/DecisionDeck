package pl.poznan.put.ahp

import org.ejml.simple.SimpleMatrix
import spock.lang.Specification
import spock.lang.Unroll

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class EigenValuesTest extends Specification {
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

    @Unroll
    def "#name should return correct principal eigen vectors"() {
        expect:
        def eigen = EigenCalculator.INSTANCE.calculate(mat)
        that eigen.principalEigenValue, closeTo(eigenValue, 0.0001)
        eigen.normalizedPrincipalEigenVector.size() == mat.size()
        def total = eigenVector.sum()
        def normalizedEigenVector = eigenVector.collect { it / total }
        for (int i = 0; i < eigen.normalizedPrincipalEigenVector.size(); i++) {
            assert Math.abs(eigen.normalizedPrincipalEigenVector[i] - normalizedEigenVector[i]) <= 0.0001
        }

        where:
        name         | mat                | eigenValue         | eigenVector
        "teamComMat" | [
                [1, 5, 3, 1 / 3, 9, 7],
                [1 / 5, 1, 1 / 5, 1 / 7, 1, 1 / 3],
                [1 / 3, 5, 1, 1 / 3, 7, 3],
                [3, 7, 3, 1, 5, 5],
                [1 / 9, 1, 1 / 7, 1 / 5, 1, 1 / 3],
                [1 / 7, 3, 1 / 3, 1 / 5, 3, 1]
        ]                                 | 6.4998             | [
                0.5619282143437326,
                0.07163313082941292,
                0.3054449292631177,
                0.7500532267704983,
                0.06785720898856805,
                0.13647097815628875
        ]

        "orgComMat"  | [
                [1, 3, 1 / 9, 1 / 5, 5, 3],
                [1 / 3, 1, 1 / 9, 1 / 7, 1, 1 / 3],
                [9, 9, 1, 3, 7, 7],
                [5, 7, 1 / 3, 1, 9, 7],
                [1 / 5, 1, 1 / 7, 1 / 9, 1, 1 / 3],
                [1 / 3, 3, 1 / 7, 1 / 7, 3, 1]
        ]                                 | 6.53347255256551   | [
                1.634161621693711,
                0.5360916090680548,
                8.024502411129097,
                4.746802051311202,
                0.5186683727413013,
                1
        ]

        "pmComMat"   | [
                [1, 7, 1 / 3, 1 / 3, 5, 3],
                [1 / 7, 1, 1 / 9, 1 / 7, 3, 1 / 3],
                [3, 9, 1, 1, 7, 7],
                [3, 7, 1, 1, 7, 9],
                [1 / 5, 1 / 3, 1 / 7, 1 / 7, 1, 1 / 5],
                [1 / 3, 3, 1 / 7, 1 / 9, 5, 1]
        ]                                 | 6.537721419439245  | [
                2.1627877893750114,
                0.5478486908290775,
                4.695902239042845,
                4.834223002551214,
                0.39305340351461854,
                1
        ]

        "scComMat"   | [
                [1, 3, 1 / 5],
                [1 / 3, 1, 1 / 9],
                [5, 9, 1]
        ]                                 | 3.029063766730802  | [
                0.2371262202975767,
                0.09371474058254632,
                1
        ]

        "roiMat"     | [
                [1, 1 / 3, 1 / 7, 1 / 9, 1 / 3, 1 / 3],
                [3, 1, 1 / 9, 1 / 9, 1 / 3, 1 / 3],
                [7, 9, 1, 1 / 3, 7, 5],
                [9, 9, 3, 1, 7, 5],
                [3, 3, 1 / 7, 1 / 7, 1, 1 / 3],
                [3, 3, 1 / 5, 1 / 5, 3, 1]
        ]                                 | 6.519262683170052  | [
                0.29240753526405183,
                0.40924094161309915,
                3.0232030067924875,
                4.500614010571974,
                0.6365179496614836,
                1
        ]

        "profitMat"  | [
                [1, 1 / 3, 1 / 7, 1 / 9, 1 / 3, 1 / 3],
                [3, 1, 1 / 9, 1 / 9, 1 / 3, 1 / 3],
                [7, 9, 1, 1 / 3, 7, 5],
                [9, 9, 3, 1, 7, 5],
                [3, 3, 1 / 7, 1 / 7, 1, 1 / 3],
                [3, 3, 1 / 5, 1 / 5, 3, 1]
        ]                                 | 6.5192626831700515 | [
                0.2924075352640521,
                0.4092409416130996,
                3.023203006792491,
                4.5006140105719785,
                0.6365179496614841,
                1
        ]

        "npvMat"     | [
                [1, 1 / 3, 1 / 5, 1 / 7, 1 / 3, 1 / 3],
                [3, 1, 1 / 5, 1 / 7, 1, 1 / 3],
                [5, 5, 1, 1 / 3, 5, 3],
                [7, 7, 3, 1, 5, 7],
                [3, 1, 1 / 5, 1 / 5, 1, 1 / 3],
                [3, 3, 1 / 3, 1 / 7, 3, 1]
        ]                                 | 6.417890513755943  | [
                0.31039075953193584,
                0.5185759746177966,
                2.0780611893860175,
                4.025373669516853,
                0.5544166222384255,
                1
        ]

        "finMat"     | [
                [1, 1 / 5, 1 / 5],
                [5, 1, 1],
                [5, 1, 1]
        ]                                 | 3                  | [1, 5, 5]

        "iacimMat" | [
                [1, 3, 1 / 9, 1 / 7, 5, 5],
                [1 / 3, 1, 1 / 9, 1 / 9, 1 / 3, 3],
                [9, 9, 1, 1, 9, 9],
                [7, 9, 1, 1, 9, 9],
                [1 / 5, 3, 1 / 9, 1 / 9, 1, 3],
                [1 / 5, 1 / 3, 1 / 9, 1 / 9, 1 / 3, 1]
        ]            | 6.68787318076921   | [
                4.2791744784457615,
                1.5376369762634228,
                16.891758011467147,
                15.612076306152996,
                2.1238664412146413,
                1
        ]

        "iipMat" | [
                [1, 1 / 5, 3, 5, 1, 7],
                [5, 1, 7, 7, 1, 7],
                [1 / 3, 1 / 7, 1, 1, 1 / 7, 1],
                [1 / 5, 1 / 7, 1, 1, 1 / 7, 1 / 3],
                [1, 1, 7, 7, 1, 7],
                [1 / 7, 1 / 7, 1, 3, 1 / 7, 1]
        ]            | 6.4380979130739036 | [
                3.7194387221255294,
                7.768781520678094,
                0.8897408915511427,
                0.7091608747164805,
                5.457888600225656,
                1
        ]
        "irMat" | [
                [1, 1 / 3, 1 / 7, 1 / 5, 3, 1 / 7],
                [3, 1, 1 / 9, 1 / 5, 5, 1 / 7],
                [7, 9, 1, 3, 7, 1],
                [5, 5, 1 / 3, 1, 7, 1 / 3],
                [1 / 3, 1 / 5, 1 / 7, 1 / 7, 1, 1 / 9],
                [7, 7, 1, 3, 9, 1]
        ]            | 6.522719882865472  | [
                0.12411599147568284,
                0.20061401986966368,
                1.0385205237607542,
                0.512075579635929,
                0.07498472675326293,
                1
        ]

        "strMat" | [
                [1, 7, 3],
                [1 / 7, 1, 1 / 5],
                [1 / 3, 5, 1]
        ]            | 3.064887579975886  | [
                2.3269667714557185,
                0.2578463979136428,
                1
        ]

        "lrMat" | [
                [1, 5, 7, 3, 5, 1],
                [1 / 5, 1, 5, 3, 3, 1 / 7],
                [1 / 7, 1 / 5, 1, 1 / 3, 1 / 3, 1 / 9],
                [1 / 3, 1 / 3, 3, 1, 5, 1 / 7],
                [1 / 5, 1 / 3, 3, 1 / 5, 1, 1 / 9],
                [1, 7, 9, 7, 9, 1]
        ]            | 6.640032910115349  | [
                0.7077474127981013,
                0.2760163068104429,
                0.06598108316577549,
                0.21047271870025927,
                0.10367028558337271,
                1
        ]

        "urMat" | [
                [1, 1 / 3, 1 / 5, 1 / 7, 3, 1],
                [3, 1, 1 / 7, 1 / 9, 3, 3],
                [5, 7, 1, 1 / 3, 5, 7],
                [7, 9, 3, 1, 7, 7],
                [1 / 3, 1 / 3, 1 / 5, 1 / 7, 1, 1 / 3],
                [1, 1 / 3, 1 / 7, 1 / 7, 3, 1]
        ]            | 6.638620680983789  | [
                1.0468995398127874,
                1.749111087938022,
                5.448594465340575,
                9.034476478115037,
                0.6465579921725131,
                1
        ]

        "itkMat" | [
                [1, 9, 9, 9, 9, 3],
                [1 / 9, 1, 1 / 3, 1 / 3, 1 / 5, 1 / 9],
                [1 / 9, 3, 1, 3, 1, 1 / 9],
                [1 / 9, 3, 1 / 3, 1, 1 / 3, 1 / 9],
                [1 / 9, 5, 1, 3, 1, 1 / 9],
                [1 / 3, 9, 9, 9, 9, 1]
        ]            | 6.62084961531312   | [
                1.4478644156074028,
                0.07305539901868238,
                0.18271523929693653,
                0.11036007528374311,
                0.204783524566463,
                1
        ]

        "ocMat" | [
                [1, 5, 1 / 3],
                [1 / 5, 1, 1 / 7],
                [3, 7, 1]
        ]            | 3.064887579975886  | [
                0.4297439964621474,
                0.1108079415127779,
                1
        ]

    }
}
