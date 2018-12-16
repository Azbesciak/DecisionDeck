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

convert <- function(results, programExecutionResult) {
	xmcdaOutEffect = assignAlternatives(results$R, programExecutionResult)
	xmcdaInEffect = assignAlternatives(results$C, programExecutionResult)
	totalInfluence = assignAlternativesMatrix(results$T, programExecutionResult)
	if (is.null(xmcdaOutEffect) ||
		is.null(xmcdaInEffect) ||
		is.null(totalInfluence)) {
		NULL
	} else {
		list(
		out_effect = xmcdaOutEffect,
		in_effect = xmcdaInEffect,
		total_influence = totalInfluence
		)
	}
}
