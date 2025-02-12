package co.uk.foodgen.dao

import cats.implicits.toFunctorOps
import co.uk.foodgen.models.{DietaryRequirement, User}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

object UsersDao:
  def insertUser(user: User): ConnectionIO[User.Id] =
    sql"""
         insert into users (
           email,
           username,
           password,
           target_calories,
           dietary_requirements
         )
         values (
           ${user.email},
           ${user.username},
           ${user.password},
           ${user.targetCalories},
           ${user.dietaryRequirements}::diet[]
         )""".update
      .withUniqueGeneratedKeys[User.Id]("id")

  def selectUser(username: String): ConnectionIO[Option[User]] =
    sql"select * from users where username = $username"
      .query[User]
      .option

  def selectTargetCalories(userId: User.Id): ConnectionIO[Option[Int]] =
    sql"select target_calories from users where id = $userId".query[Int].option

  def selectDietaryRequirements(userId: User.Id): ConnectionIO[List[DietaryRequirement]] =
    sql"select dietary_requirements from users where id = $userId"
      .query[List[DietaryRequirement]]
      .unique

  def updateDietaryRequirements(
    userId: User.Id,
    dietaryRequirements: List[DietaryRequirement]
  ): ConnectionIO[Unit] =
    sql"update users set dietary_requirements = $dietaryRequirements::diet[] where id = $userId".update.run.void
