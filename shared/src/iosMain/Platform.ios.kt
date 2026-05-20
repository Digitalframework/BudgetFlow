package data

import platform.Foundation.NSDate
import platform.Foundation.NSString
import platform.Foundation.NSCharacterSet
import platform.Foundation.dateFormatter
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.timeIntervalSince1970

/**
 * iOS platform implementation.
 */
class IOSPlatform : Platform {
    override val name: String = "iOS ${platform.UIKit.UIDevice.currentDevice.systemVersion}"
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual class DateParser actual constructor() {
    actual fun parseDateStr(raw: String): String? {
        val match = Regex("""(\d{2})\.(\d{2})\.(\d{2,4})?""").find(raw) ?: return null
        val (_, dd, mm, yyyy) = match.destructured
        val year = if (yyyy.isNullOrBlank() || yyyy.length < 4) {
            NSDate().dateWithTimeIntervalSinceNow(0).toString().take(4)
        } else {
            yyyy
        }
        return "$year-$mm-$dd"
    }
}