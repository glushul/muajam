package muajam.frontend.views

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.all.*
import muajam.frontend.api.loadWords
import muajam.frontend.utils.*
import muajam.shared.WordResponse
import muajam.shared.stripHarakat

private val russianLetters: Seq[String] =
  "А Б В Г Д Е Ё Ж З И Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш Щ Ъ Ы Ь Э Ю Я"
    .split(' ')
    .toSeq

private var selectedLetter: Option[String] = None
private var searchQuery: String = ""

def dictionaryPage(target: dom.HTMLElement, token: Int): Unit = {
  document.title = "Словарь — Muajam"
  target.innerHTML = ""
  target.appendChild(p(cls := "muted")("Загрузка слов…").render)

  loadWords(
    words => {
      if (RenderState.isCurrent(token)) {
        target.innerHTML = ""
        if (words.isEmpty) {
          target.appendChild(p(cls := "muted")("Словарь пока пуст.").render)
        } else {
          target.appendChild(dictionary(words))
        }
      }
    },
    message => {
      if (RenderState.isCurrent(token)) showMessage(target, message, "error")
    }
  )
}

private def dictionary(words: List[WordResponse]): dom.HTMLElement = {
  final case class Entry(
      element: dom.HTMLLIElement,
      letter: String,
      searchText: String
  )

  val entries = words.map { word =>
    val arabicGroup = frag(
      HarakatPrefs.pluralOf(word).map { plural =>
        span(cls := "dictionary-plural")(plural)
      },
      span(cls := "dictionary-arabic")(HarakatPrefs.of(word))
    )

    val element = li(cls := "dictionary-item")(
      span(cls := "dictionary-arabic-group")(arabicGroup),
      span(cls := "dictionary-translation")(word.translation)
    ).render

    val letter =
      word.translation.trim.headOption.map(_.toUpper.toString).getOrElse("")
    val text = stripHarakat(
      (Seq(word.translation, word.arabic, word.withoutHarakat) ++
        word.plural.toSeq ++ word.pluralWithoutHarakat.toSeq).mkString(" ")
    )

    Entry(element, letter, text)
  }

  val badge = span(cls := "badge")(s"${entries.length} слов").render

  val search = input(
    `type` := "search",
    cls := "dictionary-search",
    placeholder := "Поиск по слову или переводу…",
    value := searchQuery
  ).render

  val letterButtons = russianLetters.map { letter =>
    val button =
      scalatags.JsDom.all
        .button(
          `type` := "button",
          cls := "letter-btn",
          attr("aria-pressed") := "false"
        )(letter)
        .render
    (letter, button)
  }

  val empty = p(cls := "muted dictionary-empty")("Ничего не найдено.").render
  empty.classList.add("is-hidden")

  def applyFilters(): Unit = {
    val needle = stripHarakat(searchQuery.trim)

    var visible = 0
    entries.foreach { entry =>
      val show = selectedLetter.forall(_ == entry.letter) &&
        (needle.isEmpty || entry.searchText.contains(needle))
      entry.element.classList.toggle("is-hidden", !show)
      if (show) visible += 1
    }

    empty.classList.toggle("is-hidden", visible > 0)
    badge.textContent =
      if (selectedLetter.isEmpty && needle.isEmpty) s"${entries.length} слов"
      else s"$visible из ${entries.length}"

    letterButtons.foreach { case (letter, button) =>
      val active = selectedLetter.contains(letter)
      button.classList.toggle("active", active)
      button.setAttribute("aria-pressed", active.toString)
    }
  }

  search.addEventListener(
    "input",
    (_: dom.Event) => {
      searchQuery = search.value
      applyFilters()
    }
  )

  letterButtons.foreach { case (letter, button) =>
    button.addEventListener(
      "click",
      (_: dom.Event) => {
        selectedLetter =
          if (selectedLetter.contains(letter)) None else Some(letter)
        applyFilters()
      }
    )
  }

  val root = div(cls := "dictionary")(
    div(cls := "dictionary-header")(
      h2("Словарь"),
      badge
    ),
    div(cls := "dictionary-toolbar")(
      search,
      div(cls := "letter-buttons")(letterButtons.map(_._2)*)
    ),
    ul(cls := "dictionary-list")(entries.map(_.element)*),
    empty
  ).render

  applyFilters()
  root
}
