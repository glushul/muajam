package muajam.backend

import zio.*

final case class DbConfig(
    host: String,
    port: Int,
    name: String,
    user: String,
    password: String
) {
  def jdbcUrl: String = s"jdbc:postgresql://$host:$port/$name"
}

final case class AppConfig(
    httpPort: Int,
    db: DbConfig
)

object AppConfig {
  def load: Task[AppConfig] = ZIO.attempt {
    def value(key: String, default: String): String =
      sys.env.getOrElse(key, default)

    def intValue(key: String, default: Int): Int =
      sys.env.get(key).flatMap(_.toIntOption).getOrElse(default)

    AppConfig(
      httpPort = intValue("HTTP_PORT", 8080),
      db = DbConfig(
        host = value("DB_HOST", "127.0.0.1"),
        port = intValue("DB_PORT", 5432),
        name = value("DB_NAME", "muajam"),
        user = value("DB_USER", "postgres"),
        password = value("DB_PASSWORD", "postgres")
      )
    )
  }
}
