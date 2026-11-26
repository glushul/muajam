package muajam.frontend.views.components

import org.scalajs.dom
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*
import muajam.frontend.utils.{HarakatPrefs, wordsLabel}
import muajam.shared.dto.ModuleResponse

def moduleCard(module: ModuleResponse): TypedTag[dom.html.Anchor] = {
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

  a(href := s"#/modules/${module.id}", cls := "module-card")(
    div(cls := "module-header")(
      h2(module.name),
      span(cls := "badge")(wordsLabel(module.wordsCount))
    ),
    preview
  )
}
