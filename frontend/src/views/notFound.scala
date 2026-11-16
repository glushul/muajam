package muajam.frontend.views

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.all.*

def notFound(
    target: dom.HTMLElement,
    title: String
): Unit = {
  document.title = "Muajam"
  target.innerHTML = ""
  target.appendChild(
    div(cls := "empty-state")(
      h2(title),
      a(href := "#/", cls := "back-link")("← Все модули")
    ).render
  )
}
