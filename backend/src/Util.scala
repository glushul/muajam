package muajam.backend

import zio.*
import zio.http.*
import zio.http.codec.PathCodec
import scalasql.*
import muajam.backend.db.*
import muajam.shared.*

object Util {
  def pathCodec(path: String): PathCodec[Unit] =
    path
      .split("/")
      .filter(_.nonEmpty)
      .foldLeft(PathCodec.empty)((acc, segment) =>
        acc / PathCodec.literal(segment)
      )

  object Responses {
    def wordResponse(
        word: Word[Sc]
    ): WordResponse =
      WordResponse(
        id = word.id,
        arabic = word.arabic,
        withoutHarakat = stripHarakat(word.arabic),
        translation = word.translation,
        plural = word.plural,
        pluralWithoutHarakat = word.plural.map(p => stripHarakat(p)),
        moduleId = word.moduleId,
        createdAt = word.createdAt.toString
      )

    def moduleResponse(
        module: Module[Sc],
        words: Seq[Word[Sc]],
        previewOnly: Boolean
    ): ModuleResponse = {
      val selectedWords =
        if (previewOnly) words.take(math.min(5, words.length))
        else words

      ModuleResponse(
        id = module.id,
        name = module.name,
        createdAt = module.createdAt.toString,
        wordsCount = words.length,
        words = selectedWords.map(wordResponse).toList
      )
    }
  }
}
