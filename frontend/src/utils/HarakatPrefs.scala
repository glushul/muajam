package muajam.frontend.utils

import org.scalajs.dom
import muajam.shared.dto.WordResponse

object HarakatPrefs {
  private val StorageKey = "muajam.withoutHarakat"

  def withoutHarakat: Boolean =
    dom.window.localStorage.getItem(StorageKey) == "true"

  def setWithoutHarakat(value: Boolean): Unit =
    dom.window.localStorage.setItem(StorageKey, value.toString)

  def of(word: WordResponse): String =
    if (withoutHarakat) word.withoutHarakat else word.arabic

  def pluralOf(word: WordResponse): Option[String] =
    if (withoutHarakat) word.pluralWithoutHarakat else word.plural
}
