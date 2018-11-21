library(XMCDA3)

extractProgramParameters <- function(xmcdaData) {
	activeAlternatives <- handleException(
		function() getProgramParametersList(xmcdaData),
		xmcdaMessages,
		humanMessage = "Unable to extract the program parameters, reason: "
	)
}

processAlternatives <- function(xmcdaData) {
	activeAlternatives <- handleException(
		function() getActiveAlternatives(xmcdaData),
		xmcdaMessages,
		humanMessage = "Unable to extract the active alternatives, reason: "
	)
}
getAlternativesValues <- function(xmcdaData) {
	activeAlternatives <- handleException(
		function() getNumericAlternativesValuesList (xmcdaData),
		xmcdaMessages,
		humanMessage = "Unable to extract the alternatives values (S and R), reason: "
	)
}

validateSize <- function(size, expectedSize, objectName) {
	if (size == 0) {
		msg <- paste(objectName, "has not been supplied")
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		FALSE
	} else if (size != expectedSize) {
		msg <- paste("Expected", expectedSize, "but found", size, objectName)
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		FALSE
	} else {
		TRUE
	}
}

checkAndExtractInputs <- function(xmcdaData) {
	error <- FALSE
	noAlternativesSupplied = xmcdaData$alternatives$isEmpty()
	if (noAlternativesSupplied) {
		msg <- "Alternatives not supplied"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		error <- TRUE
	}

	error <- !validateSize(xmcdaData$programParametersList$size(), 1, "parameters list") || error
	error <- !validateSize(xmcdaData$alternativesValuesList$size(), 2, "alternatives values lists - S and R") || error
	if (error)
		return(NULL)

	activeAlts = xmcdaData$alternatives$getActiveAlternatives()

	for (i in 0:(xmcdaData$alternativesValuesList$size()-1)) {
		altValList = xmcdaData$alternativesValuesList$get(as.integer(i))
		currentAltList = if (i == 1) "S" else "R"
		if (!altValList$isNumeric()) {
			msg <- paste(currentAltList, "is not numeric")
			putProgramExecutionResult(xmcdaMessages, errors = msg)
			error <- TRUE
		}
		if (altValList$size() != activeAlts$size()) {
			msg <- paste(currentAltList, "has invalid size; alternatives", activeAlts$size(), altValList, ",",
							currentAltList, altValList$size(), altValList)
			putProgramExecutionResult(xmcdaMessages, errors = msg)
			error <- TRUE
		}
		for (j in 0:(activeAlts$size() - 1)) {
			alt = activeAlts$get(as.integer(j))
			value = altValList$get(alt)
			if (is.null(value)) {
				msg <- paste(currentAltList, "has no value for alternative", alt$id())
				putProgramExecutionResult(xmcdaMessages, errors = msg)
				error <- TRUE
			}
		}
	}
	if (error)
		return(NULL)

	alternativesValues = getAlternativesValues(xmcdaData)
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