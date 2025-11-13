package co.uk.foodgen.repositories

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.{DietaryRequirement, User}
import io.getquill.*
import io.getquill.context.ZioJdbc.QIO
import zio.*

import java.sql.SQLException
import javax.sql.DataSource

object UserRepository:
  import DbContext.*

  def insertUser(user: User): QIO[EntityId[User]] =
    run(query[User].insertValue(lift(user)).returningGenerated(_.id))

  def selectUserById(userId: EntityId[User]): QIO[Option[User]] =
    run(query[User].filter(_.id == lift(userId)).value)

  def selectUserByIdOrFail(userId: EntityId[User]): QIO[User] =
    selectUserById(userId).expectOne

  def selectUser(username: String): QIO[Option[User]] =
    run(query[User].filter(_.username == lift(username)).value)

  def selectUserOrFail(username: String): QIO[User] =
    selectUser(username).expectOne

  def selectTargetCalories(userId: EntityId[User]): QIO[Option[Int]] =
    run(query[User].filter(_.id == lift(userId)).map(_.targetCalories).value).expectOne

  def selectDietaryRequirements(userId: EntityId[User]): QIO[List[DietaryRequirement]] =
    run(query[User].filter(_.id == lift(userId)).map(_.dietaryRequirements).value).expectOne

end UserRepository
