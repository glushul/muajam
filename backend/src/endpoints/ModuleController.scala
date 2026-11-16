package muajam.backend.endpoints

import zio.*
import zio.http.*
import zio.http.codec.PathCodec
import zio.json.*
import scalasql.*
import scalasql.dbzio.ZDbClient
import muajam.backend.db.*
import scalasql.dialects.PostgresDialect.*
import scala.util.Random
import muajam.shared.*
import scalasql.dbzio.DbException
import muajam.backend.Util

object ModuleController {

  private val modulesRoute: RoutePattern[Unit] =
    Method.GET / Util.pathCodec(ApiRoutes.modules)

  private val moduleByIdRoute: RoutePattern[Int] =
    Method.GET / Util.pathCodec(ApiRoutes.modules) / int("id")

  def wordResponse(word: Word[Sc]): WordResponse =
    WordResponse(
      id = word.id,
      arabic = word.arabic,
      translation = word.translation,
      plural = word.plural,
      moduleId = word.moduleId,
      createdAt = word.createdAt.toString
    )

  private def moduleResponse(
      module: Module[Sc],
      words: Seq[Word[Sc]],
      previewOnly: Boolean
  ): ModuleResponse = {
    val selectedWords =
      if (previewOnly) Random.shuffle(words).take(math.min(5, words.length))
      else words

    ModuleResponse(
      id = module.id,
      name = module.name,
      createdAt = module.createdAt.toString,
      wordsCount = words.length,
      words = selectedWords.map(wordResponse).toList
    )
  }

  private val modules: Handler[ZDbClient, DbException, Request, Response] =
    Handler.fromFunctionZIO[Request] { _ =>
      withDb {
        for {
          moduleRows <- run(Module.select)
          modulesWithWords <- ZIO.foreach(moduleRows) { module =>
            run(Word.select.filter(_.moduleId `=` module.id))
              .map(words => moduleResponse(module, words, previewOnly = true))
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
                    moduleResponse(module, words, previewOnly = false).toJson
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
