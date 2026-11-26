package muajam.frontend.views.pages

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*
import muajam.frontend.api.ModulesApi
import muajam.frontend.utils.*
import muajam.frontend.views.components.moduleCard

def modulesPage(target: dom.HTMLElement, token: Int): Unit = {
  document.title = "Muajam"
  target.innerHTML = ""
  target.appendChild(p(cls := "muted")("Загрузка модулей…").render)

  ModulesApi.loadModules(
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
