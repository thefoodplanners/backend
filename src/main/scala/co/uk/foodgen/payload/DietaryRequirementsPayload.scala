package co.uk.foodgen.payload

import co.uk.foodgen.models.DietaryRequirement
import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder}
import sttp.tapir.Schema

case class DietaryRequirementsPayload(
  dietaryRequirements: List[DietaryRequirement]
)

object DietaryRequirementsPayload:
  given Codec[DietaryRequirementsPayload] = Codec.from(
    Decoder.decodeList[DietaryRequirement].map(DietaryRequirementsPayload.apply),
    Encoder.instance(_.dietaryRequirements.asJson)
  )
  given Schema[DietaryRequirementsPayload] =
    Schema.schemaForIterable[DietaryRequirement, List].as[DietaryRequirementsPayload]
