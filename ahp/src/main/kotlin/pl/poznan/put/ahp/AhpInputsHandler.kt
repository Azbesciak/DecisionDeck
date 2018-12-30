package pl.poznan.put.ahpimport org.xmcda.*import org.xmcda.utils.Coordimport pl.poznan.put.ahp.AhpAlternative.Companion.rankingimport pl.poznan.put.xmcda.InputsHandler/** * @author */internal object AhpInputsHandler : InputsHandler<AhpResult> {    /**     * This class contains every element which are needed to compute     * AHP.<br></br>     * It is populated by [InputsHandler.checkAndExtractInputs].     */    /**     * @param xmcda     * @return     */    override fun checkAndExtractInputs(xmcda: XMCDA) = HierarchyParser(xmcda).ranking()    private class HierarchyParser(private val xmcda: XMCDA) {        fun ranking() = build {            alternatives = xmcda.alternatives.activeAlternatives.map { AhpAlternative(it.id()) }.sortedBy { it.name }            criteria = xmcda.criteria.activeCriteria.map { it.id() }.sorted()            xmcda.criteriaHierarchiesList.forEach { it.flatten() }            xmcda.criteriaMatricesList.forEach { it.fetch() }            xmcda.alternativesMatricesList.forEach { it.fetch() }        }        private inline fun build(f: AhpBuilder.() -> Unit) =                AhpBuilder().run {                    f()                    build()                }    }    private class AhpBuilder {        var alternatives = listOf<AhpAlternative>()        var criteria = listOf<CrytId>()        private var relations = Relations()        private var alternativesPreferences = mutableMapOf<CrytId, List<RelationValue>>()        private var criteriaComp = mutableMapOf<CrytId, List<RelationValue>>()        private var topNode: CrytId? = null        fun CriteriaHierarchy.flatten() {            require(rootNodes.size == 1) {                "Exactly one root required!, found ${rootNodes.map {                    it.criterion.id() ?: "unknown ID"                }}"            }            require(topNode == null) { "root hierarchy already set" }            topNode = rootNodes.first().criterion.id()            rootNodes.forEach { it.flatten() }        }        private fun CriterionHierarchyNode.flatten() {            children.forEach { it.flatten() }            val critId = criterion.id()            requireNotNull(critId) { "criterion id not set in hierarchy" }            require(critId !in relations) { "criteria $critId duplicated in hierarchy outside of the context" }            relations[critId] = children.map { it.criterion.id() }.sorted()        }        fun CriteriaMatrix<*>.fetch() {            requireNotNull(id()) { "Missing parent criteria comparision id " }            require(id() !in criteriaComp) { "duplicated criteria comparisons for criterion ${id()}" }            criteriaComp[id()] = map { it.relation }        }        fun AlternativesMatrix<*>.fetch() {            requireNotNull(id()) { "Missing criteriaID for alternatives comparision" }            require(id() !in alternativesPreferences) { "duplicated alternatives comparision on criterion ${id()}" }            alternativesPreferences[id()] = map { it.relation }        }        private inline val <reified K : CommonAttributes, reified V> Map.Entry<Coord<K, K>, QualifiedValues<out V>>.relation            get() = RelationValue(                    key.x.id(),                    key.y.id(),                    value.convertToDouble().value()            ).also {                require(it.firstID != it.secondID || it.value == 1.0) {                    "Self reference is required to have preference equal to 1; got ${it.value} for ${it.firstID}"                }            }        private fun QualifiedValues<Double>.value() = first().value.also {            require(it > 0) { "Value must be positive real value, got $it" }        }        fun build(): AhpResult {            val criteriaFromPreference = getCriteriaFromPreference()            val leafCriteria = relations.leafs            require(criteriaFromPreference == leafCriteria) {                """                |leaf criteria must be the same in hierarchy and in preferences.                |   criteria from hierarchy: $leafCriteria                |   preferences: $criteriaFromPreference            """.trimMargin()            }            val criteriaPreferences = CriteriaPreferences(criteriaComp.toMatrix { k -> relations.of(k) }.toMutableMap())            val invalidNodes = createLeafsWithAlternatives(leafCriteria)                    .asLeafs()                    .topCategory(relations, criteriaPreferences)                    .checkValidity()            return AhpResult(alternatives.ranking(), invalidNodes)        }        private fun getCriteriaFromPreference() =                alternativesPreferences.run {                    require(isNotEmpty()) { "preferences not found" }                    forEach { k, y -> require(y.isNotEmpty()) { "preferences on $k are not set" } }                    keys.sorted()                }        private fun createLeafsWithAlternatives(leafCriteria: List<CrytId>): Map<CrytId, Category> {            val sameAlternativesRelations = alternatives.map { it.relation() }            val altPreferencesOnCriteria = alternativesPreferences.toMatrix { sameAlternativesRelations }            return leafCriteria.map { it to Category(it, alternatives, altPreferencesOnCriteria[it]!!) }.toMap()        }        private inline fun Map<String, List<RelationValue>>.toMatrix(diagonalSup: (key: String) -> List<RelationValue>) =                mapValues { (k, v) ->                    val notDiagonal = v.filter { it.firstID != it.secondID }                    if (notDiagonal.isEmpty()) return emptyMap<String, List<List<Double>>>()                    val diagonal = diagonalSup(k)                    val matrix = (notDiagonal.flatMap { listOf(it, it.reversed()) } + diagonal)                            .sortedWith(compareBy({ it.firstID }, { it.secondID }))                    matrix.map { it.firstID to it.secondID }                            .requireDistinct { "values of $it are duplicated comparision for $k" }                    matrix.map { it.value }.chunked(diagonal.size).also {                        require(it.size == it.last().size) { "dimensions of $k are not equal" }                        require(it.size == diagonal.size) { "too few elements for $k" }                    }                }        private inline fun <T> List<T>.requireDistinct(onError: (T) -> String) {            val set = mutableSetOf<T>()            forEach {                require(set.contains(it).not()) { onError(it) }                set += it            }        }    }    private data class RelationValue(            val firstID: String,            val secondID: String,            val value: Double    ) {        fun reversed() = RelationValue(secondID, firstID, 1 / value)    }    private fun AhpAlternative.relation() = name.relation()    private fun String.relation() = RelationValue(this, this, 1.0)    private data class Relations(            private val map: MutableMap<CrytId, List<CrytId>> = mutableMapOf()    ) : MutableMap<CrytId, List<CrytId>> by map {        val leafs get() = filter { it.value.isEmpty() }.keys.sorted()        fun of(criterion: CrytId) = get(criterion)!!.map { it.relation() }    }    private data class CriteriaPreferences(            private val map: MutableMap<CrytId, List<List<Double>>> = mutableMapOf()    ) : MutableMap<CrytId, List<List<Double>>> by map    private data class Leafs(            private val map: MutableMap<CrytId, Category>    ) : MutableMap<CrytId, Category> by map {        private val single            get(): Category {                require(size == 1) { "expected only one head of hierarchy, got $size: ${map { it.key }}" }                return map { it.value }.first()            }        fun topCategory(                relations: Relations,                criteriaPreferences: CriteriaPreferences        ) = buildCat(relations, criteriaPreferences, this).single        private tailrec fun buildCat(                relations: Relations,                criteriaPreferences: CriteriaPreferences,                leafs: Leafs        ): Leafs {            leafs.forEach { relations.remove(it.key) }            if (relations.isEmpty()) return leafs            val newLeafs = matchingRelationsToCategories(relations, leafs, criteriaPreferences)            return buildCat(relations, criteriaPreferences, newLeafs)        }        private fun matchingRelationsToCategories(relations: Relations, leafs: Leafs, criteriaPreferences: CriteriaPreferences) =                relations                        .mapValues { (k, v) -> matchRelationsWithCategories(k, v, leafs) }                        .filter { it.value.isNotEmpty() }                        .mapValues { (k, v) -> Category(k, v, criteriaPreferences.remove(k)!!) }                        .asLeafs()        private fun matchRelationsWithCategories(k: CrytId, v: List<CrytId>, leafs: Leafs) =                v.mapNotNull { leafs.remove(it) }.also {                    require(it.isEmpty() || it.size == v.size) { "not all categories matched for $k" }                }    }    private fun Map<CrytId, Category>.asLeafs() = Leafs(toMutableMap())}