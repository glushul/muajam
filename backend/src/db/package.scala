package muajam.backend.db

import com.zaxxer.hikari.{HikariDataSource}
import scalasql.dialects.PostgresDialect.*
import muajam.backend.DbConfig
import zio.*
import scalasql.Queryable
import scalasql.dbzio.{ZDbClient, ZDbApi, DbException, ZDbTxnApi}

inline def run[Q, R](
    query: Q,
    fetchSize: Int = -1,
    queryTimeoutSeconds: Int = -1
)(using qr: Queryable[Q, R]): ZIO[ZDbApi, DbException, R] =
  ZIO.service[ZDbApi].flatMap(_.run(query, fetchSize, queryTimeoutSeconds))

def withDb[R, E, A](
    use: ZIO[ZDbApi & R, E, A]
): ZIO[ZDbClient & R, DbException | E, A] =
  ZDbClient.transaction {
    ZIO
      .service[ZDbTxnApi]
      .flatMap(db => use.provideSomeEnvironment[R](_.add[ZDbApi](db)))
  }

def values[Q, R](ts: Seq[R])(implicit
    qr: scalasql.Queryable.Row[Q, R]
): scalasql.query.Values[Q, R] =
  new scalasql.query.Values(ts)

def layer(config: DbConfig): ZLayer[Any, Throwable, ZDbClient] = ZLayer {
  ZIO.attemptBlocking {
    val source = com.zaxxer.hikari.HikariDataSource()
    source.setJdbcUrl(config.jdbcUrl)
    source.setUsername(config.user)
    source.setPassword(config.password)
    source.setMaximumPoolSize(20)
    source.setMinimumIdle(5)
    source.setConnectionTimeout(10000)
    source.setValidationTimeout(2000)
    source.setKeepaliveTime(30000)
    source.setMaxLifetime(600000)
    source.setLeakDetectionThreshold(30000)
    ZDbClient.dataSource(source, new scalasql.Config {})
  }
}
