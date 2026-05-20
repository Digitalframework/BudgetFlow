package utils

inline fun <T : Any> jso(block: T.() -> Unit = {}): T {
  return (js("{}") as T).apply(block)
}