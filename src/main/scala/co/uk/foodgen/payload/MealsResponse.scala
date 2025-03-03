package co.uk.foodgen.payload

import co.uk.foodgen.service.models.MealView
import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder}
import io.scalaland.chimney.Transformer
import sttp.tapir.Schema

final case class MealsResponse(meals: List[MealDaysResponse])

object MealsResponse:
  given Transformer[List[MealView], MealsResponse] = (views: List[MealView]) =>
    val maxMealNumber = Math.max(views.map(_.mealNumber).maxOption.getOrElse(1), 3)
    val meals = List.range(1, maxMealNumber + 1).map(mealNumber => MealDaysResponse(views, mealNumber))
    MealsResponse(meals)

  given Codec[MealsResponse] = Codec.from(
    Decoder.decodeList[MealDaysResponse].map(MealsResponse.apply),
    Encoder.instance(_.meals.asJson)
  )
  given Schema[MealsResponse] = Schema.schemaForIterable[MealDaysResponse, List].as[MealsResponse]
