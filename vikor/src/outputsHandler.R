library(rJava)
library(XMCDA3)
XMCDA_v2_TAG_FOR_FILENAME <- list(
  # output name -> XMCDA v2 tag
  s = "alternativesValues",
  r = "alternativesValues",
  q = "alternativesValues",
  ranking = "alternativesValues",
  compromiseSolution = "alternatives",
  messages = "methodMessages"
)
XMCDA_v3_TAG_FOR_FILENAME <- list(
  # output name -> XMCDA v3 tag
  s = "alternativesValues",
  r = "alternativesValues",
  q = "alternativesValues",
  ranking = "alternativesValues",
  compromiseSolution = "alternatives",
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

getVec <- function(ranking, column) sort(setNames(as.character(ranking[[column]]), rownames(ranking)))

getXmcdaRanking <- function(ranking, column, programExecutionResult) {
  values = getVec(ranking, column)
  assignAlternatives(values, programExecutionResult)
}


getAlternativesNames <- function(alternativesValues, programExecutionResult){
  xmcdaResult <- .jnew("org/xmcda/XMCDA")
  tmp <- handleException(
    function() {
      xmcdaResult$alternatives<-.jnew("org/xmcda/Alternatives")
      for (i in 1:length(alternativesValues)){
        xmcdaResult$alternatives$add(.jnew("org/xmcda/Alternative",alternativesValues[i]))
      }
      xmcdaResult
    },
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

convert <- function(results, programExecutionResult) {
  s = getXmcdaRanking(results$ranking, "S", programExecutionResult)
  r = getXmcdaRanking(results$ranking, "R", programExecutionResult)
  q = getXmcdaRanking(results$ranking, "Q", programExecutionResult)
  ranking = getXmcdaRanking(results$ranking, "Ranking", programExecutionResult)
  compromises = getAlternativesNames(results$compromiseSolution, programExecutionResult)
  if (is.null(s) || is.null(r) || is.null(q) || is.null(ranking) || is.null(compromises)) {
    NULL
  } else{
    list(q = q, s = s, r = r, ranking = ranking, compromiseSolution = compromises)
  }
}
