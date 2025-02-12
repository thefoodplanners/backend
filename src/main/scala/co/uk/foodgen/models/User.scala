package co.uk.foodgen.models

import doobie.*
import doobie.implicits.*
import doobie.util.meta.Meta
import doobie.util.{Read, Write}

case class User(
  id: User.Id,
  email: String,
  username: String,
  password: String,
  targetCalories: Option[Int],
  dietaryRequirements: List[DietaryRequirement]
)

object User extends HasId:
  given Write[User] =
    Write[(String, String, String, Option[Int], List[DietaryRequirement])].contramap(u =>
      (u.email, u.username, u.password, u.targetCalories, u.dietaryRequirements)
    )

  given Read[User] = Read[(User.Id, String, String, String, Option[Int], List[DietaryRequirement])]
    .map(User.apply _ tupled _)
