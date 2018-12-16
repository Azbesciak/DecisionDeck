displayDematelPlot <- function(x, y, T) {
	plot(x, y,
	type = 'p',
	main = "DEMATEL",
	sub = "The cause and effect diagram",
	xlab = "R + C",
	ylab = "R - C",
	cex = 2,
	)
	colors = c("#FF5722", "#FFC107", "#8BC34A", "#795548", "#9C27B0", "#03A9F4", "#009688", "#3F51B5")
	text(x, y, colnames(T), pos = 2, col = colors)
	abline(h = mean(y), v = mean(x))
	for (i in 1 : nrow(T))
	for (j in 1 : ncol(T))
	if (!is.na(T[i, j])) {
		arrows(x[j], y[j], x[i], y[i],
		length = 0.15, angle = 45,
		code = 1, col = colors[i %% length(colors)])
	}
}


dematelRelationship = function(T, R, C, alpha = NULL) {
	if (missing(R) || missing(C) || missing(T))
	stop("usage:
	T - n x n total influence matrix
	R - vector of influence by row
	C - vector of influence by column
	alpha - optional threshold; if not passed mean is used as default
	")

	if (! is.vector(R))
	stop("argument 'R' must be of type vector")
	if (! is.vector(C))
	stop("argument 'C' must be of type vector")
	if (! all(R >= 0))
	stop("vector R contains other than non-negative values")
	if (! all(C >= 0))
	stop("vector C contains other than non-negative values")
	if ((!is.null(alpha) && !is.numeric(alpha)) || (is.numeric(alpha) && alpha < 0))
	stop("alpha if passed must be non-negative real value")
	R = R[order(names(R))]
	C = C[order(names(C))]
	T = T[order(rownames(T)), order(colnames(T))]
	if (length(R) != length(C) || !all.equal(names(R), names(C)))
	stop("vectors of R and C have different factors")
	if (nrow(T) != ncol(T) || !all.equal(rownames(T), colnames(T)))
	stop("influence matrix has different factors in rows and columns")
	if (nrow(T) != length(R) || !all.equal(rownames(T), names(R)))
	stop("factors of influence matrix are not equal to factors in vectors R and C")
	x = R + C; y = R - C
	if (is.null(alpha)) {
		alpha = mean(T)
	}
	# step 5
	T[T < alpha] = NA
	# displayDematelPlot(x, y, T)

	list(
		"R + C" = x,
		"R - C" = y,
		relationship = T
	)
}