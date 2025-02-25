package co.uk.foodgen

import cats.data.Kleisli
import cats.effect.{IO, Resource}
import cats.implicits.toSemigroupKOps
import co.uk.foodgen.endpoints.*
import co.uk.foodgen.endpoints.Authentication.*
import com.comcast.ip4s.{ipv4, port}
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.{ContextRoutes, HttpRoutes}
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

  private def mainEndpoints(tx: Transactor[IO]): List[HttpEndpoint] =
    List(
      new LoginEndpoints(tx),
      new UserEndpoints(tx),
      new RecipeEndpoints(tx),
      new CalendarEndpoints(tx),
      new ProgressChartEndpoints(tx)
    )

  private def buildHttpRoutes(
    routes: List[HttpRoutes[IO]],
    authRoutes: List[ContextRoutes[AuthInfo, IO]]
  ): HttpRoutes[IO] =
    val normalRoutes = routes.fold(HttpRoutes.empty[IO])(_ <+> _)
    val authenticatedRoutes = authRoutes.fold(ContextRoutes.empty[AuthInfo, IO])(_ <+> _).withAuthentication
    normalRoutes <+> authenticatedRoutes

  def mainHttpRoutes(tx: Transactor[IO]): HttpRoutes[IO] =
    val routes = mainEndpoints(tx)
    val normalRoutes = routes.flatMap(_.routes.left)
    val authRoutes = routes.flatMap(_.routes.right)
    buildHttpRoutes(normalRoutes, authRoutes)

  private def serverHttpApp(tx: Transactor[IO]) =
    val allEndpoints = StaticEndpoints :: mainEndpoints(tx)

    val fullEndpoints = allEndpoints.flatMap(_.endpoints)

    val docsRoutes = Http4sServerInterpreter[IO]().toRoutes(DocsEndpoints.docs(fullEndpoints))

    val routes = allEndpoints.flatMap(_.routes.left)
    val authRoutes = allEndpoints.flatMap(_.routes.right)
    val mainRoutes = buildHttpRoutes(routes, authRoutes)

    val corsRoutes = CORS.policy
      .withAllowOriginHost(origin => origin.port.contains(5173))
      .withAllowCredentials(true)
      .apply(mainRoutes)

    (docsRoutes <+> corsRoutes).orNotFound

  val server: IO[Nothing] =
    (for
      tx <- mainTransactor
      server <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"9000")
        .withHttpApp(serverHttpApp(tx))
        .build
    yield server).useForever
