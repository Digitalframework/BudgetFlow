package data

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Android platform implementation.
 */
class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual class DateParser actual constructor() {
    @RequiresApi(Build.VERSION_CODES.O)
    actual fun parseDateStr(raw: String): String? {
        val match = Regex("""(\d{2})\.(\d{2})\.(\d{2,4})?""").find(raw) ?: return null
        val (_, dd, mm, yyyy) = match.destructured
        val year = if (yyyy.isNullOrBlank() || yyyy.length < 4) {
            LocalDate.now().year.toString()
        } else {
            yyyy
        }
        return "$year-$mm-$dd"
    }
}