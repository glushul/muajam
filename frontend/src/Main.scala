package muajam.frontend

import org.scalajs.dom
import org.scalajs.dom.document

object Main {
  def main(args: Array[String]): Unit = {
    Option(document.getElementById("app")) match {
      case Some(root) =>
        root.innerHTML = ""
        root.appendChild(App().render)
        App.start()
      case None =>
        dom.console.error("Не найден элемент #app")
    }
  }
}
