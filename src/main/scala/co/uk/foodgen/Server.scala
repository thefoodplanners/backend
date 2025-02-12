package co.uk.foodgen

import cats.data.Kleisli
import cats.effect.{IO, Resource}
import cats.implicits.toSemigroupKOps
import co.uk.foodgen.endpoints.*
import com.comcast.ip4s.{ipv4, port}
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Server:

  def setTransactor(
    driverClass: String,
    jdbcUrl: String,
    username: String,
    password: String
  ): Resource[IO, HikariTransactor[IO]] =
    for
      hikariConfig <- Resource.pure[IO, HikariConfig] {
        val config = new HikariConfig()
        config.setDriverClassName(driverClass)
        config.setJdbcUrl(jdbcUrl)
        config.setUsername(username)
        config.setPassword(password)
        config
      }
      hikariTransactor <- HikariTransactor.fromHikariConfig[IO](hikariConfig)
    yield hikariTransactor

  private val mainTransactor = setTransactor(
    driverClass = "org.postgresql.Driver",
    jdbcUrl = System.getenv("JDBC_URL"),
    username = System.getenv("DB_USERNAME"),
    password = System.getenv("DB_PASSWORD")
  )

  private def mainHttpApp(tx: Transactor[IO]) =
    val loginEndpoints = new LoginEndpoints(tx)
    val userEndpoints = new UserEndpoints(tx)
    val recipeEndpoints = new RecipeEndpoints(tx)
    val calendarEndpoints = new CalendarEndpoints(tx)
    val progressChartEndpoints = new ProgressChartEndpoints(tx)

    val fullEndpoints = loginEndpoints.allEndpoints ++
      userEndpoints.allEndpoints ++
      recipeEndpoints.allEndpoints ++
      calendarEndpoints.allEndpoints ++
      progressChartEndpoints.allEndpoints

    val docsRoutes = Http4sServerInterpreter[IO]().toRoutes(DocsEndpoints.docs(fullEndpoints))

    val authenticatedRoutes =
      (
        userEndpoints.allRoutes <+>
          recipeEndpoints.allRoutes <+>
          calendarEndpoints.allRoutes <+>
          progressChartEndpoints.allRoutes
      ).withAuthentication

    val corsRoutes = CORS.policy.withAllowOriginAll
      .withAllowCredentials(false)
      .apply(loginEndpoints.allRoutes <+> authenticatedRoutes)

    (docsRoutes <+> corsRoutes).orNotFound

  val server: IO[Nothing] =
    (for
      tx <- mainTransactor
      server <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"9000")
        .withHttpApp(mainHttpApp(tx))
        .build
    yield server).useForever
