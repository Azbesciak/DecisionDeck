# usage:
# R --slave --vanilla --file=dematelRelationshipCLI_XMCDAv2.R --args "[inDirectory]" "[outDirectory]"

rm(list = ls())

# tell R to use the rJava package and the RXMCDA3 package

library(rJava)
.jinit(parameters = "--add-modules=java.xml.bind")
library(XMCDA3)
# cf. http://stackoverflow.com/questions/1815606/rscript-determine-path-of-the-executing-script

script.dir <- function() {
	cmdArgs <- commandArgs(trailingOnly = FALSE)
	needle <- "--file="
	match <- grep(needle, cmdArgs)
	if (length(match) > 0) {
		# Rscript
		return(dirname(normalizePath(sub(needle, "", cmdArgs[match]))))
	} else {
		# 'source'd via R console
		return(dirname(normalizePath(sys.frames()[[1]]$ofile)))
	}
}
# load the R files in the script's directory
script.wd <- setwd(script.dir())
source("../../rutils/utils.R")
source("inputsHandler.R")
source("outputsHandler.R")
source("dematelRelationship.R")
# restore the working directory so that relative paths passed as
# arguments work as expected
if (! is.null(script.wd)) setwd(script.wd)

inDirectory <- commandArgs(trailingOnly = TRUE)[1]
outDirectory <- commandArgs(trailingOnly = TRUE)[2]
dir.create(file.path(outDirectory), showWarnings = FALSE)
messagesFile <- "messages.xml"
xmcdaMessages <- .jnew("org/xmcda/XMCDA")
xmcdaDatav2 <- .jnew("org/xmcda/v2/XMCDA")
xmcdaData <- .jnew("org/xmcda/XMCDA")

executionResults <- xmcdaMessages$programExecutionResultsList
validate <- function(message) {
	if (executionResults$size() > 0) {
		if (executionResults$get(as.integer(0))$isError()) {
			writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep = "/"))
			stop(message)
		}
	}
}

stopIfNull <- function(value, message) {
	if (is.null(value)) {
		writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep = "/"))
		stop(message)
	}
}

loadXMCDAv2(xmcdaDatav2, inDirectory, "alternatives.xml", mandatory = TRUE, xmcdaMessages, "alternatives")
loadXMCDAv2(xmcdaDatav2, inDirectory, "out_effect.xml", mandatory = TRUE, xmcdaMessages, "alternativesValues")
loadXMCDAv2(xmcdaDatav2, inDirectory, "in_effect.xml", mandatory = TRUE, xmcdaMessages, "alternativesValues")
loadXMCDAv2(xmcdaDatav2, inDirectory, "total_influence.xml", mandatory = TRUE, xmcdaMessages, "alternativesComparisons")
loadXMCDAv2(xmcdaDatav2, inDirectory, "alpha.xml", mandatory = FALSE, xmcdaMessages, "methodParameters")
# if we have problem with the inputs, it is time to stop
validate(paste("An error has occured while loading the input files. For further details, see ", messagesFile, sep = ""))
converter <- .jnew("org/xmcda/converters/v2_v3/XMCDAConverter")
# @formatter:off
xmcdaData <- handleException(
	function() return(converter$convertTo_v3(xmcdaDatav2)),
	xmcdaMessages,
	humanMessage = "Could not convert inputs to XMCDA v3, reason: "
)
# @formatter:on
validate(paste("An error has occured while converting the inputs to XMCDA v3. For further details, see", messagesFile))
inputs <- checkAndExtractInputs(xmcdaData)

validate(paste("An error has occured while checking and extracting the inputs. For further details, see", messagesFile))
# @formatter:off
results <- handleException(
	function() dematelRelationship(inputs$influenceMatrix, inputs$R, inputs$C, inputs$alpha),
	xmcdaMessages,
	humanMessage = "The calculation could not be performed, reason: "
)
# @formatter:on
stopIfNull(results, "Calculation failed.")

xResults = convert(results, xmcdaMessages)
stopIfNull(xResults, "Could not convert dematel results into XMCDA")
for (i in 1 : length(xResults)) {
	outputFilename = paste(outDirectory, paste(names(xResults)[i], ".xml", sep = ""), sep = "/")
	results_v2 <- .jnew("org/xmcda/v2/XMCDA")
	# @formatter:off
	results_v2 <- handleException(
		function() return(converter$convertTo_v2(xResults[[i]])),
		xmcdaMessages,
		humanMessage = paste("Could not convert ", names(xResults)[i], " into XMCDA_v2, reason: ", sep = "")
	)
	# formatter:on
	stopIfNull(results_v2, paste("Could not convert ", names(xResults)[i], " into XMCDA_v2", sep = ""))
	parser2 <- .jnew("org/xmcda/parsers/xml/xmcda_v2/XMCDAParser")
	# @formatter:off
	tmp <- handleException(
		function() return(
			parser2$writeXMCDA(results_v2, outputFilename, .jarray(xmcda_v2_tag(names(xResults)[i])))
		),
		xmcdaMessages,
		humanMessage = paste("Error while writing ", outputFilename, " reason: ", sep = "")
	)
	# @formatter:on
	validate(paste("Error while writing ", outputFilename, sep = ""))
}
# @formatter:off
tmp <- handleException(
	function() return(putProgramExecutionResult(xmcdaMessages, infos = "")), xmcdaMessages
)
# @formatter:off
stopIfNull(tmp, "Could not add methodExecutionResult to tree.")

# @formatter:off
tmp <- handleException(
	function() return(writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep = "/"))),
	xmcdaMessages
)
# @formatter:on
validate("Error while writing messages file.")