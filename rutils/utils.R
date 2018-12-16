loadXMCDAv3 <- function(xmcdaData, inDirectory, filename, mandatory, programExecutionResult, tag){
	if (! file.exists(paste(inDirectory, filename, sep = "/")))
	{
		if (mandatory) {
			putProgramExecutionResult(programExecutionResult, errors = paste("Could not find the mandatory file ", filename, sep = ""))
		}
	} else {
		handleException(
		function() return(
		readXMCDA(file = paste(inDirectory, filename, sep = "/"), xmcda = xmcdaData, tag = tag)
		),
		programExecutionResult,
		humanMessage = paste("Unable to read & parse the file ", filename, ", reason: ", sep = "")
		)
	}
}

loadXMCDAv2 <- function(xmcdaData_v2, inDirectory, filename, mandatory, programExecutionResult, tag){
	if (! file.exists(paste(inDirectory, filename, sep = "/"))) {
		if (mandatory) {
			putProgramExecutionResult(programExecutionResult, errors = paste("Could not find the mandatory file ", filename, sep = ""))
		}
	} else {
		handleException(
		function() return(
		xmcdaData_v2 <- readXMCDAv2_and_update(xmcda_v2 = xmcdaData_v2, file = paste(inDirectory, filename, sep = "/"), tag = tag)
		),
		programExecutionResult,
		humanMessage = paste("Unable to read & parse the file ", filename, ", reason: ", sep = "")
		)
	}
}

readXMCDAv2_and_update <- function(xmcda_v2 = NULL, file, tag){
	if (is.null(xmcda_v2))xmcda_v2 <- .jnew("org/xmcda/v2/XMCDA")
	parser2 <- .jnew("org/xmcda/parsers/xml/xmcda_v2/XMCDAParser")
	new_xmcda <- parser2$readXMCDA(file, .jarray(c(tag)))
	new_content <- new_xmcda$getProjectReferenceOrMethodMessagesOrMethodParameters()
	xmcda_v2$getProjectReferenceOrMethodMessagesOrMethodParameters()$addAll(new_content)
	return(xmcda_v2)
}

writeXMCDAv2 <- function(xmcda, filename){
	xmcda_v2 <- .jnew("org/xmcda/v2/XMCDA")
	converter <- .jnew("org/xmcda/converters/v2_v3/XMCDAConverter")
	xmcda_v2 <- converter$convertTo_v2(xmcda)
	parser2 <- .jnew("org/xmcda/parsers/xml/xmcda_v2/XMCDAParser")
	parser2$writeXMCDA(xmcda_v2, paste(filename, sep = "/"))
}

mergeVectors <- function(names, values) setNames(as.character(values), names)

validateSize <- function(size, expectedSize, objectName) {
	hasOrHave = if (expectedSize == 1) "has" else "have"
	if (size == 0) {
		msg <- paste(objectName, hasOrHave, "not been supplied")
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

getMatrixValue = function(xmcdaMat, a1, a2) {
	value = xmcdaMat$get(a1, a2)
	if (is.null(value)) {
		msg <- paste("The alternatives matrix does not contain value for", a1$id(), "-", a2$id())
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		NULL
	} else if (! value$isValid()) {
		msg <- paste("The alternatives matrix has invalid value for", a1$id(), "-", a2$id())
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		NULL
	} else if (! value$isNumeric()) {
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

processMatrix <- function(xmcdaMat, alternatives, no = 1) {
	alts = alternatives[no]$alternatives
	alternativeNames = map(alts, function(a) a$id())
	res = outer(alts, alts, Vectorize(function(a1, a2) getMatrixValue(xmcdaMat, a1, a2)))
	colnames(res) = alternativeNames
	rownames(res) = alternativeNames
	res
}

qualifiedValue <- function(value) {
	javaValues <- .jnew("org/xmcda/QualifiedValues")
	javaValue <- .jnew("org/xmcda/QualifiedValue")
	if (is.integer(value))
	javaValue$setValue(.jnew("java/lang/Integer", value))
	else if (is.logical(value))
	javaValue$setValue(.jnew("java/lang/Boolean", value))
	else if (is.double(value))
	javaValue$setValue(.jnew("java/lang/Double", value))
	javaValues$add(javaValue)
	javaValues
}

putAlternativesMatrix <- function(xmcdaResult, matrix) {
	javaAlternativesAssignments <- .jnew("org/xmcda/AlternativesMatrix")
	for (i in rownames(matrix)) {
		ai = .jcast(.jnew("org/xmcda/Alternative", i), "java/lang/Object")
		for (j in colnames(matrix)) {
			mval = matrix[i, j]
			if (!is.na(mval)) {
				aj = .jcast(.jnew("org/xmcda/Alternative", j), "java/lang/Object")
				coord = .jnew("org/xmcda/utils/Coord", ai, aj)
				value = qualifiedValue(mval)
				javaAlternativesAssignments$put(coord, value)
			}
		}
	}
	xmcdaResult$alternativesMatricesList$add(javaAlternativesAssignments)
}

assignXMCDA <- function (values, programExecutionResult, onErrorMessage, fun) {
	xmcdaResult <- .jnew("org/xmcda/XMCDA")
	tmp <- handleException(
	function() fun(xmcdaResult, values),
	programExecutionResult,
	humanMessage = onErrorMessage
	)
	# if an error occurs, return null, else a dictionnary "xmcdaTag -> xmcdaObject"
	if (is.null(tmp)) {
		NULL
	} else {
		xmcdaResult
	}
}

processAlternatives <- function(xmcdaData) {
	activeAlternatives <- handleException(
	function() return(getActiveAlternatives(xmcdaData)),
	xmcdaMessages,
	humanMessage = "Unable to extract the active alternatives, reason: "
	)
}

extractAlternativesValues <- function(xmcdaData, expectedVectorsStr = "") {
	activeAlternatives <- handleException(
	function() getNumericAlternativesValuesList (xmcdaData),
	xmcdaMessages,
	humanMessage = paste("Unable to extract the alternatives values",
	expectedVectorsStr, ", reason: ")
	)
}

assignAlternatives <- function(values, programExecutionResult) {
	assignXMCDA(values, programExecutionResult, "Could not put overall values in tree, reason: ", putAlternativesValues)
}

assignAlternativesMatrix <- function(values, programExecutionResult) {
	assignXMCDA(values, programExecutionResult, "Could not put relations, reason: ", putAlternativesMatrix)
}

validateAlternativesLists <- function(xmcdaData, expectedVectors) {
	if (! is.vector(expectedVectors))
	stop("expectedVectors is require to be a vector of strings")
	valid = validateSize(xmcdaData$alternativesValuesList$size(), length(expectedVectors),
		paste("alternatives values lists - ", paste(expectedVectors, collapse = ", ")))
	if (!valid) return(FALSE)
	activeAlts = xmcdaData$alternatives$getActiveAlternatives()
	for (i in 0 : (xmcdaData$alternativesValuesList$size() - 1)) {
		altValList = xmcdaData$alternativesValuesList$get(as.integer(i))
		currentAltList = expectedVectors[i + 1]
		if (! altValList$isNumeric()) {
			msg <- paste(currentAltList, "is not numeric")
			putProgramExecutionResult(xmcdaMessages, errors = msg)
			valid <- FALSE
		}
		if (altValList$size() != activeAlts$size()) {
			msg <- paste(currentAltList, "has invalid size; alternatives", activeAlts$size(), altValList, ",",
			currentAltList, altValList$size(), altValList)
			putProgramExecutionResult(xmcdaMessages, errors = msg)
			valid <- FALSE
		}
		for (j in 0 : (activeAlts$size() - 1)) {
			alt = activeAlts$get(as.integer(j))
			value = altValList$get(alt)
			if (is.null(value) || value$size() == 0) {
				msg <- paste(currentAltList, "has no value for alternative", alt$id())
				putProgramExecutionResult(xmcdaMessages, errors = msg)
				valid <- FALSE
			} else if (value$size() > 1) {
				msg <- paste(currentAltList, "is expected to have single value for alternative", alt$id())
				putProgramExecutionResult(xmcdaMessages, errors = msg)
				valid <- FALSE
			}
		}
	}
	valid
}

extractProgramParameters <- function(xmcdaData) {
	activeAlternatives <- handleException(
	function() getProgramParametersList(xmcdaData),
	xmcdaMessages,
	humanMessage = "Unable to extract the program parameters, reason: "
	)
}