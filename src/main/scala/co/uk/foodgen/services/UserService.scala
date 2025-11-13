package co.uk.foodgen.services

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.User
import co.uk.foodgen.repositories.UserRepository

object UserService:
  def fetchTargetCalories(userId: EntityId[User]) =
    UserRepository.selectTargetCalories(userId)

  def fetchDietaryRequirements(userId: EntityId[User]) =
    UserRepository.selectDietaryRequirements(userId)

end UserService
