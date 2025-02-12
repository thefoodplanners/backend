package co.uk.foodgen.payload

import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder}
import sttp.tapir.Schema

case class TargetCaloriesResponse(
  targetCalories: Option[Int]
)

object TargetCaloriesResponse:
  given Codec[TargetCaloriesResponse] = Codec.from(
    Decoder.decodeOption[Int].map(TargetCaloriesResponse.apply),
    Encoder.instance(_.targetCalories.asJson)
  )
  given Schema[TargetCaloriesResponse] = Schema.schemaForOption[Int].as[TargetCaloriesResponse]
