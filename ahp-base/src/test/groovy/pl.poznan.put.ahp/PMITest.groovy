package pl.poznan.put.ahp


import spock.lang.Specification

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

class PMITest extends Specification {

    // https://www.pmi.org/learning/library/analytic-hierarchy-process-prioritize-projects-6608
    def "PMI example"() {

        def nOffice = alt("no")
        def erp = alt("erp")
        def chOff = alt("chOff")
        def iProd = alt("iProd")
        def itOuts = alt("itOuts")
        def locCamp = alt("locCamp")

        given:
        def alternatives = [nOffice, erp, chOff, iProd, itOuts, locCamp]

        def teamCom = "teamCom"
        def orgCom = "orgCom"
        def pmCom = "pmCom"
        def scCom = "scCom"

        def teamComMat = [
                [1, 5, 3, 1 / 3, 9, 7],
                [1 / 5, 1, 1 / 5, 1 / 7, 1, 1 / 3],
                [1 / 3, 5, 1, 1 / 3, 7, 3],
                [3, 7, 3, 1, 5, 5],
                [1 / 9, 1, 1 / 7, 1 / 5, 1, 1 / 3],
                [1 / 7, 3, 1 / 3, 1 / 5, 3, 1]
        ]

        def orgComMat = [
                [1, 3, 1 / 9, 1 / 5, 5, 3],
                [1 / 3, 1, 1 / 9, 1 / 7, 1, 1 / 3],
                [9, 9, 1, 3, 7, 7],
                [5, 7, 1 / 3, 1, 9, 7],
                [1 / 5, 1, 1 / 7, 1 / 9, 1, 1 / 3],
                [1 / 3, 3, 1 / 7, 1 / 7, 3, 1]
        ]
        def pmComMat = [
                [1, 7, 1 / 3, 1 / 3, 5, 3],
                [1 / 7, 1, 1 / 9, 1 / 7, 3, 1 / 3],
                [3, 9, 1, 1, 7, 7],
                [3, 7, 1, 1, 7, 9],
                [1 / 5, 1 / 3, 1 / 7, 1 / 7, 1, 1 / 5],
                [1 / 3, 3, 1 / 7, 1 / 9, 5, 1]
        ]

        def scComMat = [
                [1, 3, 1 / 5],
                [1 / 3, 1, 1 / 9],
                [5, 9, 1]
        ]

        def scComCat = new Category(scCom, [
                new Category(teamCom, alternatives, teamComMat),
                new Category(orgCom, alternatives, orgComMat),
                new Category(pmCom, alternatives, pmComMat)
        ], scComMat)

        def roi = "roi"
        def profit = "profit"
        def npv = "npv"
        def fin = "fin"

        def roiMat = [
                [1, 1 / 3, 1 / 7, 1 / 9, 1 / 3, 1 / 3],
                [3, 1, 1 / 9, 1 / 9, 1 / 3, 1 / 3],
                [7, 9, 1, 1 / 3, 7, 5],
                [9, 9, 3, 1, 7, 5],
                [3, 3, 1 / 7, 1 / 7, 1, 1 / 3],
                [3, 3, 1 / 5, 1 / 5, 3, 1]
        ]

        def profitMat = [
                [1, 1, 1 / 7, 1 / 9, 1 / 5, 1 / 3],
                [1, 1, 1 / 7, 1 / 9, 1 / 3, 1 / 5],
                [7, 7, 1, 1 / 3, 7, 5],
                [9, 9, 3, 1, 9, 5],
                [5, 3, 1 / 7, 1 / 9, 1, 1 / 3],
                [3, 5, 1 / 5, 1 / 5, 3, 1]
        ]
        def npvMat = [
                [1, 1 / 3, 1 / 5, 1 / 7, 1 / 3, 1 / 3],
                [3, 1, 1 / 5, 1 / 7, 1, 1 / 3],
                [5, 5, 1, 1 / 3, 5, 3],
                [7, 7, 3, 1, 5, 7],
                [3, 1, 1 / 5, 1 / 5, 1, 1 / 3],
                [3, 3, 1 / 3, 1 / 7, 3, 1]
        ]

        def finMat = [
                [1, 1 / 5, 1 / 5],
                [5, 1, 1],
                [5, 1, 1]
        ]
        def finCat = new Category(fin, [
                new Category(roi, alternatives, roiMat),
                new Category(profit, alternatives, profitMat),
                new Category(npv, alternatives, npvMat)
        ], finMat)


        def iacim = "iacim"
        def iip = "iip"
        def ir = "ir"
        def str = "str"

        def iacimMat = [
                [1, 3, 1 / 9, 1 / 7, 5, 5],
                [1 / 3, 1, 1 / 9, 1 / 9, 1 / 3, 3],
                [9, 9, 1, 1, 9, 9],
                [7, 9, 1, 1, 9, 9],
                [1 / 5, 3, 1 / 9, 1 / 9, 1, 3],
                [1 / 5, 1 / 3, 1 / 9, 1 / 9, 1 / 3, 1]
        ]

        def iipMat = [
                [1, 1 / 5, 3, 5, 1, 7],
                [5, 1, 7, 7, 1, 7],
                [1 / 3, 1 / 7, 1, 1, 1 / 7, 1],
                [1 / 5, 1 / 7, 1, 1, 1 / 7, 1 / 3],
                [1, 1, 7, 7, 1, 7],
                [1 / 7, 1 / 7, 1, 3, 1 / 7, 1]
        ]
        def irMat = [
                [1, 1 / 3, 1 / 7, 1 / 5, 3, 1 / 7],
                [3, 1, 1 / 9, 1 / 5, 5, 1 / 7],
                [7, 9, 1, 3, 7, 1],
                [5, 5, 1 / 3, 1, 7, 1 / 3],
                [1 / 3, 1 / 5, 1 / 7, 1 / 7, 1, 1 / 9],
                [7, 7, 1, 3, 9, 1]
        ]
        def strMat = [
                [1, 7, 3],
                [1 / 7, 1, 1 / 5],
                [1 / 3, 5, 1]
        ]

        def strCat = new Category(str, [
                new Category(iacim, alternatives, iacimMat),
                new Category(iip, alternatives, iipMat),
                new Category(ir, alternatives, irMat)
        ], strMat)

        def lr = "lr"
        def ur = "ur"
        def itk = "itk"
        def oc = "oc"

        def lrMat = [
                [1, 5, 7, 3, 5, 1],
                [1 / 5, 1, 5, 3, 3, 1 / 7],
                [1 / 7, 1 / 5, 1, 1 / 3, 1 / 3, 1 / 9],
                [1 / 3, 1 / 3, 3, 1, 5, 1 / 7],
                [1 / 5, 1 / 3, 3, 1 / 5, 1, 1 / 9],
                [1, 7, 9, 7, 9, 1]
        ]

        def urMat = [
                [1, 1 / 3, 1 / 5, 1 / 7, 3, 1],
                [3, 1, 1 / 7, 1 / 9, 3, 3],
                [5, 7, 1, 1 / 3, 5, 7],
                [7, 9, 3, 1, 7, 7],
                [1 / 3, 1 / 3, 1 / 5, 1 / 7, 1, 1 / 3],
                [1, 1 / 3, 1 / 7, 1 / 7, 3, 1]
        ]
        def itkMat = [
                [1, 9, 9, 9, 9, 3],
                [1 / 9, 1, 1 / 3, 1 / 3, 1 / 5, 1 / 9],
                [1 / 9, 3, 1, 3, 1, 1 / 9],
                [1 / 9, 3, 1 / 3, 1, 1 / 3, 1 / 9],
                [1 / 9, 5, 1, 3, 1, 1 / 9],
                [1 / 3, 9, 9, 9, 9, 1]
        ]
        def ocMat = [
                [1, 5, 1 / 3],
                [1 / 5, 1, 1 / 7],
                [3, 7, 1]
        ]

        def ocCat = new Category(oc, [
                new Category(lr, alternatives, lrMat),
                new Category(ur, alternatives, urMat),
                new Category(itk, alternatives, itkMat)
        ], ocMat)

        def goalMat = [
                [1, 1 / 5, 1 / 9, 1],
                [5, 1, 1, 5],
                [9, 1, 1, 5],
                [1, 1 / 5, 1 / 5, 1]
        ]

        def goalCat = new Category("goal", [
                scComCat, finCat, strCat, ocCat
        ], goalMat)

        expect:
        def ranking = AhpAlternative.ranking(alternatives)
        def EPS = 0.0001
        that scComCat[teamCom].preference, closeTo(0.0122, EPS)
        that scComCat[orgCom].preference, closeTo(0.0048, EPS)
        that scComCat[pmCom].preference, closeTo(0.0514, EPS)

        that finCat[roi].preference, closeTo(0.0357, EPS)
        that finCat[profit].preference, closeTo(0.1785, EPS)
        that finCat[npv].preference, closeTo(0.1785, EPS)

        that strCat[iacim].preference, closeTo(0.2988, EPS)
        that strCat[iip].preference, closeTo(0.0331, EPS)
        that strCat[ir].preference, closeTo(0.1284, EPS)

        that ocCat[lr].preference, closeTo(0.0219, EPS)
        that ocCat[ur].preference, closeTo(0.0056, EPS)
        that ocCat[itk].preference, closeTo(0.0510, EPS)

        // values in the example, are as said there, approximated (10% accuracy)
        def GOAL_EPS = EPS * 50
        that goalCat[scCom].preference, closeTo(0.0693, GOAL_EPS)
        that goalCat[fin].preference, closeTo(0.3946, GOAL_EPS)
        that goalCat[str].preference, closeTo(0.4571, GOAL_EPS)
        that goalCat[oc].preference, closeTo(0.0789, GOAL_EPS)

        def FINAL_EPS = EPS * 100
        that iProd.total(), closeTo(0.3439, FINAL_EPS)
        that chOff.total(), closeTo(0.3074, FINAL_EPS)
        that locCamp.total(), closeTo(0.131, FINAL_EPS)
        that nOffice.total(), closeTo(0.0992, FINAL_EPS)
        that itOuts.total(), closeTo(0.0596, FINAL_EPS)
        that erp.total(), closeTo(0.059, FINAL_EPS)
        ranking[0].alternative == iProd
        ranking[1].alternative == chOff
        ranking[2].alternative == locCamp
        ranking[3].alternative == nOffice
        ranking[4].alternative == itOuts
        ranking[5].alternative == erp
    }

    def alt(name) {
        new AhpAlternative(name, [:])
    }

}
