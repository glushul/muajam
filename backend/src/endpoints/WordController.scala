package muajam.backend.endpoints

import zio.http.*
import muajam.shared.ApiRoutes
import scalasql.dbzio.*
import muajam.backend.db.*
import scalasql.dialects.PostgresDialect.*
import scalasql.*
import zio.*
import zio.json.*
import muajam.backend.http.Util
import muajam.backend.http.Responses

object WordController {

  private val words: Handler[ZDbClient, DbException, Request, Response] =
    Handler.fromFunctionZIO[Request] { _ =>
      withDb {
        for {
          words <- run(Word.select.sortBy(w => w.russian))
          response = Response.json(
            words
              .map(Responses.wordResponse(_))
              .distinctBy(w => (w.withoutHarakat, w.russian))
              .toJson
          )
        } yield response
      }
    }

  def routes: Routes[ZDbClient, DbException] = {
    Routes(
      Method.GET / Util.pathCodec(ApiRoutes.words) -> words
    )
  }
}
