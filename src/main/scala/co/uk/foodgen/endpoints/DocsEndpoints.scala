package co.uk.foodgen.endpoints

import cats.effect.IO
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object DocsEndpoints:

  def docs(fullEndpoints: List[ServerEndpoint[_, IO]]) =
    SwaggerInterpreter().fromServerEndpoints[IO](fullEndpoints, "FoodGen", "1.0")

end DocsEndpoints
