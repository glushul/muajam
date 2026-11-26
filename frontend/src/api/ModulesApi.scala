package muajam.frontend.api

import muajam.shared.ApiRoutes
import muajam.shared.dto.*

object ModulesApi {
  private var moduleListCache: Option[List[ModuleResponse]] = None
  private val moduleCache =
    scala.collection.mutable.Map.empty[Int, ModuleResponse]

  def loadModules(
      onLoaded: List[ModuleResponse] => Unit,
      onError: String => Unit
  ): Unit =
    moduleListCache match {
      case Some(modules) =>
        onLoaded(modules)
      case None =>
        Http.getJson[List[ModuleResponse]](
          ApiRoutes.modules,
          modules => {
            moduleListCache = Some(modules)
            onLoaded(modules)
          },
          onNotFound = () => onError("Модули не найдены"),
          onError = onError
        )
    }

  def loadModule(
      id: Int,
      onLoaded: Option[ModuleResponse] => Unit,
      onError: String => Unit
  ): Unit =
    moduleCache.get(id) match {
      case Some(module) =>
        onLoaded(Some(module))
      case None =>
        Http.getJson[ModuleResponse](
          ApiRoutes.module(id),
          module => {
            moduleCache.update(id, module)
            onLoaded(Some(module))
          },
          onNotFound = () => onLoaded(None),
          onError = onError
        )
    }

  def saveModule(
      request: ModuleRequest,
      onSaved: () => Unit,
      onError: String => Unit
  ): Unit = {
    val (url, method) = (ApiRoutes.modules, "POST")

    Http.sendJson[ModuleRequest](
      url,
      method,
      request,
      () => {
        moduleListCache = None
        moduleCache.clear()
        onSaved()
      },
      onError
    )
  }

  def invalidate(moduleId: Option[Int]): Unit = {
    moduleListCache = None
    moduleId.foreach(moduleCache.remove)
  }
}
