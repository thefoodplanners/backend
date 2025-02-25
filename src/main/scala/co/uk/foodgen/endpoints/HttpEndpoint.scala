package co.uk.foodgen.endpoints

import cats.data.Ior
import cats.effect.IO
import org.http4s.{ContextRoutes, HttpRoutes}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Context

trait HttpEndpoint:
  def endpoints: List[ServerEndpoint[Context[AuthInfo], IO]]
  def routes: Ior[HttpRoutes[IO], ContextRoutes[AuthInfo, IO]]
