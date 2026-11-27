package muajam.frontend.views.pages

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.all.*
import muajam.frontend.api.ModulesApi
import muajam.frontend.utils.*
import muajam.shared.dto.{ModuleResponse, WordResponse}

private var activeKeyHandler: Option[dom.KeyboardEvent => Unit] = None

private def installKeyboard(handler: dom.KeyboardEvent => Unit): Unit = {
  activeKeyHandler.foreach(document.removeEventListener("keydown", _))
  activeKeyHandler = Some(handler)
  document.addEventListener("keydown", handler)
}

def studyPage(
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
          case Some(value) if value.words.nonEmpty =>
            document.title = s"Учить: ${value.name} — Muajam"
            target.innerHTML = ""
            renderStudy(target, value)
          case Some(value) =>
            document.title = s"${value.name} — Muajam"
            target.innerHTML = ""
            target.appendChild(
              div(cls := "empty-state")(
                h2(value.name),
                p(cls := "muted")("В модуле пока нет слов для изучения."),
                a(href := s"#/modules/${value.id}", cls := "back-link")(
                  "← К модулю"
                )
              ).render
            )
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

private def renderStudy(
    target: dom.HTMLElement,
    module: ModuleResponse
): Unit = {

  def startSession(sessionWords: Vector[WordResponse]): Unit = {
    var index = 0
    var flipped = false
    val known = scala.collection.mutable.Set.empty[Int]
    val repeat = scala.collection.mutable.Set.empty[Int]

    def render(): Unit =
      if (index >= sessionWords.length) renderSummary()
      else renderCard()

    def answerCurrent(isKnown: Boolean): Unit =
      if (index < sessionWords.length) {
        val wordId = sessionWords(index).id
        if (isKnown) known += wordId else repeat += wordId
        index += 1
        flipped = false
        render()
      }

    def isInteractive(target: dom.EventTarget): Boolean =
      target match {
        case element: dom.Element =>
          element.tagName match {
            case "INPUT" | "TEXTAREA" | "SELECT" | "BUTTON" | "A" => true
            case _                                                => false
          }
        case _ => false
      }

    def renderCard(): Unit = {
      val word = sessionWords(index)
      val progress =
        (((index + 1).toDouble / sessionWords.length) * 100).toInt

      val content: Modifier =
        if (flipped) {
          val pluralBlock: Modifier =
            HarakatPrefs.pluralOf(word) match {
              case Some(plural) =>
                div(cls := "study-plural")(
                  span(cls := "plural-label")("мн. ч."),
                  span(cls := "plural-word plural-word-highlight")(plural)
                )
              case None => frag()
            }

          div(cls := "study-card-content")(
            div(cls := "study-card-label")("Перевод"),
            div(cls := "study-card-translation")(word.russian),
            pluralBlock
          )
        } else {
          div(cls := "study-card-content")(
            div(cls := "study-card-label")("Арабское слово"),
            div(cls := "study-card-arabic")(HarakatPrefs.of(word))
          )
        }

      val card = div(
        cls := "study-card",
        onclick := { (_: dom.Event) =>
          flipped = !flipped
          render()
        }
      )(content).render

      val actions = div(cls := "study-actions").render

      if (flipped) {
        val repeatButton = button(cls := "btn btn-ghost")("← Повторить").render
        repeatButton.addEventListener(
          "click",
          (_: dom.Event) => answerCurrent(false)
        )

        val knownButton = button(cls := "btn btn-primary")("Знаю →").render
        knownButton.addEventListener(
          "click",
          (_: dom.Event) => answerCurrent(true)
        )

        actions.appendChild(repeatButton)
        actions.appendChild(knownButton)
      } else {
        val showButton =
          button(cls := "btn btn-primary")("Показать перевод").render
        showButton.addEventListener(
          "click",
          (_: dom.Event) => {
            flipped = true
            render()
          }
        )
        actions.appendChild(showButton)
      }

      target.innerHTML = ""
      target.appendChild(
        div(cls := "study-page")(
          a(href := s"#/modules/${module.id}", cls := "back-link")(
            "← К модулю"
          ),
          div(cls := "study-header")(
            div(
              h2(module.name),
              p(cls := "muted")("Изучение слов")
            ),
            span(cls := "badge")(s"${index + 1} / ${sessionWords.length}")
          ),
          div(cls := "study-progress")(
            div(cls := "study-progress-fill", style := s"width: $progress%")()
          ),
          card,
          actions
        ).render
      )
    }

    def renderSummary(): Unit = {
      val repeatWords = sessionWords.filter(word => repeat.contains(word.id))
      val knownCount = sessionWords.count(word => known.contains(word.id))

      val actions = div(cls := "study-summary-actions").render

      if (repeatWords.nonEmpty) {
        val repeatButton =
          button(cls := "btn btn-primary")("Повторить сложные").render
        repeatButton.addEventListener(
          "click",
          (_: dom.Event) => startSession(repeatWords)
        )
        actions.appendChild(repeatButton)
      }

      val againButton = button(cls := "btn btn-ghost")("Пройти заново").render
      againButton.addEventListener(
        "click",
        (_: dom.Event) => startSession(module.words.toVector)
      )
      actions.appendChild(againButton)

      actions.appendChild(
        a(href := s"#/modules/${module.id}", cls := "btn btn-ghost")(
          "К модулю"
        ).render
      )

      target.innerHTML = ""
      target.appendChild(
        div(cls := "study-page")(
          a(href := s"#/modules/${module.id}", cls := "back-link")(
            "← К модулю"
          ),
          div(cls := "study-summary")(
            h2("Готово!"),
            p(s"Знаю: $knownCount"),
            p(s"Повторить: ${repeatWords.length}"),
            actions
          )
        ).render
      )
    }

    val keyHandler: dom.KeyboardEvent => Unit = { (event: dom.KeyboardEvent) =>
      if (
        target.querySelector(".study-page") != null &&
        !isInteractive(event.target) &&
        !event.ctrlKey &&
        !event.metaKey &&
        !event.altKey
      ) {
        event.key match {
          case "Escape" =>
            dom.window.location.hash = s"#/modules/${module.id}"
          case _ if index < sessionWords.length && !event.repeat =>
            event.key match {
              case " " | "Enter" =>
                event.preventDefault()
                flipped = !flipped
                render()
              case "ArrowRight" =>
                event.preventDefault()
                if (flipped) answerCurrent(true)
                else ()
              case "ArrowLeft" =>
                event.preventDefault()
                if (flipped) answerCurrent(false)
                else ()
              case _ => ()
            }
          case _ => ()
        }
      }
    }

    HarakatPrefs.setChangeListener(
      Some(() =>
        if (target.querySelector(".study-page") != null) {
          render()
          true
        } else false
      )
    )

    installKeyboard(keyHandler)
    render()
  }

  startSession(module.words.toVector)
}
