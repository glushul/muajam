package muajam.frontend

import org.scalajs.dom

object Route {

  def parse(hash: String): ClientRoute = {
    val path = hash.stripPrefix("#")

    if (path.isEmpty || path == "/") ModulesPage
    else if (path == "/modules/new" || path == "/modules/new/")
      NewModulePage
    else if (path.startsWith("/modules/")) {
      val (modulePath, query) = path.split("\\?", 2) match {
        case Array(p, q) => (p, Some(q))
        case _           => (path, None)
      }
      val rawId = modulePath.drop("/modules/".length).stripSuffix("/")
      if (rawId.endsWith("/edit")) {
        val wordId = query
          .toSeq
          .flatMap(_.split("&"))
          .collectFirst {
            case part if part.startsWith("word=") => part.drop("word=".length)
          }
          .flatMap(_.toIntOption)

        rawId
          .stripSuffix("/edit")
          .toIntOption
          .fold[ClientRoute](NotFound)(id => EditModulePage(id, wordId))
      } else if (rawId.endsWith("/study"))
        rawId
          .stripSuffix("/study")
          .toIntOption
          .fold[ClientRoute](NotFound)(StudyModulePage.apply)
      else
        rawId.toIntOption.fold[ClientRoute](NotFound)(ModulePage.apply)
    } else if (path == "/dictionary" || path == "/dictionary/") DictionaryPage
    else if (path == "/grammar" || path == "/grammar/") GrammarPage
    else NotFound
  }

  def current: ClientRoute = parse(dom.window.location.hash)

  def navigate(page: ClientRoute): Unit =
    dom.window.location.hash = "#" + page.route

  def onChange(handler: ClientRoute => Unit): Unit =
    dom.window.addEventListener(
      "hashchange",
      (_: dom.Event) => handler(current)
    )

  sealed trait ClientRoute {
    def route: String
  }

  case object ModulesPage extends ClientRoute {
    val route = "/"
  }

  final case class ModulePage(id: Int) extends ClientRoute {
    val route = s"/modules/$id"
  }

  final case class StudyModulePage(id: Int) extends ClientRoute {
    val route = s"/modules/$id/study"
  }

  case object NewModulePage extends ClientRoute {
    val route = "/modules/new"
  }

  final case class EditModulePage(
      id: Int,
      wordId: Option[Int] = None
  ) extends ClientRoute {
    def route =
      s"/modules/$id/edit" + wordId.fold("")(wid => s"?word=$wid")
  }

  case object DictionaryPage extends ClientRoute {
    val route = "/dictionary"
  }

  case object GrammarPage extends ClientRoute {
    val route = "/grammar"
  }

  case object NotFound extends ClientRoute {
    val route = "/not-found"
  }
}
