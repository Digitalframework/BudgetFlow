package data

/**
 * Expected platform interface for platform-specific implementations.
 */
interface Platform {
    val name: String
}

/**
 * Expected function to get the current platform.
 */
expect fun getPlatform(): Platform

/**
 * Expected function to get current timestamp in milliseconds.
 */
expect fun currentTimeMillis(): Long

/**
 * Expected function to parse date string in a platform-agnostic way.
 */
expect class DateParser () {
    fun parseDateStr(raw: String): String?
}
