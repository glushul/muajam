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
import muajam.backend.Util.*
import muajam.backend.Util

object ModuleController {

  private val modules: Handler[ZDbClient, DbException, Request, Response] =
    Handler.fromFunctionZIO[Request] { _ =>
      withDb {
        for {
          moduleRows <- run(Module.select.sortBy(m => m.id).desc)
          modulesWithWords <- ZIO.foreach(moduleRows) { module =>
            run(Word.select.filter(_.moduleId `=` module.id))
              .map(words =>
                Responses.moduleResponse(module, words, previewOnly = true)
              )
          }
        } yield Response.json(modulesWithWords.toJson)
      }
    }

  private val moduleById
      : Handler[ZDbClient, DbException, (Int, Request), Response] =
    Handler.fromFunctionZIO[
      (Int, Request)
    ] { case (id, _) =>
      withDb {
        for {
          moduleRows <- run(Module.select.filter(_.id `=` id))
          response <- moduleRows.headOption match {
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
                      .moduleResponse(module, words, previewOnly = false)
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
