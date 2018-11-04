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

assignAlternatives <- function(values, programExecutionResult) {
  xmcdaResult <- .jnew("org/xmcda/XMCDA")
  tmp <- handleException(
  function() putAlternativesValues(xmcdaResult, values),
    programExecutionResult,
    humanMessage = "Could not put overall values in tree, reason: "
  )
  # if an error occurs, return null, else a dictionnary "xmcdaTag -> xmcdaObject"
  if (is.null(tmp)){
    NULL
  } else {
    xmcdaResult
  }
}
convert <- function(results, programExecutionResult){
  prominence = setNames(as.character(results[["R + C"]]), results$Criterion)
  relation = setNames(as.character(results[["R - C"]]), results$Criterion)
  xmcdaProminence = assignAlternatives(prominence, programExecutionResult)
  xmcdaRelation = assignAlternatives(relation, programExecutionResult)
  if (is.null(xmcdaProminence) || is.null(xmcdaRelation)){
    NULL
  } else{
    list(prominence = xmcdaProminence, relation = xmcdaRelation)
  }
}
