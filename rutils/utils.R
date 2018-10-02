loadXMCDAv3 <- function(xmcdaData, inDirectory, filename, mandatory, programExecutionResult, tag){
    if (! file.exists(paste(inDirectory, filename, sep = "/")))
    {
        if (mandatory)
        {
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
