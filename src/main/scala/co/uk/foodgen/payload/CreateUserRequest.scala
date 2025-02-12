package co.uk.foodgen.payload

import cats.effect.IO
import co.uk.foodgen.models.DietaryRequirement
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import sttp.tapir.Schema

case class CreateUserRequest(
  email: String,
  username: String,
  password: String,
  targetCalories: Option[Int],
  dietaryRequirements: List[DietaryRequirement]
)

object CreateUserRequest:
  given EntityEncoder[IO, CreateUserRequest] = jsonEncoderOf[IO, CreateUserRequest]
  given Codec[CreateUserRequest] = deriveCodec
  given Schema[CreateUserRequest] = Schema.derived
