package muajam.backend.db

import scalasql.*, PostgresDialect.*

case class Word[T[_]](
    id: T[Int],
    arabic: T[String],
    russian: T[String],
    plural: T[Option[String]],
    moduleId: T[Int],
    createdAt: T[java.time.LocalDateTime]
)
object Word extends Table[Word]
