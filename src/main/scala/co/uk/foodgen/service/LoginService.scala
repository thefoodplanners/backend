package co.uk.foodgen.service

import cats.effect.IO
import cats.syntax.traverse.toTraverseOps
import co.uk.foodgen.dao.UsersDao
import co.uk.foodgen.endpoints
import co.uk.foodgen.endpoints.encrypt
import co.uk.foodgen.models.{DietaryRequirement, User}
import co.uk.foodgen.service.errorModels.ServiceError
import doobie.util.transactor.Transactor
import org.mindrot.jbcrypt.BCrypt

import java.time.Instant

final class LoginService(using Transactor[IO]):

  def register(
    email: String,
    username: String,
    password: String,
    targetCalories: Option[Int],
    dietaryRequirements: List[DietaryRequirement]
  ): ServiceResult[Unit] =
    for
      _ <- UsersDao
        .selectUser(username)
        .failIfFound(ServiceError.BadRequest("Username already exists"))
      hashed = BCrypt.hashpw(password, BCrypt.gensalt(12))
      user = User(
        id = User.Id(0),
        email = email,
        username = username,
        password = hashed,
        targetCalories = targetCalories,
        dietaryRequirements = dietaryRequirements
      )
      _ <- UsersDao.insertUser(user).validTx
    yield ()

  def login(username: String, password: String): IO[Option[String]] =
    for
      maybeUser <- UsersDao.selectUser(username).tx
      sessionKey <- maybeUser
        .filter(user => BCrypt.checkpw(password, user.password))
        .traverse(user => encrypt.map(_.signToken(user.id.toString, Instant.now.toEpochMilli.toString)))
    yield sessionKey
