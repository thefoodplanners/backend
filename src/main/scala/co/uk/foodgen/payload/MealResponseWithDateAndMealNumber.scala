package co.uk.foodgen.payload

import co.uk.foodgen.service.models.MealView
import io.circe.*
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.scalaland.chimney.syntax.transformInto
import sttp.tapir.Schema

import java.time.LocalDate

final case class MealResponseWithDateAndMealNumber(
  date: LocalDate,
  mealNumber: Int,
  meal: Option[MealResponse]
)

object MealResponseWithDateAndMealNumber:
  def create(date: LocalDate, mealNumber: Int, view: Option[MealView]): MealResponseWithDateAndMealNumber =
    MealResponseWithDateAndMealNumber(
      date = date,
      mealNumber = mealNumber,
      meal = view.map(_.transformInto[MealResponse])
    )

  given Encoder[MealResponseWithDateAndMealNumber] =
    deriveEncoder[MealResponseWithDateAndMealNumber].mapJson(_.dropNullValues)
  given Decoder[MealResponseWithDateAndMealNumber] = deriveDecoder[MealResponseWithDateAndMealNumber]
  given Schema[MealResponseWithDateAndMealNumber] = Schema.derived
