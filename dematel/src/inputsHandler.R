library(XMCDA3)
checkAndExtractInputs <- function(xmcdaData){
	error <- FALSE
	matrices = xmcdaData$alternativesMatricesList
	providedMatrices = matrices$size()
	print("alternatives")
	print(xmcdaData$alternatives)
	print("alternativesSets")
	print(xmcdaData$alternativesSets)
	print("criteria")
	print(xmcdaData$criteria)
	print("criteriaSets")
	print(xmcdaData$criteriaSets)
	print("categories")
	print(xmcdaData$categories)
	print("categoriesSets")
	print(xmcdaData$categoriesSets)
	print("performanceTablesList")
	print(xmcdaData$performanceTablesList)
	print("alternativesValuesList")
	print(xmcdaData$alternativesValuesList)
	print("alternativesSetsValuesList")
	print(xmcdaData$alternativesSetsValuesList)
	print("alternativesLinearConstraintsList")
	print(xmcdaData$alternativesLinearConstraintsList)
	print("alternativesSetsLinearConstraintsList")
	print(xmcdaData$alternativesSetsLinearConstraintsList)
	print("alternativesMatricesList")
	print(xmcdaData$alternativesMatricesList)
	print("alternativesSetsMatricesList")
	print(xmcdaData$alternativesSetsMatricesList)
	print("criteriaFunctionsList")
	print(xmcdaData$criteriaFunctionsList)
	print("criteriaScalesList")
	print(xmcdaData$criteriaScalesList)
	print("criteriaThresholdsList")
	print(xmcdaData$criteriaThresholdsList)
	print("criteriaValuesList")
	print(xmcdaData$criteriaValuesList)
	print("criteriaSetsValuesList")
	print(xmcdaData$criteriaSetsValuesList)
	print("criteriaLinearConstraintsList")
	print(xmcdaData$criteriaLinearConstraintsList)
	print("criteriaSetsLinearConstraintsList")
	print(xmcdaData$criteriaSetsLinearConstraintsList)
	print("criteriaMatricesList")
	print(xmcdaData$criteriaMatricesList)
	print("criteriaSetsMatricesList")
	print(xmcdaData$criteriaSetsMatricesList)
	print("alternativesCriteriaValuesList")
	print(xmcdaData$alternativesCriteriaValuesList)
	print("categoriesProfilesList")
	print(xmcdaData$categoriesProfilesList)
	print("alternativesAssignmentsList")
	print(xmcdaData$alternativesAssignmentsList)
	print("categoriesValuesList")
	print(xmcdaData$categoriesValuesList)
	print("categoriesSetsValuesList")
	print(xmcdaData$categoriesSetsValuesList)
	print("categoriesLinearConstraintsList")
	print(xmcdaData$categoriesLinearConstraintsList)
	print("categoriesSetsLinearConstraintsList")
	print(xmcdaData$categoriesSetsLinearConstraintsList)
	print("categoriesMatricesList")
	print(xmcdaData$categoriesMatricesList)
	print("categoriesSetsMatricesList")
	print(xmcdaData$categoriesSetsMatricesList)
	print("programParametersList")
	print(xmcdaData$programParametersList)
	print("programExecutionResultsList")
	print(xmcdaData$programExecutionResultsList)


	if (providedMatrices == 0) {
		msg <- "No alternatives matrix has been supplied"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		return()
	} else if (providedMatrices > 1) {
		msg <- "More than one alternatives matrix has been supplied"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		return()
	}

	p = matrices$get(as.integer(0))
	if (p$hasMissingValues()) {
		msg <- "The alternatives matrix has missing values"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
	}
	if (! p$isNumeric()) {
		msg <- "The alternatives matrix must contain numeric values only"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
	}
	inputMatrix = as.matrix(p)
	alternativesNumber = xmcdaData$alternatives$size()

	if (alternativesNumber > 1) {
		msg <- "More than one categories file supplied"
		putProgramExecutionResult(xmcdaMessages, errors = msg)
		error <- TRUE
	}

	# at this point there is nothing more we can check or do but abort the execution if an error has occured somewhere

	if (xmcdaMessages$programExecutionResultsList$size() > 0) {
		if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError())
		return(NULL)
	}
	#
	# pT <- getNumericPerformanceTableList(xmcdaData)[[1]]
	#
	# # first, get the alternatives & criteria from the performance table
	#
	# # the active criteria and alternatives
	#
	# activeAlternatives <- handleException(
	# function() return(
	# getActiveAlternatives(xmcdaData)
	# ),
	# xmcdaMessages,
	# humanMessage = "Unable to extract the active alternatives, reason: "
	# )
	#
	# activeCriteria <- handleException(
	# function() return(getActiveCriteria(xmcdaData)),
	# xmcdaMessages,
	# humanMessage = "Unable to extract the active criteria, reason: "
	# )
	#
	# # intersection between active criteria and those present in the weights and the performance table for the filtering
	#
	# filteredPerformanceTableCriteriaIDs <- intersect(activeCriteria$criteriaIDs, colnames(pT))
	#
	# # intersection between active alternatives and those present in the performance table
	#
	# filteredAlternativesIDs <- intersect(activeAlternatives$alternativesIDs, rownames(pT))
	#
	# # check that we still have active alternatives and criteria
	# if (length(filteredPerformanceTableCriteriaIDs) == 0) {
	#     msg <- "All criteria of the performance table are inactive"
	#     putProgramExecutionResult(xmcdaMessages, errors = msg)
	# }
	# if (length(filteredAlternativesIDs) == 0) {
	#     msg <- "All alternatives of the performance table are inactive"
	#     putProgramExecutionResult(xmcdaMessages, errors = msg)
	# }
	#
	# # check that criteria in weights has a non-null intersection with active criteria used in the performance table
	#
	# if (hasWeights) {
	#
	#     # get the criteria weights
	#
	#     criteriaWeights <- getNumericCriteriaValuesList(xmcdaData)[[1]]
	#
	#     # intersection between filtered criteria from the performance table and those from the weights vector
	#
	#     filteredCriteria <- intersect(filteredPerformanceTableCriteriaIDs, names(criteriaWeights))
	#
	#     if (length(filteredCriteria) == 0) {
	#         msg <- "The set of active criteria in perf.table has no common id in the set of the criteria ids used in the weights"
	#         putProgramExecutionResult(xmcdaMessages, errors = msg)
	#     }
	# }else {
	#     filteredCriteria <- filteredPerformanceTableCriteriaIDs
	# }
	#
	# # at this point there is nothing more we can check or do but abort the execution if an error has occured somewhere
	#
	# if (xmcdaMessages$programExecutionResultsList$size() > 0) {
	#     if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError())
	#     return(NULL)
	# }
	#
	# # build filtered performance table and weights vector (for all cases)
	#
	# filteredPerformanceTable <- pT[filteredAlternativesIDs, filteredCriteria]
	#
	# # check if valid weights vector was provided
	# if (hasWeights) {
	#     if (length(criteriaWeights) > 0)
	#     filteredCriteriaWeights <- criteriaWeights[filteredCriteria]
	# }

	return(list(mat = inputMatrix))
}