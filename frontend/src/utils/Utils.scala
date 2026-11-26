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

def pluralRu(
    count: Int,
    one: String,
    few: String,
    many: String
): String = {
  val mod10 = count % 10
  val mod100 = count % 100
  if (mod10 == 1 && mod100 != 11) one
  else if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) few
  else many
}

def wordsLabel(count: Int): String =
  s"$count ${pluralRu(count, "слово", "слова", "слов")}"
