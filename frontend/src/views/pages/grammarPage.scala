package muajam.frontend.views.pages

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.all.*

def grammarPage(target: dom.HTMLElement): Unit = {
  document.title = "Грамматика — Muajam"
  target.innerHTML = ""
  target.appendChild(
    div(cls := "empty-state")(
      h2("Грамматика"),
      p(cls := "muted")("Раздел находится в разработке.")
    ).render
  )
}
