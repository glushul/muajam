package muajam.shared

object ApiRoutes {
  val modules: String = "/api/modules"

  def module(id: Int): String = s"$modules/$id"

  val words: String = "/api/words"
}
