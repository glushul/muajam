package muajam.frontend.views

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*
import muajam.frontend.api.loadModule
import muajam.frontend.utils.*
import muajam.shared.{ModuleResponse, WordResponse}

def modulePage(
    target: dom.HTMLElement,
    id: Int,
    token: Int
): Unit = {
  target.innerHTML = ""
  target.appendChild(p(cls := "muted")("Загрузка модуля…").render)

  loadModule(
    id,
    module => {
      if (RenderState.isCurrent(token)) {
        module match {
          case Some(value) =>
            document.title = s"${value.name} — Muajam"
            target.innerHTML = ""
            target.appendChild(moduleDetail(value).render)
          case None =>
            notFound(target, "Модуль не найден")
        }
      }
    },
    message => {
      if (RenderState.isCurrent(token)) showMessage(target, message, "error")
    }
  )
}

private def moduleDetail(module: ModuleResponse): TypedTag[dom.html.Div] = {

  val wordGrid: Modifier =
    if (module.words.isEmpty) {
      p(cls := "muted")("В модуле пока нет слов.")
    } else {
      val cards = module.words.map(wordCard)
      div(cls := "word-grid")(cards*)
    }

  div(cls := "module-detail")(
    a(href := "#/", cls := "back-link")("← Все модули"),
    div(cls := "module-detail-header")(
      div(
        h2(module.name)
      ),
      div(cls := "module-detail-meta")(
        span(cls := "badge")(s"${module.wordsCount} слов"),
        a(
          href := s"#/modules/${module.id}/edit",
          cls := "btn btn-ghost btn-sm"
        )("Редактировать")
      )
    ),
    wordGrid
  )
}

private def wordCard(word: WordResponse): TypedTag[dom.html.Div] = {
  val pluralBlock: Modifier =
    HarakatPrefs.pluralOf(word) match {
      case Some(plural) =>
        div(cls := "plural-pair")(
          div(cls := "plural-item")(
            span(cls := "plural-label")("ед. ч."),
            span(cls := "plural-word")(HarakatPrefs.of(word))
          ),
          span(cls := "plural-arrow")("→"),
          div(cls := "plural-item")(
            span(cls := "plural-label")("мн. ч."),
            span(cls := "plural-word plural-word-highlight")(plural)
          )
        )
      case None =>
        div(cls := "plural-empty")("мн. ч. не указано")
    }

  div(cls := "word-card")(
    div(cls := "word-card-body")(
      div(cls := "word-arabic")(HarakatPrefs.of(word)),
      div(cls := "word-translation")(word.russian)
    ),
    pluralBlock
  )
}
