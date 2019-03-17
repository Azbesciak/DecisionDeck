package pl.poznan.put.ahp

import org.xmcda.CommonAttributes
import org.xmcda.CriteriaMatrix
import org.xmcda.QualifiedValues
import org.xmcda.utils.Coord

abstract class AhpBaseBuilder {
    var criteria = listOf<CrytId>()
    protected var criteriaComp = mutableMapOf<CrytId, List<RelationValue>>()

    fun CriteriaMatrix<*>.fetch() {
        requireNotNull(id()) { "Missing parent criteria comparision id " }
        require(id() !in criteriaComp) { "duplicated criteria comparisons for criterion ${id()}" }
        criteriaComp[id()] = map { it.relation }
    }

    protected inline val <reified K : CommonAttributes, reified V> Map.Entry<Coord<K, K>, QualifiedValues<out V>>.relation
        get() = RelationValue(
                key.x.id(),
                key.y.id(),
                value.convertToDouble().value()
        ).also {
            require(it.firstID != it.secondID || it.value == 1.0) {
                "Self reference is required to have preference equal to 1; got ${it.value} for '${it.firstID}'"
            }
        }

    protected fun QualifiedValues<Double>.value() = first().value.also {
        require(it > 0) { "Value must be positive real value, got $it" }
    }

    abstract fun build(): AhpResult



    protected fun MutableMap<CrytId, List<RelationValue>>.fetchIdWithAssertion(toFetch: String): List<CrytId> {
        require(isNotEmpty()) { "$toFetch not found" }
        forEach { k, y -> require(y.isNotEmpty()) { "$toFetch on '$k' are not set" } }
        return keys.sorted()
    }
}
