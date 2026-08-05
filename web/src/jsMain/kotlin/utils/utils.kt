package utils

inline fun <T : Any> jso(block: T.() -> Unit = {}): T {
  return (js("{}") as T).apply(block)
}

/**
 * Like [jso], but hands the new object to the block as an explicit parameter.
 *
 * Use this for any object built *inside* another props DSL. With [jso] the new
 * object is an implicit receiver, so a name that also exists on the enclosing
 * props type (`value` on Segmented, `size`/`style` on Table, …) silently
 * resolves against that outer receiver and the property lands on the wrong
 * object.
 */
inline fun <T : Any> jsObject(block: (T) -> Unit): T {
  val target = js("{}") as T
  block(target)
  return target
}
