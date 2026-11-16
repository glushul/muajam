package muajam.frontend.utils

import org.scalajs.dom
import scalatags.JsDom.all.*

def showMessage(
    target: dom.HTMLElement,
    message: String,
    className: String
): Unit = {
  target.innerHTML = ""
  target.appendChild(p(cls := className)(message).render)
}

object RenderState {
  private var token = 0

  def next(): Int = {
    token += 1
    token
  }

  def isCurrent(value: Int): Boolean =
    value == token
}
