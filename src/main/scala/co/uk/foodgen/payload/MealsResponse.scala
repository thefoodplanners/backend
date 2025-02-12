package co.uk.foodgen.payload

import co.uk.foodgen.service.models.MealView
import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder}
import io.scalaland.chimney.Transformer
import io.scalaland.chimney.syntax.*
import sttp.tapir.Schema

case class MealsResponse(meals: List[MealResponse])

object MealsResponse:
  given Transformer[List[MealView], MealsResponse] = (views: List[MealView]) =>
    MealsResponse(views.map(_.transformInto[MealResponse]))

  given Codec[MealsResponse] = Codec.from(
    Decoder.decodeList[MealResponse].map(MealsResponse.apply),
    Encoder.instance(_.meals.asJson)
  )
  given Schema[MealsResponse] = Schema.schemaForIterable[MealResponse, List].as[MealsResponse]
