package components

import antd.Button
import antd.ButtonTypePrimary
import antd.CloudUploadOutlined
import antd.FilePdfOutlined
import antd.Upload
import antd.UploadProps
import design.T
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import utils.jso
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.FontWeight
import web.cssom.TextAlign
import web.cssom.px
import web.file.File

external interface UploadPanelProps : Props {
  var onUpload: (File) -> Unit
  var compact: Boolean?
}

private fun UploadProps.applyDraggerProps(onUpload: (File) -> Unit) {
  accept = ".pdf"
  multiple = true
  showUploadList = false
  beforeUpload = { file, _ ->
    onUpload(file)
    false
  }
}

/**
 * `compact` renders just the import button for the header; the full dropzone is
 * the empty state, so it stops eating half the screen once data exists.
 */
@JsName("UploadPanel")
val UploadPanel: FC<UploadPanelProps> = FC { props ->
  if (props.compact == true) {
    Upload {
      applyDraggerProps(props.onUpload)
      children = Fragment.create {
        Button {
          icon = CloudUploadOutlined.create()
          children = Fragment.create { +"PDF importieren" }
        }
      }
    }
  } else {
    div {
      className = ClassName("dropzone")
      style = jso {
        maxWidth = 520.px
        margin = "8vh auto 0".unsafeCast<web.cssom.Margin>()
      }

      Upload.Dragger {
        applyDraggerProps(props.onUpload)
        children = Fragment.create {
          div {
            style = jso { padding = "26px 16px".unsafeCast<web.cssom.Padding>() }

            div {
              style = jso {
                fontSize = 34.px
                color = Color(T.accent)
                marginBottom = 14.px
              }
              FilePdfOutlined {}
            }
            div {
              style = jso {
                fontSize = 16.px
                fontWeight = 600.unsafeCast<FontWeight>()
                color = Color(T.text)
              }
              +"Kontoauszug als PDF hier ablegen"
            }
            div {
              style = jso {
                color = Color(T.textMuted)
                fontSize = 13.px
                margin = "6px 0 18px".unsafeCast<web.cssom.Margin>()
                textAlign = TextAlign.center
              }
              +"Sparkasse, ING, DKB, Commerzbank, Volksbank & mehr."
              br {}
              +"Die Auswertung passiert lokal im Browser."
            }
            Button {
              type = ButtonTypePrimary
              icon = CloudUploadOutlined.create()
              children = Fragment.create { +"PDF auswählen" }
            }
          }
        }
      }
    }
  }
}
