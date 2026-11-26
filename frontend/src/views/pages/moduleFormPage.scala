package muajam.frontend.views.pages

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.all.*
import muajam.frontend.api.ModulesApi
import muajam.frontend.utils.{RenderState, showMessage}
import muajam.frontend.views.components.moduleForm
import muajam.shared.dto.{ModuleRequest, ModuleResponse}

def moduleFormPage(
    target: dom.HTMLElement,
    moduleId: Option[Int],
    token: Int,
    focusWordId: Option[Int] = None
): Unit = {
  val title =
    if (moduleId.isDefined) "Редактирование модуля" else "Новый модуль"

  document.title = s"$title — Muajam"
  target.innerHTML = ""

  moduleId match {
    case None =>
      renderForm(
        target,
        None,
        onCancel = () => dom.window.location.hash = "#/",
        focusWordId = focusWordId
      )
    case Some(id) =>
      target.appendChild(p(cls := "muted")("Загрузка модуля…").render)

      ModulesApi.loadModule(
        id,
        module => {
          if (RenderState.isCurrent(token)) {
            target.innerHTML = ""
            module match {
              case Some(value) =>
                renderForm(
                  target,
                  Some(value),
                  onCancel = () => dom.window.location.hash = s"#/modules/$id",
                  focusWordId = focusWordId
                )
              case None =>
                notFound(target, "Модуль не найден")
            }
          }
        },
        message => {
          if (RenderState.isCurrent(token))
            showMessage(target, message, "error")
        }
      )
  }
}

private def renderForm(
    target: dom.HTMLElement,
    module: Option[ModuleResponse],
    onCancel: () => Unit,
    focusWordId: Option[Int]
): Unit =
  moduleForm(
    target,
    module,
    onCancel = onCancel,
    onSave = (request: ModuleRequest, reportError: String => Unit) => {
      ModulesApi.saveModule(
        request,
        onSaved = () => {
          dom.window.location.hash =
            module.fold("#/")(value => s"#/modules/${value.id}")
          dom.window.location.reload()
        },
        onError = reportError
      )
    },
    focusWordId = focusWordId
  )
