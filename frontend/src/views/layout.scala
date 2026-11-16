package muajam.frontend.views

import org.scalajs.dom
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*
import muajam.frontend.Route

private def navLink(
    label: String,
    href: String,
    isActive: Boolean
): TypedTag[dom.HTMLAnchorElement] =
  a(
    scalatags.JsDom.all.href := href,
    cls := (if (isActive) "nav-link active" else "nav-link")
  )(label)

def layout: TypedTag[dom.HTMLDivElement] =
  div(cls := "page")(
    div(id := "site-header")(renderHeader(Route.current)),
    div(id := "content")
  )

def renderHeader(page: Route.ClientRoute): TypedTag[dom.HTMLDivElement] = {
  val modulesActive = page match {
    case Route.ModulesPage | _: Route.ModulePage => true
    case _                                       => false
  }

  div(cls := "hero")(
    h1("Muajam"),
    tag("nav")(cls := "nav")(
      navLink(
        "Модули",
        "#/",
        isActive = modulesActive
      ),
      navLink(
        "Словарь",
        "#/dictionary",
        isActive = page == Route.DictionaryPage
      ),
      navLink(
        "Грамматика",
        "#/grammar",
        isActive = page == Route.GrammarPage
      )
    )
  )
}
