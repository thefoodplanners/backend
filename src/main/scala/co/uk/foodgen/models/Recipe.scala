package co.uk.foodgen.models

import cats.Order
import doobie.util.meta.Meta
import doobie.util.{Read, Write}
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import neotype.interop.circe.given
import sttp.tapir.Schema

case class Recipe(
  id: Recipe.Id,
  name: String,
  mealType: MealType,
  description: Option[String],
  imageUrl: String,
  calories: Int,
  carbohydrates: Float,
  proteins: Float,
  fats: Float,
  diets: List[DietaryRequirement]
)

object Recipe extends HasId:
  given Write[Recipe] = Write[
    (
      String,
      MealType,
      Option[String],
      String,
      Int,
      Float,
      Float,
      Float,
      List[DietaryRequirement]
    )
  ].contramap(r =>
    (
      r.name,
      r.mealType,
      r.description,
      r.imageUrl,
      r.calories,
      r.carbohydrates,
      r.proteins,
      r.fats,
      r.diets
    )
  )

  given Read[Recipe] = Read[
    (
      Recipe.Id,
      String,
      MealType,
      Option[String],
      String,
      Int,
      Float,
      Float,
      Float,
      List[DietaryRequirement]
    )
  ].map(Recipe.apply _ tupled _)

  given Order[Recipe] = (x: Recipe, y: Recipe) => x.name compare y.name

  given Codec[Recipe] = deriveCodec
  given Schema[Recipe] = Schema.derived
