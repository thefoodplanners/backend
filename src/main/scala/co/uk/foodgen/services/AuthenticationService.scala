package co.uk.foodgen.services

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.{DietaryRequirement, User}
import co.uk.foodgen.repositories.UserRepository
import de.mkammerer.argon2.Argon2Factory
import de.mkammerer.argon2.Argon2Factory.Argon2Types
import zio.ZIO

object AuthenticationService:
  def register(
    email: String,
    username: String,
    password: String,
    targetCalories: Option[Int],
    dietaryRequirements: List[DietaryRequirement]
  ) =
    val argon2 = Argon2Factory.create(Argon2Types.ARGON2id)
    for
      hash <- ZIO.attemptBlocking(argon2.hash(3, 65536, 1, password.toCharArray))
      user = User(EntityId(0), email, username, hash, targetCalories, dietaryRequirements)
      id <- UserRepository.insertUser(user)
    yield id

  def login(username: String, password: String) =
    val argon2 = Argon2Factory.create(Argon2Types.ARGON2id)
    for
      user <- UserRepository.selectUserOrFail(username)
      isCorrect <- ZIO.attemptBlocking(argon2.verify(user.password, password.toCharArray))
    yield Option.when(isCorrect)(user.id)

end AuthenticationService
