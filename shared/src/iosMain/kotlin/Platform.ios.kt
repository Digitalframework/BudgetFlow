package data

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneWithName

class IosPlatform : Platform {
    override val name: String = "iOS"
}

actual fun getPlatform(): Platform {
    return IosPlatform()
}

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual class DateParser actual constructor() {
    private val formatter = NSDateFormatter().apply {
        timeZone = NSTimeZone.timeZoneWithName("UTC")!!
    }
    
    actual fun parseDateStr(raw: String): String? {
        return try {
            // Try common date formats
            val formats = listOf(
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "dd.MM.yyyy",
                "dd.MM.yyyy HH:mm:ss",
                "yyyy/MM/dd",
                "MM/dd/yyyy"
            )
            
            for (format in formats) {
                formatter.dateFormat = format
                val date = formatter.dateFromString(raw)
                if (date != null) {
                    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                    formatter.timeZone = NSTimeZone.timeZoneWithName("UTC")!!
                    return formatter.stringFromDate(date)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
