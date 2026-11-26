package muajam.frontend.views.components

import org.scalajs.dom
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.*
import muajam.frontend.utils.HarakatPrefs
import muajam.shared.dto.WordResponse

def wordCard(word: WordResponse): TypedTag[dom.html.Div] = {
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
