package components

import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.span
import web.cssom.ClassName

/**
 * Flat surface with an optional title row. Replaces the antd Card so the whole
 * overview shares one border-radius, one padding scale, one head style.
 */
external interface PanelProps : PropsWithChildren {
  var title: String?
  var extra: String?
  var style: dynamic
  var bodyStyle: dynamic
}

@JsName("Panel")
val Panel: FC<PanelProps> = FC { props ->
  div {
    className = ClassName("panel")
    style = props.style

    if (props.title != null || props.extra != null) {
      div {
        className = ClassName("panel-head")
        h3 {
          className = ClassName("panel-title")
          +(props.title ?: "")
        }
        props.extra?.let { extraText ->
          span {
            className = ClassName("panel-sub")
            +extraText
          }
        }
      }
    }

    div {
      className = ClassName("panel-body")
      style = props.bodyStyle
      props.children?.let { +it }
    }
  }
}
