package pl.poznan.put.ahp

data class RelationValue(
        val firstID: String,
        val secondID: String,
        val value: Double
) {
    fun reversed() = RelationValue(secondID, firstID, 1 / value)
}

fun AhpAlternative.relation() = name.relation()
fun String.relation() = RelationValue(this, this, 1.0)

data class Relations(
        private val map: MutableMap<CrytId, List<CrytId>> = mutableMapOf()
) : MutableMap<CrytId, List<CrytId>> by map {
    val leafs get() = filter { it.value.isEmpty() }.keys.sorted()
    fun of(criterion: CrytId) = get(criterion)!!.map { it.relation() }
}

data class CriteriaPreferences(
        private val map: MutableMap<CrytId, List<List<Double>>> = mutableMapOf()
) : MutableMap<CrytId, List<List<Double>>> by map

data class Leafs(
        private val map: MutableMap<CrytId, Category>
) : MutableMap<CrytId, Category> by map {
    private val single
        get(): Category {
            require(size == 1) { "expected only one head of hierarchy, got $size: ${map { it.key }}" }
            return map { it.value }.first()
        }

    fun topCategory(
            relations: Relations,
            criteriaPreferences: CriteriaPreferences
    ) = buildCat(relations, criteriaPreferences, this).single

    private tailrec fun buildCat(
            relations: Relations,
            criteriaPreferences: CriteriaPreferences,
            leafs: Leafs
    ): Leafs {
        leafs.forEach { relations.remove(it.key) }
        if (relations.isEmpty()) return leafs
        val newLeafs = matchingRelationsToCategories(relations, leafs, criteriaPreferences)
        return buildCat(relations, criteriaPreferences, newLeafs)
    }

    private fun matchingRelationsToCategories(relations: Relations, leafs: Leafs, criteriaPreferences: CriteriaPreferences) =
            relations
                    .mapValues { (k, v) -> matchRelationsWithCategories(k, v, leafs) }
                    .filter { it.value.isNotEmpty() }
                    .mapValues { (k, v) -> Category(k, v, criteriaPreferences.remove(k)!!) }
                    .asLeafs()

    private fun matchRelationsWithCategories(k: CrytId, v: List<CrytId>, leafs: Leafs) =
            v.mapNotNull { leafs.remove(it) }.also {
                require(it.isEmpty() || it.size == v.size) { "not all categories matched for '$k'" }
            }
}

fun Map<CrytId, Category>.asLeafs() = Leafs(toMutableMap())
