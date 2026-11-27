package muajam.frontend.views.pages

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*
import muajam.frontend.api.ModulesApi
import muajam.frontend.utils.*
import muajam.frontend.views.components.wordCard
import muajam.shared.dto.ModuleResponse

def modulePage(
    target: dom.HTMLElement,
    id: Int,
    token: Int
): Unit = {
  target.innerHTML = ""
  target.appendChild(p(cls := "muted")("Загрузка модуля…").render)

  ModulesApi.loadModule(
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

  val studyButton: Modifier =
    if (module.words.isEmpty) frag()
    else
      a(
        href := s"#/modules/${module.id}/study",
        cls := "btn btn-primary btn-sm"
      )("Учить")

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
        span(cls := "badge")(wordsLabel(module.wordsCount)),
        studyButton,
        a(
          href := s"#/modules/${module.id}/edit",
          cls := "btn btn-ghost btn-sm"
        )("Редактировать")
      )
    ),
    wordGrid
  )
}
