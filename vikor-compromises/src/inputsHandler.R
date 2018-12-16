library(XMCDA3)

checkAndExtractInputs <- function(xmcdaData) {
	error <- FALSE
	noAlternativesSupplied = xmcdaData$alternatives$isEmpty()
	if (noAlternativesSupplied) {
		msg <- "Alternatives not supplied"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		error <- TRUE
	}

	error <- !validateSize(xmcdaData$programParametersList$size(), 1, "parameters list") || error
	error <- !validateAlternativesLists(xmcdaData, c("S", "R")) || error
	if (error)
		return(NULL)

	alternativesValues = extractAlternativesValues(xmcdaData, "(S and R)")
	s = alternativesValues[[1]]
	r = alternativesValues[[2]]

	parameters = extractProgramParameters(xmcdaData)
	v = parameters[[1]]$veto[[1]]
	if (is.null(v)) {
		msg <- "veto value has not been supplied"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		return(NULL)
	}
	per = xmcdaMessages$programExecutionResultsList
	if (!per$isEmpty())
		for (i in 0:(per$size() - 1)) {
			if (per$get(as.integer(i))$isError())
				return(NULL)
		}

	list(
		S = s,
		R = r,
		v = v
	)
}