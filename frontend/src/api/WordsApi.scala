package muajam.frontend.api

import muajam.shared.ApiRoutes
import muajam.shared.dto.WordResponse

object WordsApi {
  private var wordsCache: Option[List[WordResponse]] = None

  def loadWords(
      onLoaded: List[WordResponse] => Unit,
      onError: String => Unit
  ): Unit =
    wordsCache match {
      case Some(words) =>
        onLoaded(words)
      case None =>
        Http.getJson[List[WordResponse]](
          ApiRoutes.words,
          words => {
            wordsCache = Some(words)
            onLoaded(words)
          },
          onNotFound = () => onError("Слова не найдены"),
          onError = onError
        )
    }
}
