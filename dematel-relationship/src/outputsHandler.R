library(XMCDA3)
library(rJava)
library(purrr)

XMCDA_v2_TAG_FOR_FILENAME <- list(
prominence = "alternativesValues",
relation = "alternativesValues",
messages = "methodMessages"
)
XMCDA_v3_TAG_FOR_FILENAME <- list(
prominence = "alternativesValues",
relation = "alternativesValues",
messages = "programExecutionResult"
)
xmcda_v3_tag <- function(outputName) XMCDA_v3_TAG_FOR_FILENAME[[outputName]]

xmcda_v2_tag <- function(outputName) XMCDA_v2_TAG_FOR_FILENAME[[outputName]]


convert <- function(results, programExecutionResult){
	xmcdaProminence = assignAlternatives(results[["R + C"]], programExecutionResult)
	xmcdaRelation = assignAlternatives(results[["R - C"]], programExecutionResult)
	xmcdaRelationship = assignAlternativesMatrix(results$relationship, programExecutionResult)
	if (is.null(xmcdaProminence) ||
		is.null(xmcdaRelation) ||
		is.null(xmcdaRelationship)) {
		NULL
	} else {
		list(
		prominence = xmcdaProminence,
		relation = xmcdaRelation,
		relationship = xmcdaRelationship
		)
	}
}
