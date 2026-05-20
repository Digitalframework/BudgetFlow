package data

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.format
import kotlinx.datetime.LocalDateTime

class JsPlatform : Platform {
    override val name: String = "JS"
}

actual fun getPlatform(): Platform {
    return JsPlatform()
}

actual fun currentTimeMillis(): Long {
    return js("Date.now()") as Long
}

actual class DateParser actual constructor() {
    actual fun parseDateStr(raw: String): String? {
        return try {
            // Try parsing common date formats using JavaScript Date
            val date = js("new Date(raw)")
            if (js("isNaN(date.getTime())") as Boolean) {
                null
            } else {
                js("date.toISOString()") as String
            }
        } catch (e: Exception) {
            null
        }
    }
}
