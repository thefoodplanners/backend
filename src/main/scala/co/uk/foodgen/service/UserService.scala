package co.uk.foodgen.service

import cats.effect.IO
import co.uk.foodgen.dao.UsersDao
import co.uk.foodgen.models.{DietaryRequirement, User}
import doobie.util.transactor.Transactor

final class UserService(using Transactor[IO]):
  def getTargetCalories(userId: User.Id): IO[Option[Int]] =
    UsersDao.selectTargetCalories(userId).tx

  def getDietaryRequirements(userId: User.Id): IO[List[DietaryRequirement]] =
    UsersDao.selectDietaryRequirements(userId).tx

  def updateDietaryRequirements(dietaryRequirements: List[DietaryRequirement], userId: User.Id): IO[Unit] =
    UsersDao.updateDietaryRequirements(userId, dietaryRequirements).tx
