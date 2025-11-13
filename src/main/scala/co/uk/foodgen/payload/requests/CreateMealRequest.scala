package co.uk.foodgen.payload.requests

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.Recipe
import zio.json.jsonNoExtraFields
import zio.schema.{Schema, derived}

import java.time.LocalDate

@jsonNoExtraFields
final case class CreateMealRequest(
  date: LocalDate,
  mealNumber: Option[Int],
  recipeId: EntityId[Recipe]
) derives Schema
