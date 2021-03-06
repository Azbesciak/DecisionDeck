package pl.poznan.put.ahp

import org.ejml.simple.SimpleEVD
import org.ejml.simple.SimpleMatrix
import pl.poznan.put.xmcda.ranking.Alternative
import pl.poznan.put.xmcda.ranking.RankEntry
import pl.poznan.put.xmcda.ranking.Ranking
import java.lang.Math.abs
import java.math.BigDecimal
import java.math.RoundingMode

class AhpRanking private constructor(
        private val ranking: List<AhpRankEntry>
) : Ranking<AhpAlternative>, List<RankEntry<AhpAlternative>> by ranking {
    constructor(alternatives: Iterable<AhpAlternative>) :
            this(alternatives.map { AhpRankEntry(it, it.total()) }.sortedByDescending { it.value })
}

data class AhpRankEntry(
        override val alternative: AhpAlternative,
        override val value: Double
) : RankEntry<AhpAlternative>

sealed class Node(val name: String)
class AhpAlternative(name: String, val preferences: MutableMap<String, Double> = mutableMapOf()) : Node(name), Alternative {
    companion object {
        @JvmStatic
        fun List<AhpAlternative>.ranking() = AhpRanking(this)
    }

    operator fun get(name: String) = preferences[name]

    fun total() = preferences.map { it.value }.sum()
    override fun toString() = "AhpAlternative(name=$name, preferences=$preferences)"
}

class Category(
        name: String,
        val subNodes: List<Node>,
        val preferenceMat: List<List<Double>>
) : Node(name) {
    companion object {
        val ri = arrayOf(0.0, 0.0, 0.0, 0.58, 0.9, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49).map { BigDecimal(it) }
        val VALIDITY_THRESHOLD = "0.1".toBigDecimal()
        private const val VALIDITY_EPS = 0.001
    }

    var cr = BigDecimal.ONE
    var ci = BigDecimal.ONE

    val validity get() = cr <= VALIDITY_THRESHOLD

    init {
        validate()
        initialize()
    }

    var preference: Double = 1.0
        set(value) {
            field *= value
            subNodes.forEach {
                when (it) {
                    is Category -> it.preference = value
                    is AhpAlternative -> it.preferences[name] = (it.preferences[name] ?: 1.0) * value
                }
            }
        }

    fun checkValidity(): List<InvalidNode> {
        val result = if (validity) emptyList() else listOf(InvalidNode(name, cr))
        val subNodesResult = subNodes.flatMap {
            when (it) {
                is Category -> it.checkValidity()
                else -> emptyList()
            }
        }
        return result + subNodesResult
    }

    private fun validate() {
        requireCondition(subNodes.size == preferenceMat.size) { "categories number is not equal to preferences n" }
        requireCondition(subNodes.size < ri.size) { "number of categories must be less or equal to ${ri.size - 1}" }
        requireCondition(preferenceMat.all { it.all { v -> v > 0 } }) { "all values must be positive in preference matrix" }
        preferenceMat.forEachIndexed { r, row ->
            requireCondition(row.size == preferenceMat.size) {
                "row $r differs in size (${row.size} but expected ${preferenceMat.size})"
            }
        }
        preferenceMat.forEachIndexed { r, row ->
            row.forEachIndexed { vi, value ->
                when (vi) {
                    r -> requireCondition(abs(value - 1) < VALIDITY_EPS) {
                        "all values on diagonal must be equal to 1"
                    }
                    else -> {
                        val reversePref = preferenceMat[r][vi]
                        requireCondition(abs(value - reversePref) < VALIDITY_EPS) {
                            "value for preference $vi -> $r must be equal to preference 1/($r -> $vi), got $value and ${1 / reversePref} (originally $reversePref)"
                        }
                    }
                }
            }
        }
    }

    private inline fun requireCondition(condition: Boolean, message: () -> String) {
        require(condition) {
            "${message()} for '$name'"
        }
    }

    private fun initialize() {
        val n = subNodes.size
        val (maxEigenValue, normalizedEigenVector) = EigenCalculator.calculate(preferenceMat)
        if (n > 1) {
            val bdn = BigDecimal(n)
            ci = (maxEigenValue - bdn) / (bdn - BigDecimal.ONE)
            cr = if (n > 2) ci / ri[n] else BigDecimal.ZERO
        }

        subNodes.forEachIndexed { i, c ->
            when (c) {
                is Category -> c.preference = normalizedEigenVector[i]
                is AhpAlternative -> c.preferences[name] = normalizedEigenVector[i]
            }
        }
    }

    private val totalCi: BigDecimal get() = ci + sumBy { totalCi }
    private val totalRi: BigDecimal get() = ri[subNodes.size] + sumBy { totalRi }

    private inline fun sumBy(f: Category.() -> BigDecimal) = subNodes
            .map { (it as? Category)?.f() ?: BigDecimal.ZERO }
            .reduce { a, b -> a + b }

    fun getTotalCR() = totalCi / totalRi

    operator fun get(nodeName: String) = subNodes.find { it.name == nodeName }
    override fun toString() =
            "Category(name=$name, subNodes=$subNodes, preferenceMat=$preferenceMat, preference=$preference, cr=$cr, ci=$ci)"
}

data class InvalidNode(val name: String, val cr: BigDecimal)
data class AhpResult(val ranking: AhpRanking, val invalidNode: List<InvalidNode>)


data class Eigen(
        val principalEigenValue: BigDecimal,
        val normalizedPrincipalEigenVector: List<Double>
)

object EigenCalculator {
    fun calculate(mat: List<List<Double>>): Eigen {
        val n = mat.size
        val tempMat = mat.map { it.toDoubleArray() }.toTypedArray()
        val eig = SimpleMatrix(tempMat).eig()
        require(eig.numberOfEigenvalues == n) {
            "missing eigen values, got ${eig.numberOfEigenvalues} for matrix $mat"
        }
        val eigenVector = eig.getEigenVector(n - 1)
        val positiveValues = (0 until eigenVector.numRows())
                .map { eigenVector[it] }
                .map { if (it < 0) it * -.5 else it }
        val total = positiveValues.sum()
        val normalized = positiveValues.map { it / total }
        val principalEigenValue = eig.principalEigenValue(n)
        return Eigen(principalEigenValue, normalized)
    }

    private fun SimpleEVD<SimpleMatrix>.principalEigenValue(n: Int) =
            if (n > 1)
                BigDecimal((0 until n)
                        .map { getEigenvalue(it).magnitude }
                        .max() ?: 0.0
                ).setScale(8, RoundingMode.HALF_UP)
            else BigDecimal.ONE
}
