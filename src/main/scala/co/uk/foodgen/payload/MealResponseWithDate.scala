package co.uk.foodgen.payload

import co.uk.foodgen.service.models.MealView
import io.circe.*
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.scalaland.chimney.syntax.transformInto
import sttp.tapir.Schema
//import io.circe.syntax.EncoderOps

import java.time.LocalDate

final case class MealResponseWithDate(
  date: LocalDate,
  meal: Option[MealResponse]
)

object MealResponseWithDate:
  def create(date: LocalDate, view: Option[MealView]): MealResponseWithDate =
    MealResponseWithDate(
      date = date,
      meal = view.map(_.transformInto[MealResponse])
    )

  given Encoder[MealResponseWithDate] = deriveEncoder[MealResponseWithDate].mapJson(_.dropNullValues)
  given Decoder[MealResponseWithDate] = deriveDecoder[MealResponseWithDate]
  given Schema[MealResponseWithDate] = Schema.derived
