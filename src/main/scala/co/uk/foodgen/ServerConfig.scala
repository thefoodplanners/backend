package co.uk.foodgen

import co.uk.foodgen.api.*
import co.uk.foodgen.api.endpoints.*
import com.zaxxer.hikari.HikariDataSource
import zio.*
import zio.http.*
import zio.http.endpoint.openapi.OpenAPIGen

import javax.sql.DataSource

object ServerConfig:
  private case class DbConfig(driver: String, jdbcUrl: String, username: String, password: String)

  private val dbConfigLiveLayer = ZLayer.fromZIO {
    for
      jdbcUrl <- System.env("JDBC_URL").someOrFailException
      username <- System.env("DB_USERNAME").someOrFailException
      password <- System.env("DB_PASSWORD").someOrFailException
    yield DbConfig("org.postgresql.Driver", jdbcUrl, username, password)
  }

  private val dataSourceLiveLayer = ZLayer.fromZIO {
    for
      cfg <- ZIO.service[DbConfig]
      ds = new HikariDataSource()
      _ =
        ds.setDriverClassName(cfg.driver)
        ds.setJdbcUrl(cfg.jdbcUrl)
        ds.setUsername(cfg.username)
        ds.setPassword(cfg.password)
    yield ds
  }

  private val allAPIEndpoints: List[APIEndpoint] =
    List(AuthenticationEndpoints, UserEndpoints, RecipeEndpoints, CalendarEndpoints, ProgressChartEndpoints)
  private val allEndpoints = allAPIEndpoints.flatMap(_.endpoints)
  val openAPI = OpenAPIGen.fromEndpoints(title = "FoodGen API", version = "1.0", allEndpoints)

  private val allRoutes: List[APIRoute] =
    List(AuthenticationRoutes, UserRoutes, RecipeRoutes, CalendarRoutes, ProgressChartRoutes, DocsRoutes)
  private val unauthenticatedRoutes = allRoutes.map(_.unauthenticatedRoutes).reduce(_ ++ _)
  private val authenticatedRoutes = allRoutes.map(_.routes).reduce(_ ++ _) @@ AuthMiddleware.jwtAuthentication
  val httpApp: Routes[DataSource, Response] =
    (unauthenticatedRoutes ++ authenticatedRoutes).mapError(t => Response.fromThrowable(t, errorConfig))
      @@ Middleware.serveResources(Path.empty / "static")
      @@ HandlerAspect.debug
      @@ Middleware.requestLogging(logRequestBody = true, logResponseBody = true)
      @@ Middleware.cors(
        Middleware.CorsConfig(
          allowedOrigin = origin => Some(Header.AccessControlAllowOrigin.Specific(origin)),
          allowedMethods =
            Header.AccessControlAllowMethods(Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.OPTIONS),
          exposedHeaders = Header.AccessControlExposeHeaders.Some(NonEmptyChunk.single("Authorization"))
        )
      )

  val server = Server.serve(httpApp).provide(Server.defaultWithPort(9000), dbConfigLiveLayer, dataSourceLiveLayer)

end ServerConfig
