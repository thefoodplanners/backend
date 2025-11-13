package co.uk.foodgen.domain

import io.getquill.{InsertMeta, insertMeta}
import zio.json.JsonEncoder
import zio.prelude.{Equal, EqualOps}
import zio.schema.{Schema, derived}

import Entity.EntityId

case class Recipe(
  id: EntityId[Recipe],
  name: String,
  mealType: MealType,
  description: Option[String],
  imageUrl: String,
  calories: Int,
  carbohydrates: Float,
  proteins: Float,
  fats: Float,
  dietaryRequirements: List[DietaryRequirement]
) derives Schema,
    JsonEncoder

object Recipe:
  inline given InsertMeta[Recipe] = insertMeta(_.id)
  given Equal[Recipe] = Equal.make((r1, r2) => r1.id === r2.id)
