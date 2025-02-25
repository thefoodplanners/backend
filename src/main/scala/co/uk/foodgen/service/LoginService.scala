package co.uk.foodgen.service

import cats.data.OptionT
import cats.effect.IO
import co.uk.foodgen.dao.UsersDao
import co.uk.foodgen.endpoints
import co.uk.foodgen.endpoints.Authentication
import co.uk.foodgen.models.{DietaryRequirement, User}
import co.uk.foodgen.service.errorModels.ServiceError
import doobie.util.transactor.Transactor
import tsec.authentication.AuthenticatedCookie
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

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
      hashed <- SCrypt.hashpw[IO](password).toResult
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
    (for
      user <- OptionT(UsersDao.selectUser(username).tx)
      _ <- OptionT(SCrypt.checkpwBool[IO](password, PasswordHash(user.password)).map(Option.when(_)(())))
      sessionCookie <- OptionT.liftF(Authentication.handler.authenticator.create(user.id))
      sessionKey = sessionCookie.content
    yield sessionKey).value

  def logout(auth: AuthenticatedCookie[HMACSHA256, User.Id]): IO[Unit] =
    Authentication.handler.authenticator.discard(auth).void
