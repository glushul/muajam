package muajam.frontend.views

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*
import muajam.frontend.api.loadModules
import muajam.frontend.utils.*
import muajam.shared.ModuleResponse

def modulesPage(target: dom.HTMLElement, token: Int): Unit = {
  document.title = "Muajam"
  target.innerHTML = ""
  target.appendChild(p(cls := "muted")("Загрузка модулей…").render)

  loadModules(
    modules => {
      if (RenderState.isCurrent(token)) {
        target.innerHTML = ""
        target.appendChild(modulesHeader(modules.length).render)

        if (modules.isEmpty) {
          target.appendChild(
            p(cls := "muted")("Пока нет ни одного модуля.").render
          )
        } else {
          val grid = div(cls := "module-grid").render

          modules.foreach { module =>
            grid.appendChild(moduleCard(module).render)
          }

          target.appendChild(grid)
        }
      }
    },
    message => {
      if (RenderState.isCurrent(token)) showMessage(target, message, "error")
    }
  )
}

private def modulesHeader(count: Int): TypedTag[dom.html.Div] =
  div(cls := "page-header")(
    div(cls := "page-header-title")(
      h2("Модули"),
      span(cls := "badge")(
        s"$count ${pluralRu(count, "модуль", "модуля", "модулей")}"
      )
    ),
    a(href := "#/modules/new", cls := "btn btn-primary")(
      "+ Добавить модуль"
    )
  )

private def moduleCard(module: ModuleResponse): TypedTag[dom.html.Div] = {
  val preview: Modifier =
    if (module.words.isEmpty) {
      p(cls := "muted")("В модуле пока нет слов.")
    } else {
      val items = module.words.map { word =>
        li(
          span(cls := "word-arabic")(HarakatPrefs.of(word)),
          span(cls := "word-translation")(word.russian)
        )
      }
      ul(cls := "word-preview")(items*)
    }

  div(cls := "module-card")(
    a(href := s"#/modules/${module.id}", cls := "module-card-main")(
      div(cls := "module-header")(
        h2(module.name),
        span(cls := "badge")(s"${module.wordsCount} слов")
      ),
      preview
    )
  )
}
