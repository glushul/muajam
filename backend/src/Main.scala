package muajam.backend

import zio.*
import zio.http.*
import scalasql.dbzio.ZDbClient
import muajam.backend.endpoints.*
import scalasql.dbzio.DbException

object Main extends ZIOAppDefault {

  def errorHandler(cause: Cause[Any]): Response =
    cause.failureOption match {
      case Some(_: DbException) =>
        Response.status(Status.InternalServerError)
      case _ => Response.status(Status.NotFound)
    }

  val index: Route[Any, Throwable] = {
    Method.GET / "" ->
      Handler
        .fromResource("public/index.html")
  }

  val static = {
    Routes.serveResources(Path.root, "public") ++ Routes(index)
  }

  def routes: Routes[ZDbClient, Response] = {
    (static ++ ModuleController.routes ++ WordController.routes)
      .handleErrorCauseZIO(cause =>
        ZIO.logErrorCause(cause).as(errorHandler(cause))
      )
  }

  def run =
    ZIO.scoped {
      for {
        config <- AppConfig.load
        _ <- Server
          .serve(routes)
          .provide(
            Server.defaultWithPort(config.httpPort),
            db.layer(config.db)
          )
      } yield ()
    }

}
