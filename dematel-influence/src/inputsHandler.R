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

	xmcdaAlternativesMatrices = xmcdaData$alternativesMatricesList
	error = !validateSize(xmcdaAlternativesMatrices$size(), 1, "alternatives matrix") || error
	if (error)
		return(NULL)
	p = xmcdaAlternativesMatrices$get(as.integer(0))
	inputMatrix = processMatrix(p, alternatives)

	if (xmcdaMessages$programExecutionResultsList$size() > 0) {
		if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError())
			NULL
	} else {
		inputMatrix
	}
}