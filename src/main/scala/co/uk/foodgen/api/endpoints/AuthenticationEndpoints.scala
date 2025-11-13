package co.uk.foodgen.api.endpoints

import co.uk.foodgen.payload.requests.{LoginRequest, RegisterRequest}
import zio.*
import zio.http.*
import zio.http.codec.*
import zio.http.endpoint.*
import zio.schema.Schema

object AuthenticationEndpoints extends APIEndpoint:
  val registerEndpoint =
    Endpoint(RoutePattern.POST / "register")
      .in[RegisterRequest]
      .out[Unit](Status.Created, Doc.p(""))
      .outError[String](Status.BadRequest, Doc.p(""))

  val loginEndpoint =
    Endpoint(RoutePattern.POST / "login")
      .in[LoginRequest]
      .out[Unit](Status.NoContent, Doc.p(""))
      .outHeader(HttpCodec.header(Header.Authorization.Bearer))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  override val endpoints: List[Endpoint[_, _, _, _, _]] = List(registerEndpoint, loginEndpoint)
end AuthenticationEndpoints
