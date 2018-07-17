package pl.poznan.put.ahp

import koma.matrix.ejml.backend.div
import org.ejml.simple.SimpleMatrix

sealed class Node(val name: String)
class Alternative(name: String, val preferences: MutableMap<String, Double> = mutableMapOf()) : Node(name) {

    companion object {
        @JvmStatic
        fun List<Alternative>.ranking() = map { it.name to it.total() }.sortedByDescending { it.second }


    }
    operator fun get(name: String) = preferences[name]

    fun total() = preferences.map { it.value }.sum()
}

class Category(name: String,
               val subNodes: List<Node>,
               val preferenceMat: List<List<Double>>) : Node(name) {
    init {
        validate()
        initialize()
    }

    var preference: Double = 1.0
        set(value) {
            field = value
            subNodes.forEachIndexed { _, c ->
                when (c) {
                    is Category -> c.preference *= value
                    is Alternative -> c.preferences[name] = (c.preferences[name] ?: 1.0) * value
                }
            }
        }

    private fun validate() {
        require(subNodes.size == preferenceMat.size) { "categories number is not equal to preferences n" }
        preferenceMat.forEachIndexed { i, row ->
            require(row.size == preferenceMat.size) { "row $i differs in size (${row.size} but expected ${preferenceMat.size})" }
        }
    }

    private fun initialize() {
        val tempMat = preferenceMat.map { it.toDoubleArray() }.toTypedArray()
        val eig = SimpleMatrix(tempMat).eig()
        require(eig.numberOfEigenvalues == subNodes.size) { "missing eigen values, got ${eig.numberOfEigenvalues}" }
        val eigenVector = eig.getEigenVector(subNodes.size - 1)
        val normalized = eigenVector.div(eigenVector.elementSum())

        subNodes.forEachIndexed { i, c ->
            when (c) {
                is Category -> c.preference = normalized.get(i)
                is Alternative -> c.preferences[name] = normalized.get(i)
            }
        }
    }

    operator fun get(nodeName: String) = subNodes.find { it.name == nodeName }


}