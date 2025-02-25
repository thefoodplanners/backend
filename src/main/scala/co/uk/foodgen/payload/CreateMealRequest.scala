package co.uk.foodgen.payload

import co.uk.foodgen.models.Recipe
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import neotype.interop.circe.given
import sttp.tapir.Schema

import java.time.LocalDate

final case class CreateMealRequest(
  date: LocalDate,
  mealNumber: Option[Int],
  recipeId: Recipe.Id
)

object CreateMealRequest:
  given Codec[CreateMealRequest] = deriveCodec
  given Schema[CreateMealRequest] = Schema.derived
