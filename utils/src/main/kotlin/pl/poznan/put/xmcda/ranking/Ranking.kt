package pl.poznan.put.xmcda.ranking

interface Ranking<T : Alternative> : List<RankEntry<T>>
interface RankEntry<T : Alternative> {
    val alternative: T
    val value: Double
}

interface Alternative {
    val name: String
}
