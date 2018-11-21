# usage:
# R --slave --vanilla --file=vikorSRCLI_XMCDAv2.R --args "[inDirectory]" "[outDirectory]"
rm(list=ls())
# tell R to use the rJava package and the RXMCDA3 package
library(rJava)
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
source("vikorSR.R")
# restore the working directory so that relative paths passed as
# arguments work as expected
if (!is.null(script.wd)) setwd(script.wd)
# get the in and out directories from the arguments
inDirectory <- commandArgs(trailingOnly=TRUE)[1]
outDirectory <- commandArgs(trailingOnly=TRUE)[2]
dir.create(file.path(outDirectory), showWarnings = FALSE)
# Override the directories here: uncomment this when testing from inside R e.g.
# (uncomment this when testing from inside R e.g.)
#inDirectory <- "/path/to/vikor/tests/in1.v2"
#outDirectory <- "/path/to/vikor/tests/out_tmp/"
# filenames
alternativesFile <- "alternatives.xml"
criteriaFile <- "criteria.xml"
performanceFile <- "performance.xml"
weightsFile <- "weights.xml"
sFile <- "s.xml"
rFile <- "r.xml"
messagesFile <- "messages.xml"
# the Java xmcda object for the output messages
xmcdaMessages<-.jnew("org/xmcda/XMCDA")
xmcdaDatav2 <- .jnew("org/xmcda/v2/XMCDA")
xmcdaData <- .jnew("org/xmcda/XMCDA")
loadXMCDAv2(xmcdaDatav2, inDirectory, alternativesFile, mandatory = TRUE, xmcdaMessages,"alternatives")
loadXMCDAv2(xmcdaDatav2, inDirectory, criteriaFile, mandatory = TRUE, xmcdaMessages,"criteria")
loadXMCDAv2(xmcdaDatav2, inDirectory, performanceFile, mandatory = TRUE, xmcdaMessages,"performanceTable")
loadXMCDAv2(xmcdaDatav2, inDirectory, weightsFile, mandatory = TRUE, xmcdaMessages,"criteriaValues")
# if we have problem with the inputs, it is time to stop
if (xmcdaMessages$programExecutionResultsList$size() > 0){
  if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError()){
    writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
    stop(paste("An error has occured while loading the input files. For further details, see ", messagesFile, sep=""))
  }
}
# convert that to XMCDA v3
converter<-.jnew("org/xmcda/converters/v2_v3/XMCDAConverter")
xmcdaData <- handleException(
  function() return(
    converter$convertTo_v3(xmcdaDatav2)
  ),
  xmcdaMessages,
  humanMessage = "Could not convert inputs to XMCDA v3, reason: "
)
if (xmcdaMessages$programExecutionResultsList$size() > 0){
  if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError()){
    writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
    stop(paste("An error has occured while converting the inputs to XMCDA v3. For further details, see ", messagesFile, sep=""))
  }
}
# let's check the inputs and convert them into our own structures
inputs<-checkAndExtractInputs(xmcdaData)
if (xmcdaMessages$programExecutionResultsList$size()>0){
  if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError()){
    writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
    stop(paste("An error has occured while checking and extracting the inputs. For further details, see ", messagesFile, sep=""))
  }
}
# here we know that everything was loaded as expected
# now let's call the calculation method
results <- handleException(
  function() vikorSR(inputs$performanceTable, inputs$weights, inputs$criteriaTypes),
  xmcdaMessages,
  humanMessage = "The calculation could not be performed, reason: "
)
if (is.null(results)){
  writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
  stop("Calculation failed.")
}
# fine, now let's put the results into XMCDA structures
xResults = convert(results, xmcdaMessages)
if (is.null(xResults)){
  writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
  stop("Could not convert vikor results into XMCDA")
}
# and last, convert them to XMCDAv2 and write them onto the disk
for (i in 1:length(xResults)){
  outputFilename = paste(outDirectory, paste(names(xResults)[i],".xml",sep=""), sep="/")
  # convert current xResults to v2
  results_v2 <- .jnew("org/xmcda/v2/XMCDA")
  results_v2 <- handleException(
    function() return(
      converter$convertTo_v2(xResults[[i]])
    ),
    xmcdaMessages,
    humanMessage = paste("Could not convert ", names(xResults)[i], " into XMCDA_v2, reason: ", sep ="")
  )
  if (is.null(results_v2)){
    writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
    stop(paste("Could not convert ", names(xResults)[i], " into XMCDA_v2", sep =""))
  }
  # now write the converted result to the file
  parser2<-.jnew("org/xmcda/parsers/xml/xmcda_v2/XMCDAParser")
  tmp <- handleException(
     function() return(
       parser2$writeXMCDA(results_v2, outputFilename, .jarray(xmcda_v2_tag(names(xResults)[i])))
     ),
     xmcdaMessages,
     humanMessage = paste("Error while writing ", outputFilename, " reason: ", sep="")
   )
   if (xmcdaMessages$programExecutionResultsList$size()>0){
     if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError()){
       writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
       stop(paste("Error while writing ", outputFilename, sep=""))
     }
   }
}
tmp <- handleException(
  function() return(
    putProgramExecutionResult(xmcdaMessages, infos="")
  ),
  xmcdaMessages
)
if (is.null(tmp)){
  writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
  stop("Could not add methodExecutionResult to tree.")
}
tmp <- handleException(
  function() return(
    writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
  ),
  xmcdaMessages
)
if (xmcdaMessages$programExecutionResultsList$size()>0){
  if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError()){
    writeXMCDAv2(xmcdaMessages, paste(outDirectory, messagesFile, sep="/"))
    stop("Error while writing messages file.")
  }
}
