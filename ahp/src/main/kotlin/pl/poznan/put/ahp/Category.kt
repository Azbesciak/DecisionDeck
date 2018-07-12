package pl.poznan.put.ahp

import koma.matrix.ejml.backend.div
import org.ejml.simple.SimpleMatrix

data class Category(val name: String,
                    val subcategories: List<Category> = listOf(),
                    val preferenceMat: List<List<Double>> = listOf()) {
    init {
        validate()
        if (subcategories.isNotEmpty())
            initialize()
    }

    var preference: Double = 1.0

    private fun validate() {
        require(subcategories.size == preferenceMat.size) { "categories number is not equal to preferences n" }
        preferenceMat.forEachIndexed { i, row ->
            require(row.size == preferenceMat.size) { "row $i differs in size (${row.size} but expected ${preferenceMat.size})" }
        }
    }

    private fun initialize() {
        val tempMat = preferenceMat.map { it.toDoubleArray() }.toTypedArray()
        val eig = SimpleMatrix(tempMat).eig()
        require(eig.numberOfEigenvalues == subcategories.size) {"missing eigen values, got ${eig.numberOfEigenvalues}"}
        val eigenVector = eig.getEigenVector(subcategories.size - 1)
        val normalized = eigenVector.div(eigenVector.elementSum())
        subcategories.forEachIndexed {i, c ->
            c.preference = normalized.get(i)
        }
    }
}