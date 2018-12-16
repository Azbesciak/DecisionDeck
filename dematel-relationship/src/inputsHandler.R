library(XMCDA3)
library(purrr)

checkAndExtractInputs <- function(xmcdaData) {
	error <- FALSE
	noAlternativesSupplied = xmcdaData$alternatives$isEmpty()
	if (noAlternativesSupplied) {
		msg <- "Alternatives not supplied"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		error <- TRUE
	}
	alternatives = processAlternatives(xmcdaData)
	error = !validateSize(xmcdaData$alternativesValuesList$size(), 2, "in and out effects") || error
	error = !validateSize(xmcdaData$alternativesMatricesList$size(), 1, "alternatives matrix") || error
	error <- !validateAlternativesLists(xmcdaData, c("in effect vector", "out effect vector")) || error
	if (error)
	return(NULL)

	alternativesValues = extractAlternativesValues(xmcdaData, "(in and out effect)")
	r = alternativesValues[[1]]
	c = alternativesValues[[2]]


	parameters = extractProgramParameters(xmcdaData)
	alpha = if (length(parameters) > 0) parameters[[1]]$alpha[[1]] else NULL
	if (!is.null(alpha) && !is.numeric(alpha)) {
		msg <- "alpha value has may be null or numeric only"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		return(NULL)
	}
	per = xmcdaMessages$programExecutionResultsList
	if (!per$isEmpty())
	for (i in 0:(per$size() - 1)) {
		if (per$get(as.integer(i))$isError())
		return(NULL)
	}
	if (error) {
		return(NULL)
	}

	matrix = xmcdaData$alternativesMatricesList$get(as.integer(0))
	influenceMatrix = processMatrix(matrix, alternatives)

	if (xmcdaMessages$programExecutionResultsList$size() > 0) {
		if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError())
			NULL
	} else {
		list(
		R = r,
		C = c,
		alpha = alpha,
		influenceMatrix = influenceMatrix
		)
	}
}