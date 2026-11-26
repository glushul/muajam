package muajam.backend.endpoints

import zio.*
import zio.http.*
import zio.http.codec.PathCodec
import zio.json.*
import scalasql.*
import scalasql.dbzio.ZDbClient
import muajam.backend.db.*
import scalasql.dialects.PostgresDialect.*
import muajam.shared.ApiRoutes
import muajam.shared.dto.*
import scalasql.dbzio.DbException
import muajam.backend.http.Responses
import muajam.backend.http.Util
import java.time.LocalDateTime

object ModuleController {

  private val modules: Handler[ZDbClient, DbException, Request, Response] =
    Handler.fromFunctionZIO[Request] { _ =>
      withDb {
        for {
          modules <- run(Module.select.sortBy(m => m.id).desc)
          modulesWithWords <- ZIO.foreach(modules) { module =>
            run(
              Word.select
                .filter(_.moduleId `=` module.id)
                .sortBy(m => m.id)
                .desc
            )
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

  private val postModule: Handler[ZDbClient, DbException, Request, Response] =
    Handler.fromFunctionZIO[Request] { (req) =>
      for {
        body <- req.body.asString.catchAll(_ => ZIO.succeed(""))
        response <- body.fromJson[ModuleRequest] match {
          case Left(_) =>
            ZIO.succeed(Response.status(Status.BadRequest))
          case Right(moduleRequest) =>
            withDb {
              for {
                moduleId <- moduleRequest.id match {
                  case Some(id) =>
                    run(
                      Module
                        .update(_.id `=` id)
                        .set(_.name := moduleRequest.name)
                    ).as(id)
                  case None =>
                    run(
                      Module.insert
                        .values(
                          Module(
                            id = 0,
                            name = moduleRequest.name,
                            createdAt = LocalDateTime.now()
                          )
                        )
                        .skipColumns(_.id)
                        .returning(_.id)
                        .single
                    )
                }
                _ <- ZIO.foreachDiscard(moduleRequest.words) { word =>
                  word.id match {
                    case Some(id) =>
                      run(
                        Word
                          .update(w =>
                            (w.id `=` id) && (w.moduleId `=` moduleId)
                          )
                          .set(
                            _.arabic := word.arabic,
                            _.russian := word.russian,
                            _.plural := word.plural
                          )
                      )
                    case None =>
                      run(
                        Word.insert
                          .values(
                            Word(
                              id = 0,
                              arabic = word.arabic,
                              russian = word.russian,
                              plural = word.plural,
                              moduleId = moduleId,
                              createdAt = LocalDateTime.now()
                            )
                          )
                          .skipColumns(_.id)
                      )
                  }
                }
                existingIds <- run(
                  Word.select.filter(_.moduleId `=` moduleId).map(_.id)
                )
                keptIds = moduleRequest.words.flatMap(_.id).toSet
                removedIds = existingIds.filterNot(keptIds.contains)
                _ <- ZIO.foreachDiscard(removedIds) { wordId =>
                  run(
                    Word.delete(w =>
                      (w.id `=` wordId) && (w.moduleId `=` moduleId)
                    )
                  )
                }
              } yield ()
            }.as(Response.status(Status.Ok))
        }
      } yield response
    }

  def routes: Routes[ZDbClient, DbException] = {
    Routes(
      Method.GET / Util.pathCodec(ApiRoutes.modules) -> modules,
      Method.GET / Util.pathCodec(ApiRoutes.modules) / int("id") -> moduleById,
      Method.POST / Util.pathCodec(ApiRoutes.modules) -> postModule
    )
  }
}
