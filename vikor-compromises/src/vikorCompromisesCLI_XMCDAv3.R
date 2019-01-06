# usage:
# R --slave --vanilla --file=vikorCompromisesCLI_XMCDAv2.R --args "[inDirectory]" "[outDirectory]"
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
source("vikorCompromises.R")
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
vetoFile <- "veto.xml"
sFile <- "s.xml"
rFile <- "r.xml"
qFile <- "q.xml"
rankingFile <- "ranking.xml"
compromiseSolutionFile <- "compromise_solution.xml"
messagesFile <- "messages.xml"
# the Java xmcda object for the output messages
xmcdaMessages<-.jnew("org/xmcda/XMCDA")
xmcdaData <- .jnew("org/xmcda/XMCDA")
loadXMCDAv3(xmcdaData, inDirectory, alternativesFile, mandatory = TRUE, xmcdaMessages, "alternatives")
loadXMCDAv3(xmcdaData, inDirectory, sFile, mandatory = TRUE, xmcdaMessages, "alternativesValues")
loadXMCDAv3(xmcdaData, inDirectory, rFile, mandatory = TRUE, xmcdaMessages, "alternativesValues")
loadXMCDAv3(xmcdaData, inDirectory, vetoFile, mandatory = TRUE, xmcdaMessages, "programParameters")
# if we have problem with the inputs, it is time to stop
if (xmcdaMessages$programExecutionResultsList$size() > 0){
  if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError()){
    writeXMCDA(xmcdaMessages, paste(outDirectory,messagesFile, sep="/"))
    stop(paste("An error has occured while loading the input files. For further details, see ", messagesFile, sep=""))
  }
}
# let's check the inputs and convert them into our own structures
inputs<-checkAndExtractInputs(xmcdaData)
if (xmcdaMessages$programExecutionResultsList$size()>0){
  if (xmcdaMessages$programExecutionResultsList$get(as.integer(0))$isError()){
    writeXMCDA(xmcdaMessages, paste(outDirectory,messagesFile, sep="/"))
    stop(paste("An error has occured while checking and extracting the inputs. For further details, see ", messagesFile, sep=""))
  }
}
# here we know that everything was loaded as expected
# now let's call the calculation method
results <- handleException(
  function() vikorCompromises(inputs$S, inputs$R, inputs$v),
  xmcdaMessages,
  humanMessage = "The calculation could not be performed, reason: "
)
if (is.null(results)){
  writeXMCDA(xmcdaMessages, paste(outDirectory,messagesFile, sep="/"))
  stop("Could not calculate vikor.")
}
# fine, now let's put the results into XMCDA structures
xResults = convert(results, xmcdaMessages)
if (is.null(xResults)){
  writeXMCDA(xmcdaMessages, paste(outDirectory,messagesFile, sep="/"))
  stop("Could not convert vikor results into XMCDA")
}
# and last, write them onto the disk
for (i in 1:length(xResults)){
  outputFilename = paste(outDirectory, paste(names(xResults)[i],".xml",sep=""), sep="/")
  tmp <- handleException(
    function() return(
      writeXMCDA(xResults[[i]], outputFilename, xmcda_v3_tag(names(xResults)[i]))
    ),
    xmcdaMessages,
    humanMessage = paste("Error while writing ", outputFilename,", reason :")
  )
  if (is.null(tmp)){
    writeXMCDA(xmcdaMessages, paste(outDirectory,messagesFile, sep="/"))
    stop("Error while writing ",outputFilename,sep="")
  }
}
tmp <- handleException(
  function() return(
    putProgramExecutionResult(xmcdaMessages, infos="")
  ),
  xmcdaMessages
)
if (is.null(tmp)){
  writeXMCDA(xmcdaMessages, paste(outDirectory,messagesFile, sep="/"))
  stop("Could not add methodExecutionResult to tree.")
}
tmp <- handleException(
  function() return(
    writeXMCDA(xmcdaMessages, paste(outDirectory,messagesFile, sep="/"))
  ),
  xmcdaMessages
)
if (is.null(tmp)){
  writeXMCDA(xmcdaMessages, paste(outDirectory,messagesFile, sep="/"))
  stop("Error while writing messages file.")
}
