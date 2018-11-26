displayDematelPlot <- function(x, y, df, T, alpha) {
    plot(x, y,
        type = 'p',
        main = "DEMATEL",
        sub = "The cause and effect diagram",
        xlab = "R + C",
        ylab = "R - C",
        cex = 2,

    )
    colors = c("#FF5722", "#FFC107", "#8BC34A", "#795548", "#9C27B0", "#03A9F4", "#009688", "#3F51B5")
    text(x, y, df$Criterion, pos=2, col=colors)
    abline(h = mean(y), v = mean(x))
    for (i in 1: nrow(T))
        for (j in 1: ncol(T))
            if (T[i,j] > alpha && i != j) {
                arrows(x[j],y[j],x[i],y[i],
                length = 0.15, angle = 45,
                code = 1, col = colors[i %% length(colors)])
            }
    View(df)
}

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
    alpha = mean(T)
    Criterion = colnames(D)
    df = data.frame(Criterion, R, C, x, y)
    colnames(df) <- c("Criterion", "R", "C", "R + C", "R - C")
    displayDematelPlot(x, y, df, T, alpha)
    df
}
#
# Z = as.matrix(read.csv("myfile.csv", header = TRUE, sep = ",", row.names = 1))
# value = dematel(Z)
# print(value)