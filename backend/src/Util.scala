package muajam.backend

import zio.http.codec.PathCodec

object Util {
  def pathCodec(path: String): PathCodec[Unit] =
    path
      .split("/")
      .filter(_.nonEmpty)
      .foldLeft(PathCodec.empty)((acc, segment) =>
        acc / PathCodec.literal(segment)
      )
}
