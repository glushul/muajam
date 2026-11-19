package muajam.backend.endpoints

import zio.*
import zio.http.*
import zio.http.codec.PathCodec
import zio.json.*
import scalasql.*
import scalasql.dbzio.ZDbClient
import muajam.backend.db.*
import scalasql.dialects.PostgresDialect.*
import muajam.shared.*
import scalasql.dbzio.DbException
import muajam.backend.http.Responses
import muajam.backend.http.Util

object ModuleController {

  private val modules: Handler[ZDbClient, DbException, Request, Response] =
    Handler.fromFunctionZIO[Request] { _ =>
      withDb {
        for {
          modules <- run(Module.select.sortBy(m => m.id).desc)
          modulesWithWords <- ZIO.foreach(modules) { module =>
            run(Word.select.filter(_.moduleId `=` module.id))
              .map(words =>
                Responses.moduleResponse(module, words, limit = Some(5))
              )
          }
          response = Response.json(modulesWithWords.toJson)
        } yield response
      }
    }

  private val moduleById
      : Handler[ZDbClient, DbException, (Int, Request), Response] =
    Handler.fromFunctionZIO[
      (Int, Request)
    ] { case (id, _) =>
      withDb {
        for {
          module <- run(Module.select.filter(_.id `=` id))
          response <- module.headOption match {
            case None =>
              ZIO.succeed(Response.status(Status.NotFound))
            case Some(module) =>
              run(
                Word.select
                  .filter(_.moduleId `=` module.id)
                  .sortBy(m => m.moduleId)
                  .desc
              )
                .map(words =>
                  Response.json(
                    Responses
                      .moduleResponse(module, words)
                      .toJson
                  )
                )
          }
        } yield response
      }
    }

  def routes: Routes[ZDbClient, DbException] = {
    Routes(
      Method.GET / Util.pathCodec(ApiRoutes.modules) -> modules,
      Method.GET / Util.pathCodec(ApiRoutes.modules) / int("id") -> moduleById
    )
  }
}
