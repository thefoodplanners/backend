package co.uk.foodgen.payload

import co.uk.foodgen.models.{Meal, Recipe}
import co.uk.foodgen.service.models.MealView
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.scalaland.chimney.Transformer
import neotype.interop.circe.given
import sttp.tapir.Schema

import java.time.format.DateTimeFormatter

final case class MealResponse(
  mealId: Meal.Id,
  day: String,
  recipe: Recipe
)

object MealResponse:
  given Transformer[MealView, MealResponse] = (view: MealView) =>
    MealResponse(
      mealId = view.id,
      day = view.date.format(DateTimeFormatter.ofPattern("EEEE")),
      recipe = view.recipe
    )

  given Codec[MealResponse] = deriveCodec
  given Schema[MealResponse] = Schema.derived
