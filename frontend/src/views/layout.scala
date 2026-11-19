package muajam.frontend.views

import org.scalajs.dom
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*
import muajam.frontend.App
import muajam.frontend.Route
import muajam.frontend.utils.HarakatPrefs

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
    div(id := "content"),
    harakatToggle()
  )

private def harakatToggle(): TypedTag[dom.HTMLLabelElement] =
  label(cls := "harakat-toggle")(
    input(
      `type` := "checkbox",
      if (HarakatPrefs.withoutHarakat) checked := true else (),
      onchange := { (event: dom.Event) =>
        val input = event.target.asInstanceOf[dom.HTMLInputElement]
        HarakatPrefs.setWithoutHarakat(input.checked)
        App.rerender()
      }
    ),
    span("Без огласовок")
  )

def renderHeader(page: Route.ClientRoute): TypedTag[dom.HTMLDivElement] = {
  val modulesActive = page match {
    case Route.ModulesPage | _: Route.ModulePage | Route.NewModulePage |
        _: Route.EditModulePage =>
      true
    case _ => false
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
