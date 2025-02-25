package co.uk.foodgen.payload

import cats.effect.IO
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import sttp.tapir.Schema

final case class LoginRequest(username: String, password: String)

object LoginRequest:
  given EntityEncoder[IO, LoginRequest] = jsonEncoderOf[IO, LoginRequest]
  given Codec[LoginRequest] = deriveCodec
  given Schema[LoginRequest] = Schema.derived
