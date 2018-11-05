vikor = function(performanceTable, weights, criteriaTypes, v) {
    if (missing(performanceTable) || missing(weights) || missing(criteriaTypes) || missing(v))
        stop("usage:
            performanceTable - table n x m of alternatives (n) and values of their criteria (m).
            weights - vector of m positive values, each representing weight for criterum at the same index. Total equal to 1.
            criteriaTypes - vector of m values ['min','max'].
            v - veto threshold, value in range [0,1].
        ")
    if (!is.matrix(performanceTable))
        stop("argument 'performanceTable' must be a matrix with the criteria values of the alternatives")
    if (!is.vector(weights) || !all(weights > 0))
        stop("'weights' must be a positive values vector")
    if (abs(sum(weights) - 1) > 0.001)
        stop("The sum of criteria weights is not equal to 1")
    if (length(weights) != ncol(performanceTable))
        stop("Length of weights is not equal to the number of criteria")
    if (!is.vector(criteriaTypes) || !all(criteriaTypes == "max" | criteriaTypes == "min"))
        stop("'criteriaTypes' must be a vector containing only n values - 'max' or 'min'")
    if (length(criteriaTypes) != ncol(performanceTable))
        stop("Length of cryteria types vector is not equal to the number of criteria")
    if (v < 0 || v > 1)
        stop("A value for 'v' (veto) in [0,1] should be provided")

    #1. Ideal solutions
    idealSol = function(type1, type2) {
        as.integer(criteriaTypes == type1) * apply(performanceTable, 2, max) +
        as.integer(criteriaTypes == type2) * apply(performanceTable, 2, min)
    }
    positiveIdeal = idealSol("max", "min")
    negativeIdeal = idealSol("min", "max")

    #2. S and R index
    norm = function(x, w, p, n) {
        w * (p - x) / (p - n)
    }
    weightsVector = apply(performanceTable, 1, norm, weights, positiveIdeal, negativeIdeal)
    S = apply(weightsVector, 2, sum)
    R = apply(weightsVector, 2, max)


    #3. Q index
    Q = v * (S - min(S)) / (max(S) - min(S)) + (1 - v) * (R - min(R)) / (max(R) - min(R))

    #4. Checking if Q is valid
    isQValid = Q != "NaN" && Q != "Inf"
    QRanking = if (isQValid) {
        rank(Q, ties.method = "first")
    } else {
        rep("-", nrow(performanceTable))
    }
    #5. Ranking the alternatives
    compromiseSolution = if (isQValid) {
        sortedQRank = names(sort(QRanking))
        sortedSRank = names(sort(rank(S, ties.method = "first")))
        sortedRRank = names(sort(rank(R, ties.method = "first")))
        DQ = 1 / (nrow(performanceTable) - 1)
        isAcceptableAdvantage = Q[[sortedQRank[2]]] - Q[[sortedQRank[1]]] >= DQ
        isAcceptableStability = sortedQRank[1] == sortedSRank[1] || sortedQRank[1] == sortedRRank[1]
        if (!isAcceptableStability) {
            Filter(function(X) X - Q[sortedQRank[1]] < DQ, Q)
        } else if (!isAcceptableAdvantage) {
            sortedQRank[1:2]
        } else {
            sortedQRank[1]
        }
    } else {
        c()
    }

    list(
        ranking = data.frame(
            S = S,
            R = R,
            Q = Q,
            Ranking = QRanking
        ),
        compromiseSolutions = compromiseSolution
    )
}

d = matrix(c(1, 2, 5, 0, 3000, 3750, 4500, 3500), nrow = 4, ncol = 2)
colnames(d) = c('time', 'gain')
rownames(d) = c('seller', 'manager', 'developer', 'Chuck Norris')
w = c(0.5, 0.5)
cb = c('min', 'max')
v = 0.5
r = vikor(d, w, cb, v)
print(r)