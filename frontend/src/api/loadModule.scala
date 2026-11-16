package muajam.frontend.api

import org.scalajs.dom
import zio.json.*
import muajam.shared.{ApiRoutes, ModuleResponse}

private val modulesCache = scala.collection.mutable.Map.empty[Int, ModuleResponse]

def loadModule(
    id: Int,
    onLoaded: Option[ModuleResponse] => Unit,
    onError: String => Unit
): Unit =
  modulesCache.get(id) match {
    case Some(module) => onLoaded(Some(module))
    case None         =>
      dom
        .fetch(ApiRoutes.module(id))
        .`then`[Unit] { response =>
          if (response.status == 404) {
            onLoaded(None)
          } else if (!response.ok) {
            onError(s"Ошибка загрузки модуля: ${response.status}")
          } else {
            response.text().`then`[Unit] { body =>
              body.fromJson[ModuleResponse] match {
                case Left(error) =>
                  onError(s"Ошибка разбора модуля: $error")
                case Right(module) =>
                  modulesCache.update(id, module)
                  onLoaded(Some(module))
              }
            }
          }
          ()
        }
        .`catch` { (error: Any) =>
          onError(s"Не удалось загрузить модуль: ${error.toString}")
          ()
        }
  }
