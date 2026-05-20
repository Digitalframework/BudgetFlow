package components

import antd.Upload
import web.cssom.*
import utils.jso
import web.file.File
import react.FC
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

external interface UploadPanelProps : Props {
    var onUpload: (File) -> Unit
}

@JsName("UploadPanel")
val UploadPanel: FC<UploadPanelProps> = FC { props ->
    // 1. Replaces <Card> with a standard div styled exactly like the card
    div {
        style = jso {
            borderRadius = 16.px
            marginBottom = 24.px
            //border = "2px dashed #1677FF"
            borderRadius = 2.px
            background = Color("#111127")
            overflow = Overflow.hidden // Keeps the dragger inside the rounded corners
        }

        Upload {
            // Force it into Dragger mode safely bypassing strict enum types
            asDynamic().type = "drag"
            name = "file"
            accept = ".pdf"
            multiple = false
            showUploadList = false
            style = jso {
                background = Color("transparent")
                border = "none"
            }
            beforeUpload = { file, _ ->
                props.onUpload(file)
                false
            }

            // 2. Pure HTML elements inside Fragment to prevent "invoke" DSL errors
            children = Fragment.create {
                div {
                    style = jso {
                        padding = 32.px
                        textAlign = "center".unsafeCast<TextAlign>()
                    }
                    p {
                        style = jso {
                            fontSize = 48.px
                            margin = 0.px
                        }
                        +"📄"
                    }
                    p {
                        style = jso {
                            fontSize = 16.px
                            fontWeight = 600.unsafeCast<FontWeight>()
                            color = Color("#1677FF")
                            margin = "8px 0".unsafeCast<Margin>()
                        }
                        +"Kontoauszug PDF hier ablegen"
                    }
                    p {
                        style = jso {
                            fontSize = 13.px
                            color = Color("#888888")
                            marginBottom = 16.px
                        }
                        +"Sparkasse, ING, DKB, Commerzbank, Volksbank & mehr"
                    }
                    // Replaces the AntD <Button> with a beautifully styled div
                    div {
                        style = jso {
                            display = Display.inlineBlock
                            background = Color("#1677FF")
                            color = Color("#FFFFFF")
                            padding = "8px 16px".unsafeCast<Padding>()
                            borderRadius = 8.px
                            fontWeight = 500.unsafeCast<FontWeight>()
                            cursor = Cursor.pointer
                        }
                        +"☁️ PDF auswählen"
                    }
                }
            }
        }
    }
}