package pl.poznan.put.ahp

import koma.matrix.ejml.backend.div
import org.ejml.simple.SimpleMatrix
import pl.poznan.put.xmcda.ranking.Alternative
import pl.poznan.put.xmcda.ranking.RankEntry
import pl.poznan.put.xmcda.ranking.Ranking
import java.lang.Math.abs
import java.math.BigDecimal
import java.math.RoundingMode

internal class AhpRanking private constructor(
        private val ranking: List<AhpRankEntry>
) : Ranking<AhpAlternative>, List<RankEntry<AhpAlternative>> by ranking {
    constructor(alternatives: Iterable<AhpAlternative>) :
            this(alternatives.map { AhpRankEntry(it, it.total()) }.sortedByDescending { it.value })
}

internal data class AhpRankEntry(
        override val alternative: AhpAlternative,
        override val value: Double
) : RankEntry<AhpAlternative>

internal sealed class Node(val name: String)
internal class AhpAlternative(name: String, val preferences: MutableMap<String, Double> = mutableMapOf()) : Node(name), Alternative {
    companion object {
        @JvmStatic
        fun List<AhpAlternative>.ranking() = AhpRanking(this)
    }

    operator fun get(name: String) = preferences[name]

    fun total() = preferences.map { it.value }.sum()
    override fun toString() = "AhpAlternative(name=$name, preferences=$preferences)"
}

internal class Category(
        name: String,
        val subNodes: List<Node>,
        val preferenceMat: List<List<Double>>
) : Node(name) {
    companion object {
        val ri = arrayOf(0.0, 0.0, 0.0, 0.58, 0.9, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49).map { BigDecimal(it) }
        private const val VALIDITY_EPS = 0.001
    }

    var cr = BigDecimal.ONE
    var ci = BigDecimal.ONE

    val validity get() = cr <= "0.1".toBigDecimal()

    init {
        validate()
        initialize()
    }

    var preference: Double = 1.0
        set(value) {
            field = value
            subNodes.forEach {
                when (it) {
                    is Category -> it.preference *= value
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
        val tempMat = preferenceMat.map { it.toDoubleArray() }.toTypedArray()
        val eig = SimpleMatrix(tempMat).eig()
        requireCondition(eig.numberOfEigenvalues == n) { "missing eigen values, got ${eig.numberOfEigenvalues}" }
        val eigenVector = eig.getEigenVector(n - 1)
        val normalized = eigenVector.div(eigenVector.elementSum())
        if (n > 1) {
            val bdn = BigDecimal(n)
            val maxValue = BigDecimal(
                    (0 until n)
                            .map { eig.getEigenvalue(it).magnitude }
                            .max() ?: 0.0
            ).setScale(8, RoundingMode.HALF_UP)
            ci = (maxValue - bdn) / (bdn - BigDecimal.ONE)
            cr = if (n > 2) ci / ri[n] else BigDecimal.ZERO
        }

        subNodes.forEachIndexed { i, c ->
            when (c) {
                is Category -> c.preference = normalized[i]
                is AhpAlternative -> c.preferences[name] = normalized[i]
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

internal data class InvalidNode(val name: String, val cr: BigDecimal)
internal data class AhpResult(val ranking: AhpRanking, val invalidNode: List<InvalidNode>)
