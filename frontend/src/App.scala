package muajam.frontend

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.all.*
import muajam.frontend.utils.RenderState
import muajam.frontend.views.*
import muajam.frontend.views.pages.*
import scalatags.*
import muajam.frontend.Route.*

object App {
  def apply() = layout

  def start(): Unit = {
    renderRoute(Route.current)
    Route.onChange(renderRoute)
  }

  def rerender(): Unit = renderRoute(Route.current)

  private def renderRoute(page: Route.ClientRoute): Unit = {
    val token = RenderState.next()

    updateHeader(page)

    content.foreach { target =>
      page match {
        case ModulesPage =>
          modulesPage(target, token)
        case ModulePage(id) =>
          modulePage(target, id, token)
        case StudyModulePage(id) =>
          studyPage(target, id, token)
        case NewModulePage =>
          moduleFormPage(target, None, token)
        case EditModulePage(id, wordId) =>
          moduleFormPage(target, Some(id), token, wordId)
        case DictionaryPage =>
          dictionaryPage(target, token)
        case GrammarPage =>
          grammarPage(target)
        case NotFound =>
          notFound(target, "Страница не найдена")
      }
    }
  }

  private def updateHeader(page: Route.ClientRoute): Unit =
    Option(document.getElementById("site-header")).foreach { target =>
      target.innerHTML = ""
      target.appendChild(renderHeader(page).render)
    }

  private def content: Option[dom.HTMLElement] =
    Option(document.getElementById("content"))
      .map(_.asInstanceOf[dom.HTMLElement])
}
