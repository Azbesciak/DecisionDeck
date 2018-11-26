vikorCompromises = function(S, R, v) {
    if (missing(S) || missing(R) || missing(v))
        stop("usage:
            S - weighted and normalized Manhattan distance ranking by minimum value.
            R - weighted and normalized Chebyshev distance ranking by minimum value
            v - veto threshold, value in range [0,1].
        ")
    if (is.null(S))
        stop("'S' is null")
    if (is.null(R))
        stop("'R' is null")
    if (is.null(v))
        stop("'v' is null")
    if (!is.vector(S) || !all(S >= 0))
        stop("'S' must be a non-negative values vector")
    if (!is.vector(R) || !all(R >= 0))
        stop("'R' must be a non-negative values vector")
    if (v < 0 || v > 1)
        stop("A value for 'v' (veto) in [0,1] should be provided")

    S = S[order(names(S))]
    R = R[order(names(R))]
    #3. Q index
    Q = v * (S - min(S)) / (max(S) - min(S)) + (1 - v) * (R - min(R)) / (max(R) - min(R))

    #4. Checking if Q is valid
    isQValid = Q != "NaN" && Q != "Inf"
    QRanking = if (isQValid) {
        rank(Q, ties.method = "first")
    } else {
        rep("-", length(R))
    }
    #5. Ranking the alternatives
    compromiseSolution = if (isQValid) {
        sortedQRank = names(sort(QRanking))
        sortedSRank = names(sort(rank(S, ties.method = "first")))
        sortedRRank = names(sort(rank(R, ties.method = "first")))
        DQ = 1 / (length(R) - 1)
        isAcceptableAdvantage = Q[[sortedQRank[2]]] - Q[[sortedQRank[1]]] >= DQ
        isAcceptableStability = sortedQRank[1] == sortedSRank[1] || sortedQRank[1] == sortedRRank[1]
        if (!isAcceptableStability && isAcceptableAdvantage) {
            sortedQRank[1:2]
        } else if (!isAcceptableAdvantage) {
            names(Filter(function(X) X - Q[sortedQRank[1]] < DQ, Q))
        } else {
            sortedQRank[1]
        }
    } else {
        c()
    }
    list(
        ranking = data.frame(
            Q = Q,
            Ranking = QRanking
        ),
        compromiseSolutions = compromiseSolution
    )
}