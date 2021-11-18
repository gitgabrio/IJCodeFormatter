
package net.cardosi.ijcodeformatter

import java.lang.Exception

class IJCodeFormatterException : Exception {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : super(cause) {}
}