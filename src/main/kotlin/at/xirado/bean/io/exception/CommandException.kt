package at.xirado.bean.io.exception

import at.xirado.bean.util.ResponseType

class CommandException(val responseType: ResponseType,
                       val key: String,
                       val supportButton: Boolean = false) : RuntimeException()