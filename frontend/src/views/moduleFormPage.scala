package muajam.frontend.views

import org.scalajs.dom
import org.scalajs.dom.document
import scalatags.JsDom.all.*
import muajam.frontend.api.loadModule
import muajam.frontend.utils.*
import muajam.shared.{ModuleResponse, WordResponse}

def moduleFormPage(
    target: dom.HTMLElement,
    moduleId: Option[Int],
    token: Int
): Unit = {
  val title =
    if (moduleId.isDefined) "Редактирование модуля" else "Новый модуль"

  document.title = s"$title — Muajam"
  target.innerHTML = ""

  moduleId match {
    case None =>
      renderEditor(target, None)
    case Some(id) =>
      target.appendChild(p(cls := "muted")("Загрузка модуля…").render)

      loadModule(
        id,
        module => {
          if (RenderState.isCurrent(token)) {
            target.innerHTML = ""
            module match {
              case Some(value) => renderEditor(target, Some(value))
              case None        => notFound(target, "Модуль не найден")
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

private def renderEditor(
    target: dom.HTMLElement,
    module: Option[ModuleResponse]
): Unit = {
  val backHref = module.fold("#/")(value => s"#/modules/${value.id}")
  val backLabel = if (module.isDefined) "← К модулю" else "← Все модули"

  val wordCount = span(cls := "badge")(wordCountLabel(0)).render

  val grid = div(cls := "word-grid").render

  def updateCount(): Unit = {
    val cards = grid.querySelectorAll(".word-card-edit")
    var filled = 0
    var i = 0
    while (i < cards.length) {
      val card = cards(i).asInstanceOf[dom.HTMLElement]
      val inputs = card.querySelectorAll("input")
      var hasValue = false
      var j = 0
      while (j < inputs.length) {
        if (inputs(j).asInstanceOf[dom.HTMLInputElement].value.trim.nonEmpty) {
          hasValue = true
        }
        j += 1
      }
      if (hasValue) filled += 1
      i += 1
    }
    wordCount.textContent = wordCountLabel(filled)
  }

  def createWordCard(word: Option[WordResponse]): dom.HTMLDivElement = {
    val arabicInput = input(
      `type` := "text",
      cls := "text-input word-input word-input-arabic",
      dir := "rtl",
      placeholder := "كِتَاب",
      value := word.map(_.arabic).getOrElse("")
    ).render

    val russianInput = input(
      `type` := "text",
      cls := "text-input word-input",
      placeholder := "книга",
      value := word.map(_.russian).getOrElse("")
    ).render

    val pluralInput = input(
      `type` := "text",
      cls := "text-input word-input word-input-arabic",
      dir := "rtl",
      placeholder := "необязательно",
      value := word.flatMap(_.plural).getOrElse("")
    ).render

    val deleteButton = button(
      `type` := "button",
      cls := "btn btn-danger-ghost btn-sm word-delete"
    )("Удалить").render

    val card = div(cls := "word-card word-card-edit")(
      div(cls := "form-field")(
        label(cls := "form-label")("Арабское слово"),
        arabicInput
      ),
      div(cls := "form-field")(
        label(cls := "form-label")("Перевод"),
        russianInput
      ),
      div(cls := "form-field")(
        label(cls := "form-label")("Мн. ч."),
        pluralInput
      ),
      div(cls := "word-card-actions")(deleteButton)
    ).render

    deleteButton.addEventListener(
      "click",
      (_: dom.Event) => {
        card.remove()
        updateCount()
      }
    )

    Seq(arabicInput, russianInput, pluralInput).foreach { input =>
      input.addEventListener(
        "input",
        (_: dom.Event) => updateCount()
      )
    }

    card
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
      val card = createWordCard(None)
      grid.insertBefore(card, addTile)
      updateCount()
      card
        .querySelector(".word-input-arabic")
        .asInstanceOf[dom.HTMLInputElement]
        .focus()
    }
  )

  module.map(_.words).getOrElse(Nil) match {
    case Nil =>
      grid.appendChild(createWordCard(None))
    case words =>
      words.foreach { word =>
        grid.appendChild(createWordCard(Some(word)))
      }
  }

  grid.appendChild(addTile)
  updateCount()

  val nameInput = input(
    `type` := "text",
    id := "module-name",
    cls := "text-input module-name-input",
    placeholder := "Название модуля",
    attr("aria-label") := "Название модуля",
    value := module.map(_.name).getOrElse("")
  ).render

  val saveNote = p(
    cls := "form-note muted",
    attr("aria-live") := "polite"
  )("Сохранение пока не подключено к серверу.").render
  saveNote.setAttribute("hidden", "true")

  val saveButton = button(
    `type` := "button",
    cls := "btn btn-primary"
  )("Сохранить").render

  saveButton.addEventListener(
    "click",
    (_: dom.Event) => {
      saveNote.removeAttribute("hidden")
    }
  )

  target.appendChild(
    div(cls := "module-detail module-editor")(
      a(href := backHref, cls := "back-link")(backLabel),
      div(cls := "module-editor-header")(
        div(cls := "module-editor-heading")(
          nameInput
        ),
        wordCount
      ),
      grid,
      div(cls := "module-editor-actions")(
        a(href := backHref, cls := "btn btn-ghost")("Отмена"),
        saveButton
      ),
      saveNote
    ).render
  )
}

private def wordCountLabel(count: Int): String =
  s"$count ${pluralRu(count, "слово", "слова", "слов")}"
