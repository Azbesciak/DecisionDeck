dematel = function(Z) {
    if (missing(Z))
    stop("usage:
            Z - n x n decision matrix
        ")
    if (! is.matrix(Z))
    stop("argument 'Z' must be a matrix with the values of the alternatives")
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

    x = R + C; y = R - C
    # step 5
    alpha = mean(T) # filter...?
    Criterion = colnames(D)
    df = data.frame(Criterion, R, C, x, y)
    df$Criterion = paste("", Criterion, sep = "    ")
    colnames(df) <- c("Criterion", "R", "C", "R + C", "R - C")
    plot(x, y, type = 'p',
    main = "DEMATEL", sub = "The cause and effect diagram", xlab = "R + C", ylab = "R - C", cex = 0.5)
    abline(h = mean(y), v = mean(x))
    text(x, y, df$Criterion)
    View(df)
}

Z = as.matrix(read.csv("myfile.csv", header = TRUE, sep = ",", row.names = 1))
dematel(Z)