dematelInfluence = function(Z) {
    if (missing(Z))
    stop("usage:
            Z - n x n decision matrix
        ")
    if (! is.matrix(Z))
    stop("argument 'Z' must be a matrix with the values of the alternatives")
    Z = Z[order(rownames(Z)), order(colnames(Z))]
    if (nrow(Z) != ncol(Z) || !all.equal(colnames(Z), rownames(Z)))
        stop("column factors are not equal to row factors")
    if (! all(Z >= 0))
    stop("all values must be not negative")
    # step 2
    maxSum = 0
    for (i in 1 : nrow(Z)) {
        maxSum = max(maxSum, sum(Z[i,]))
    }
    for (i in 1 : ncol(Z)) {
        maxSum = max(maxSum, sum(Z[, i]))
    }
    D = Z / maxSum
    # step 3
    Ones = diag(nrow(D))
    T = D %*% solve(Ones - D) # solve with one arg makes inverse matrix

    # step 4
    R = c()
    for (i in 1 : nrow(T)) {
        R[i] = sum(T[i,])
    }
    C = c()
    for (i in 1 : ncol(T)) {
        C[i] = sum(T[, i])
    }

    Criterion = colnames(D)
    # df = data.frame(Criterion, R, C)
    list(
        T = T,
        R = mergeVectors(Criterion, R),
        C = mergeVectors(Criterion, C)
    )
    # colnames(df) <- c("Criterion", "R", "C")
    # df
}
