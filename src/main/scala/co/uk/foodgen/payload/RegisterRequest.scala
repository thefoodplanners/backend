package co.uk.foodgen.payload

import cats.effect.IO
import co.uk.foodgen.models.DietaryRequirement
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import sttp.tapir.Schema.*
import sttp.tapir.{Schema, *}

final case class RegisterRequest(
  email: String,
  username: String,
  password: String,
  targetCalories: Option[Int],
  dietaryRequirements: List[DietaryRequirement]
)

object RegisterRequest:
  given EntityEncoder[IO, RegisterRequest] = jsonEncoderOf[IO, RegisterRequest]
  given Codec[RegisterRequest] = deriveCodec
  given Schema[RegisterRequest] =
    Schema.derived[RegisterRequest].modify(_.dietaryRequirements)(_.copy(isOptional = false))
