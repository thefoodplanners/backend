package co.uk.foodgen.api

import co.uk.foodgen.ServerConfig.openAPI
import zio.http.*

object DocsRoutes extends APIRoute:
  override val unauthenticatedRoutes =
    (Method.GET / "docs" -> handler {
      Response(
        status = Status.Ok,
        headers = Headers(
          Header.ContentType(MediaType.application.json),
          Header.ContentDisposition.attachment
        ),
        body = Body.fromString(openAPI.toJsonPretty)
      )
    }).toRoutes

end DocsRoutes
