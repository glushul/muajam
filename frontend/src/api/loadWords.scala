package muajam.frontend.api

import org.scalajs.dom
import zio.json.*
import muajam.shared.{ApiRoutes, WordResponse}

private var wordsCache: Option[List[WordResponse]] = None

def loadWords(
    onLoaded: List[WordResponse] => Unit,
    onError: String => Unit
): Unit =
  wordsCache match {
    case Some(words) => onLoaded(words)
    case None        =>
      dom
        .fetch(ApiRoutes.words)
        .`then`[Unit] { response =>
          if (!response.ok) {
            onError(s"Ошибка загрузки слов: ${response.status}")
          } else {
            response.text().`then`[Unit] { body =>
              body.fromJson[List[WordResponse]] match {
                case Left(error) =>
                  onError(s"Ошибка разбора модулей: $error")
                case Right(words) =>
                  wordsCache = Some(words)
                  onLoaded(words)
              }
            }
          }
          ()
        }
        .`catch` { (error: Any) =>
          onError(s"Не удалось загрузить слова: ${error.toString}")
          ()
        }
  }
