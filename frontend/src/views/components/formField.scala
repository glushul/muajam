package muajam.frontend.views.components

import org.scalajs.dom
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*

def formField(
    labelText: String
)(inputElement: dom.HTMLInputElement): TypedTag[dom.html.Div] =
  div(cls := "form-field")(
    label(cls := "form-label")(labelText),
    inputElement
  )
