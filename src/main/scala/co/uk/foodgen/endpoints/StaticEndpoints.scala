package co.uk.foodgen.endpoints

import cats.data.Ior
import cats.effect.IO
import org.http4s.{ContextRoutes, HttpRoutes}
import sttp.model.{HeaderNames, MediaType, StatusCode}
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.*
import cats.syntax.traverse.toTraverseOps

object StaticEndpoints extends HttpEndpoint:

  private val staticEndpoint: ServerEndpoint[Any, IO] = endpoint.get
    .in("resources" / path[String]("filename"))
    .out(inputStreamRangeBody)
    .out(header(HeaderNames.ContentType, MediaType.ImageJpeg.toString))
    .errorOut(statusCode(StatusCode.NotFound))
    .serverLogic[IO] { filename =>
      val classLoader = getClass.getClassLoader
      val resourcePath = s"assets/$filename"
      val resourceUrl = Option(classLoader.getResource(resourcePath))

      resourceUrl
        .map(url => IO.blocking(InputStreamRange(() => url.openStream(), None)))
        .toRight(())
        .sequence
    }

  val endpoints = List(staticEndpoint)

  val routes: Ior[HttpRoutes[IO], ContextRoutes[AuthInfo, IO]] =
    val routes: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes(staticEndpoint)
    Ior.left(routes)

end StaticEndpoints
