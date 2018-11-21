vikorSR = function(performanceTable, weights, criteriaTypes) {
    if (missing(performanceTable) || missing(weights) || missing(criteriaTypes))
        stop("usage:
            performanceTable - table n x m of alternatives (n) and values of their criteria (m).
            weights - vector of m positive values, each representing weight for criterum at the same index. Total equal to 1.
            criteriaTypes - vector of m values ['min','max'].
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

    list(
        ranking = data.frame(
            S = S,
            R = R
        )
    )
}
