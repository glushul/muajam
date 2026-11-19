package muajam.backend.http

import java.time.format.DateTimeFormatter
import muajam.backend.db.*
import muajam.shared.utils.ArabicText
import muajam.shared.*
import scalasql.*

object Responses {

  private val dateTime = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  def wordResponse(word: Word[Sc]): WordResponse =
    WordResponse(
      id = word.id,
      arabic = word.arabic,
      withoutHarakat = ArabicText.stripHarakat(word.arabic),
      russian = word.russian,
      plural = word.plural,
      pluralWithoutHarakat = word.plural.map(ArabicText.stripHarakat),
      moduleId = word.moduleId,
      createdAt = word.createdAt.format(dateTime)
    )

  def moduleResponse(
      module: Module[Sc],
      words: Seq[Word[Sc]],
      limit: Option[Int] = None
  ): ModuleResponse = {
    val selected = limit.fold(words)(n => words.take(n))

    ModuleResponse(
      id = module.id,
      name = module.name,
      createdAt = module.createdAt.format(dateTime),
      wordsCount = words.length,
      words = selected.map(wordResponse).toList
    )
  }
}
