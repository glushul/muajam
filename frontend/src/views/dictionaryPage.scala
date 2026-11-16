package muajam.frontend.views

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*
import muajam.frontend.api.loadWords
import muajam.frontend.utils.*
import muajam.shared.WordResponse

def dictionaryPage(target: dom.HTMLElement, token: Int): Unit = {
  document.title = "Словарь — Muajam"
  target.innerHTML = ""
  target.appendChild(p(cls := "muted")("Загрузка слов…").render)

  loadWords(
    words => {
      if (RenderState.isCurrent(token)) {
        target.innerHTML = ""

        if (words.isEmpty) {
          target.appendChild(
            p(cls := "muted")("Словарь пока пуст.").render
          )
        } else {
          target.appendChild(dictionary(words).render)
        }
      }
    },
    message => {
      if (RenderState.isCurrent(token)) showMessage(target, message, "error")
    }
  )
}

private def dictionary(words: List[WordResponse]): TypedTag[dom.html.Div] = {
  val items = words.map { word =>
    val arabicGroup: Seq[Modifier] = word.plural.toSeq.map { plural =>
      span(cls := "dictionary-plural")(plural)
    } ++ Seq(span(cls := "dictionary-arabic")(word.arabic))

    li(cls := "dictionary-item")(
      span(cls := "dictionary-translation")(word.translation),
      span(cls := "dictionary-arabic-group")(arabicGroup*)
    )
  }

  div(cls := "dictionary")(
    div(cls := "dictionary-header")(
      span(cls := "badge")(s"${words.length} слов")
    ),
    ul(cls := "dictionary-list")(items*)
  )
}
