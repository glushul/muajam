package muajam.frontend.api

import org.scalajs.dom
import scalajs.js
import zio.json.*

object Http {
  def getJson[A: JsonDecoder](
      url: String,
      onSuccess: A => Unit,
      onNotFound: () => Unit,
      onError: String => Unit
  ): Unit =
    dom
      .fetch(url)
      .`then`[Unit] { response =>
        if (response.status == 404) {
          onNotFound()
        } else if (!response.ok) {
          readError(response, onError)
        } else {
          response.text().`then`[Unit] { body =>
            body.fromJson[A] match {
              case Left(error) =>
                onError(s"Ошибка разбора ответа: $error")
              case Right(value) =>
                onSuccess(value)
            }
          }
        }
        ()
      }
      .`catch` { (error: Any) =>
        onError(s"Ошибка запроса: ${error.toString}")
        ()
      }

  def sendJson[A: JsonEncoder](
      url: String,
      method: String,
      body: A,
      onSuccess: () => Unit,
      onError: String => Unit
  ): Unit = {
    val init =
      js.Dynamic
        .literal(
          method = method,
          headers = js.Dynamic.literal(`Content-Type` = "application/json"),
          body = body.toJson
        )
        .asInstanceOf[dom.RequestInit]

    dom
      .fetch(url, init)
      .`then`[Unit] { response =>
        if (!response.ok) {
          readError(response, onError)
        } else {
          onSuccess()
        }
        ()
      }
      .`catch` { (error: Any) =>
        onError(s"Ошибка запроса: ${error.toString}")
        ()
      }
  }

  private def readError(
      response: dom.Response,
      onError: String => Unit
  ): Unit = {
    response.text().`then`[Unit] { body =>
      val details = body.trim
      val suffix =
        if (details.nonEmpty && details.length <= 200 && !details.startsWith("<"))
          s": $details"
        else ""
      onError(s"Ошибка ${response.status}$suffix")
    }.`catch` { (_: Any) =>
      onError(s"Ошибка ${response.status}")
      ()
    }
    ()
  }
}
