package co.uk.foodgen.domain

import io.getquill.{InsertMeta, insertMeta}

import Entity.EntityId

final case class User(
  id: EntityId[User],
  email: String,
  username: String,
  password: String,
  targetCalories: Option[Int],
  dietaryRequirements: List[DietaryRequirement]
)

object User:
  inline given InsertMeta[User] = insertMeta(_.id)
