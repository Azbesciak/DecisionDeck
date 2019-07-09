package pl.poznan.put.xmcda

data class WarnException(override val message: String, val result: Any): Throwable(message)
