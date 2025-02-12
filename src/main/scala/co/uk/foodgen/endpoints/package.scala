package co.uk.foodgen

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.effect.std.Random
import cats.implicits.catsSyntaxEq
import cats.syntax.traverse.toTraverseOps
import co.uk.foodgen.models.User
import org.http4s.server.AuthMiddleware
import org.http4s.{ContextRoutes, HttpRoutes, Request, RequestCookie}
import org.reactormonk.{CryptoBits, PrivateKey}
import sttp.tapir.*

import scala.io.Codec

package object endpoints:
  private lazy val rand = Random.scalaUtilRandomSeedInt[IO](42)

  val encrypt: IO[CryptoBits] =
    for
      r <- rand
      nonce <- r.nextString(20)
      key = PrivateKey(Codec.toUTF8(nonce))
      encrypt = CryptoBits(key)
    yield encrypt

  def decryptUserIdFromCookie(cookie: RequestCookie): IO[Option[User.Id]] =
    encrypt.map(cb =>
      for
        validatedId <- cb.validateSignedToken(cookie.content)
        id <- validatedId.toIntOption
        userId = User.Id.apply(id)
      yield userId
    )

  private val authUser: Kleisli[OptionT[IO, *], Request[IO], User.Id] =
    Kleisli { request =>
      val maybeUserId = request.cookies
        .find(_.name === "SESSION_KEY")
        .flatTraverse(decryptUserIdFromCookie)
      OptionT(maybeUserId)
    }

  private val authMiddleware: AuthMiddleware[IO, User.Id] = AuthMiddleware(authUser)

  extension (cxtRoutes: ContextRoutes[User.Id, IO]) def withAuthentication: HttpRoutes[IO] = authMiddleware(cxtRoutes)

  val registerUrl = "register"
  val loginUrl = "login"
  val targetCaloriesUrl = "users" / "target-calories"
  val dietaryRequirementsUrl = "users" / "dietary-requirements"
  val recipeSearchUrl = "recipes" / "search"
  val recipeRecommendationsUrl = "recipes" / "recommendations"
  val calendarMealsUrl = "calendar" / "meals"
  val progressChartUrl = "progress-chart"

  extension [A](inp: EndpointInput[A]) def asString: String = inp.show.filterNot(_.isWhitespace)

end endpoints
