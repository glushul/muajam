package muajam.frontend.views.components

import org.scalajs.dom
import scalatags.JsDom.all.*
import muajam.frontend.utils.wordsLabel
import muajam.shared.dto.{
  ModuleRequest,
  ModuleResponse,
  WordRequest,
  WordResponse
}

private final case class WordForm(
    id: Option[Int] = None,
    arabic: String = "",
    russian: String = "",
    plural: String = ""
)

private final case class ModuleFormState(
    id: Option[Int] = None,
    name: String,
    words: Vector[WordForm]
)

def moduleForm(
    target: dom.HTMLElement,
    module: Option[ModuleResponse],
    onCancel: () => Unit,
    onSave: (ModuleRequest, String => Unit) => Unit
): Unit = {
  val backHref = module.fold("#/")(value => s"#/modules/${value.id}")
  val backLabel = if (module.isDefined) "← К модулю" else "← Все модули"

  var state = initialState(module)

  def filledWords: Int =
    state.words.count { word =>
      List(word.arabic, word.russian, word.plural).exists(_.trim.nonEmpty)
    }

  def showError(message: String): Unit =
    target.querySelector(".module-form-error") match {
      case null => ()
      case element =>
        element.textContent = message
        element.removeAttribute("hidden")
    }

  def clearError(): Unit =
    target.querySelector(".module-form-error") match {
      case null => ()
      case element =>
        element.setAttribute("hidden", "true")
    }

  def updateCount(): Unit =
    target.querySelector(".module-editor-header .badge") match {
      case null  => ()
      case badge => badge.textContent = wordsLabel(filledWords)
    }

  def focusLastArabicInput(): Unit = {
    val cards = target.querySelectorAll(".word-card-edit")
    if (cards.length > 0) {
      cards(cards.length - 1)
        .asInstanceOf[dom.HTMLElement]
        .querySelector(".word-input-arabic")
        .asInstanceOf[dom.HTMLInputElement]
        .focus()
    }
  }

  def render(): Unit = {
    target.innerHTML = ""

    val nameInput = input(
      `type` := "text",
      id := "module-name",
      cls := "text-input module-name-input",
      placeholder := "Название модуля",
      attr("aria-label") := "Название модуля",
      value := state.name
    ).render

    nameInput.addEventListener(
      "input",
      (_: dom.Event) => {
        state = state.copy(name = nameInput.value)
        clearError()
      }
    )

    val grid = div(cls := "word-grid").render

    state.words.zipWithIndex.foreach { case (word, index) =>
      val arabicInput = input(
        `type` := "text",
        cls := "text-input word-input word-input-arabic",
        dir := "rtl",
        value := word.arabic
      ).render

      val russianInput = input(
        `type` := "text",
        cls := "text-input word-input",
        value := word.russian
      ).render

      val pluralInput = input(
        `type` := "text",
        cls := "text-input word-input word-input-arabic",
        dir := "rtl",
        value := word.plural
      ).render

      def updateWord(transform: WordForm => WordForm): Unit = {
        state = state.copy(
          words = state.words.updated(index, transform(state.words(index)))
        )
        clearError()
        updateCount()
      }

      arabicInput.addEventListener(
        "input",
        (_: dom.Event) => updateWord(_.copy(arabic = arabicInput.value))
      )
      russianInput.addEventListener(
        "input",
        (_: dom.Event) => updateWord(_.copy(russian = russianInput.value))
      )
      pluralInput.addEventListener(
        "input",
        (_: dom.Event) => updateWord(_.copy(plural = pluralInput.value))
      )

      val deleteButton = button(
        `type` := "button",
        cls := "btn btn-danger-ghost btn-sm word-delete"
      )("Удалить").render

      deleteButton.addEventListener(
        "click",
        (_: dom.Event) => {
          state = state.copy(words = state.words.patch(index, Nil, 1))
          render()
        }
      )

      grid.appendChild(
        div(cls := "word-card word-card-edit")(
          formField("Арабское слово")(arabicInput),
          formField("Перевод")(russianInput),
          formField("Мн. ч.")(pluralInput),
          div(cls := "word-card-actions")(deleteButton)
        ).render
      )
    }

    val addTile = button(
      `type` := "button",
      cls := "add-word-card"
    )(
      span(cls := "add-word-icon")("+"),
      span("Добавить слово")
    ).render

    addTile.addEventListener(
      "click",
      (_: dom.Event) => {
        state = state.copy(words = state.words :+ WordForm())
        render()
        focusLastArabicInput()
      }
    )

    grid.appendChild(addTile)

    val cancelButton = button(
      `type` := "button",
      cls := "btn btn-ghost"
    )("Отмена").render

    cancelButton.addEventListener(
      "click",
      (_: dom.Event) => onCancel()
    )

    val errorBox = p(cls := "error module-form-error")().render
    errorBox.setAttribute("hidden", "true")

    val formElement =
      form(cls := "module-detail module-editor")(
        a(
          href := backHref,
          cls := "back-link",
          onclick := { (event: dom.Event) =>
            event.preventDefault()
            onCancel()
          }
        )(backLabel),
        div(cls := "module-editor-header")(
          div(cls := "module-editor-heading")(
            nameInput
          ),
          span(cls := "badge")(wordsLabel(filledWords))
        ),
        errorBox,
        grid,
        div(cls := "module-editor-actions")(
          cancelButton,
          button(
            `type` := "submit",
            cls := "btn btn-primary"
          )("Сохранить")
        )
      ).render

    formElement.addEventListener(
      "submit",
      (event: dom.Event) => {
        event.preventDefault()

        if (state.name.trim.isEmpty) {
          showError("Введите название модуля")
        } else {
          onSave(toRequest(state), showError)
        }
      }
    )

    target.appendChild(formElement)
  }

  render()
}

private def initialState(module: Option[ModuleResponse]): ModuleFormState =
  module match {
    case None =>
      ModuleFormState(name = "", words = Vector(WordForm()))
    case Some(value) =>
      val words = value.words.map(wordToForm).toVector
      ModuleFormState(
        id = Some(value.id),
        name = value.name,
        words = if (words.isEmpty) Vector(WordForm()) else words
      )
  }

private def wordToForm(word: WordResponse): WordForm =
  WordForm(
    id = Some(word.id),
    arabic = word.arabic,
    russian = word.russian,
    plural = word.plural.getOrElse("")
  )

private def toRequest(state: ModuleFormState): ModuleRequest =
  ModuleRequest(
    id = state.id,
    name = state.name,
    words = state.words.toList
      .filter(word =>
        List(word.arabic, word.russian, word.plural).exists(_.trim.nonEmpty)
      )
      .map { word =>
        WordRequest(
          id = word.id,
          arabic = word.arabic,
          russian = word.russian,
          plural = Option(word.plural.trim).filter(_.nonEmpty)
        )
      }
  )
