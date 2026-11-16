package muajam.backend.endpoints

import zio.http.*
import muajam.backend.Util
import muajam.shared.ApiRoutes
import scalasql.dbzio.*
import muajam.backend.db.*
import scalasql.dialects.PostgresDialect.*
import scalasql.*
import zio.*
import zio.json.*
import muajam.backend.endpoints.ModuleController.wordResponse

object WordController {

  private val words: Handler[ZDbClient, DbException, Request, Response] =
    Handler.fromFunctionZIO[Request] { _ =>
      withDb {
        for {
          wordsRows <- run(Word.select.sortBy(w => w.translation))
        } yield Response.json(wordsRows.map(wordResponse(_)).toJson)
      }
    }

  def routes: Routes[ZDbClient, DbException] = {
    Routes(
      Method.GET / Util.pathCodec(ApiRoutes.words) -> words
    )
  }
}
