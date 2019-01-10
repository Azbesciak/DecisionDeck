# It is not an optimization - R will evaluate `(1 - v) * (R - minR) / (maxR - minR)`
# even if (1-v) == 0, so in result we will receive NaN.
# Same applies to the case with v == 0.
.computeQValue = function(S, R, v) {
	minS = min(S)
	maxS = max(S)
	if (minS == maxS && v != 0)
	stop("Could not compute Q vector: extremes in vector S are equal")

	minR = min(R)
	maxR = max(R)
	if (minR == maxR && v != 1)
	stop("Could not compute Q vector: extremes in vector R are equal")

	if (v == 1)
		(S - minS) / (maxS - minS)
	else if (v == 0)
		(R - minR) / (maxR - minR)
	else
		v * (S - minS) / (maxS - minS) + (1 - v) * (R - minR) / (maxR - minR)
}

.rank = function(V) names(sort(rank(V, ties.method = "first")))

vikorCompromises = function(S, R, v) {
	if (missing(S) || missing(R) || missing(v))
	stop("usage:
            S - weighted and normalized Manhattan distance ranking by minimum value
            R - weighted and normalized Chebyshev distance ranking by minimum value
            v - weight of the strategy of S and R [0,1].
        ")
	if (is.null(S))
	stop("'S' is null")
	if (is.null(R))
	stop("'R' is null")
	if (is.null(v))
	stop("'v' is null")
	if (! is.vector(S) ||
		! all(S >= 0) ||
		! all(is.numeric(S)))
	stop("'S' must be a non-negative values vector")
	if (! is.vector(R) ||
		! all(R >= 0) ||
		! all(is.numeric(R)))
	stop("'R' must be a non-negative values vector")
	if (v < 0 || v > 1 || ! is.numeric(v))
	stop("A numeric value for 'v' (veto) in [0,1] should be provided")

	S = S[order(names(S))]
	R = R[order(names(R))]

	#3. Q index
	Q = .computeQValue(S, R, v)
	#4. Checking if Q is valid
	if (Q == "NaN" || Q == "Inf")
		stop("Could not compute compromises: Q vector contains INF or NaN")
	#5. Ranking the alternatives
	sortedQ = sort(Q)
	sortedS = sort(S)
	sortedR = sort(R)
	DQ = 1 / (length(R) - 1)
	isAcceptableAdvantage = sortedQ[2] - sortedQ[1] >= DQ
	isAcceptableStability = names(sortedQ[1]) == names(sortedS[1]) ||
							names(sortedQ[1]) == names(sortedR[1])
	compromiseSolutions = if (! isAcceptableStability && isAcceptableAdvantage) {
		sortedQ[1 : 2]
	} else if (! isAcceptableAdvantage) {
		names(Filter(function(X) X - sortedQ[[1]] < DQ, Q))
	} else {
		sortedQ[1]
	}
	list(
		Q = sortedQ,
		compromiseSolutions = compromiseSolutions
	)
}