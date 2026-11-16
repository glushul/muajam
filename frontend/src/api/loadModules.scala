package muajam.frontend.api

import org.scalajs.dom
import zio.json.*
import muajam.shared.{ApiRoutes, ModuleResponse}

private var modulesCache: Option[List[ModuleResponse]] = None

def loadModules(
    onLoaded: List[ModuleResponse] => Unit,
    onError: String => Unit
): Unit =
  modulesCache match {
    case Some(modules) => onLoaded(modules)
    case None          =>
      dom
        .fetch(ApiRoutes.modules)
        .`then`[Unit] { response =>
          if (!response.ok) {
            onError(s"Ошибка загрузки модулей: ${response.status}")
          } else {
            response.text().`then`[Unit] { body =>
              body.fromJson[List[ModuleResponse]] match {
                case Left(error) =>
                  onError(s"Ошибка разбора модулей: $error")
                case Right(modules) =>
                  modulesCache = Some(modules)
                  onLoaded(modules)
              }
            }
          }
          ()
        }
        .`catch` { (error: Any) =>
          onError(s"Не удалось загрузить модули: ${error.toString}")
          ()
        }
  }
