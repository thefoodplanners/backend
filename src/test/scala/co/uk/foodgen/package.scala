package co.uk

import com.dimafeng.testcontainers.{Container, JdbcDatabaseContainer, PostgreSQLContainer}
import com.zaxxer.hikari.HikariDataSource
import io.getquill.*
import org.testcontainers.utility.DockerImageName
import zio.http.Response
import zio.json.ast.Json
import zio.json.ast.Json.*
import zio.prelude.Equal
import zio.{ZIO, ZLayer}

import java.time.LocalDate
import javax.sql.DataSource
import scala.io.Source

package object foodgen:
  val httpApp = ServerConfig.httpApp

  type IOR[A] = ZIO[DataSource, Response, A]

  private val containerDef = PostgreSQLContainer.Def(
    dockerImageName = DockerImageName.parse("postgres:latest")
  )

  private def startContainer[C <: Container](container: C) = ZIO.attemptBlocking(container.start())
  private def releaseContainer[C <: Container](container: C) = ZIO.attemptBlocking(container.close()).ignore

  private def containerResource =
    val container = containerDef.createContainer()
    ZIO.acquireRelease(startContainer(container))(_ => releaseContainer(container)).map(_ => container)

  private def dataSourceTest(container: JdbcDatabaseContainer): DataSource =
    val ds = new HikariDataSource()
    ds.setDriverClassName(container.driverClassName)
    ds.setJdbcUrl(container.jdbcUrl)
    ds.setUsername(container.username)
    ds.setPassword(container.password)
    ds

  private def initDbTables(dataSource: DataSource) =
    (for
      source <- ZIO.fromAutoCloseable(ZIO.attempt(Source.fromResource("init.sql")))
      query = source.getLines().mkString(" ")
      ctx = new PostgresZioJdbcContext(SnakeCase)
      quillQuery = quote { sql"#$query".as[Action[Int]] }
      _ <- ctx.run(quillQuery)
    yield ()).provideSomeLayer(ZLayer.succeed(dataSource))

  def setupDbLayer = ZLayer.fromZIO {
    for
      container <- containerResource
      dataSource = dataSourceTest(container)
      _ <- initDbTables(dataSource)
    yield dataSource
  }

  given CanEqual[Json, Json] = CanEqual.derived
  given Equal[LocalDate] = Equal.make(_.isEqual(_))

  extension (json: Json)
    def removeFields(names: String*): Json =
      json match
        case Json.Obj(fields) =>
          val filtered = fields.collect {
            case (k, v) if !names.contains(k) => k -> v.removeFields(names*)
          }
          Json.Obj(filtered)
        case Json.Arr(values) =>
          Json.Arr(values.map(_.removeFields(names*)))
        case other => other
end foodgen
