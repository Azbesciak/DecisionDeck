package pl.poznan.put.ahp

object MatrixMaker {
    fun matrix(matrix: Map<String, List<RelationValue>>, diagonalSup: (key: String) -> List<RelationValue>) =
            matrix.toMatrix(diagonalSup)

    private inline fun Map<String, List<RelationValue>>.toMatrix(diagonalSup: (key: String) -> List<RelationValue>) =
            mapValues { (k, v) ->
                val notDiagonal = v.filter { it.firstID != it.secondID }
                if (notDiagonal.isEmpty()) return emptyMap<String, List<List<Double>>>()
                val diagonal = diagonalSup(k)
                val matrix = (notDiagonal.flatMap { listOf(it, it.reversed()) } + diagonal)
                        .sortedWith(compareBy({ it.firstID }, { it.secondID }))
                matrix.map { it.firstID to it.secondID }
                        .requireDistinct { "values of '$it' are duplicated comparision for '$k'" }
                matrix.map { it.value }.chunked(diagonal.size).also {
                    require(it.size == it.last().size) {
                        "dimensions of '$k' are not equal; missing comparisons: ${getMissingValues(matrix, diagonal)}"
                    }
                    require(it.size == diagonal.size) {
                        "too few elements for '$k'; missing comparisons: ${getMissingValues(matrix, diagonal)}"
                    }
                }
            }

    private fun getMissingValues(matrix: List<RelationValue>, diagonal: List<RelationValue>): Set<Pair<String, String>> {
        val providedComparisons = matrix.map { uniformKey(it.firstID, it.secondID) }.toSet()
        val requiredComparisons = diagonal
                .dropLast(1)
                .mapIndexed { i, f ->
                    diagonal.drop(i + 1)
                            .map { s -> uniformKey(f.firstID, s.secondID) }
                }.flatten()
                .toSet()
        return requiredComparisons - providedComparisons
    }

    private fun uniformKey(firstID: String, secondID: String) =
            if (firstID < secondID) firstID to secondID else secondID to firstID

    private inline fun <T> List<T>.requireDistinct(onError: (T) -> String) {
        val set = mutableSetOf<T>()
        forEach {
            require(set.contains(it).not()) { onError(it) }
            set += it
        }
    }
}
