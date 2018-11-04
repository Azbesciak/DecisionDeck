library(XMCDA3)
library(purrr)

getMatrixValue = function(xmcdaMat, a1, a2) {
	value = xmcdaMat$get(a1, a2)
	if (is.null(value)) {
		msg <- paste("The alternatives matrix does not contain value for", a1$id(), "-", a2$id())
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		NULL
	} else if (!value$isValid()) {
		msg <- paste("The alternatives matrix has invalid value for", a1$id(), "-", a2$id())
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		NULL
	} else if (!value$isNumeric()) {
		msg <- paste("The alternatives matrix has non numeric value for", a1$id(), "-", a2$id())
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		NULL
	} else if (value$size() != 1) {
		msg <- paste("The alternatives matrix can contain only single values", a1$id(), "-", a2$id())
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		NULL
	} else {
		value$get(as.integer(0))$getValue()
	}
}

processMatrix <- function(xmcdaMat, alternatives) {
	alts = alternatives[1]$alternatives
	alternativeNames = map(alts, function(a) a$id())
	res = outer(alts, alts, Vectorize(function(a1, a2) getMatrixValue(xmcdaMat, a1, a2)))
	colnames(res) = alternativeNames
	rownames(res) = alternativeNames
	res
}

processAlternatives <- function(xmcdaData) {
	activeAlternatives <- handleException(
		function() return(getActiveAlternatives(xmcdaData)),
			xmcdaMessages,
			humanMessage = "Unable to extract the active alternatives, reason: "
		)
}

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
	providedMatrices = xmcdaAlternativesMatrices$size()
	if (providedMatrices == 0) {
		msg <- "No alternatives matrix has been supplied"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		return()
	} else if (providedMatrices > 1) {
		msg <- "More than one alternatives matrix has been supplied"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		return()
	}

	p = xmcdaAlternativesMatrices$get(as.integer(0))
	inputMatrix = processMatrix(p, alternatives)

	if (xmcdaMessages$programExecutionResultsList$size() > 0) {
		if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError())
			NULL
	} else {
		inputMatrix
	}
}