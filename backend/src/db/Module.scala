package muajam.backend.db

import scalasql.*, PostgresDialect.*

case class Module[T[_]](
    id: T[Int],
    name: T[String],
    createdAt: T[java.time.LocalDateTime]
)
object Module extends Table[Module]
