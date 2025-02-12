package co.uk.foodgen.helpers

import cats.effect.IO
import co.uk.foodgen.endpoints.{calendarMealsUrl, asString}
import co.uk.foodgen.models.Recipe
import co.uk.foodgen.payload.{CreateMealRequest, MealsResponse}
import co.uk.foodgen.service.models.Period
import org.http4s.Method.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.{HttpApp, Request, RequestCookie, Uri}

import java.time.LocalDate

object MealsHelper:

  def addMeal(
    date: LocalDate,
    mealNumber: Option[Int],
    recipeId: Recipe.Id
  )(using router: HttpApp[IO], loginToken: RequestCookie): IO[Unit] =
    val resource = CreateMealRequest(date, mealNumber, recipeId)
    val request = Request[IO](POST, Uri.unsafeFromString(calendarMealsUrl.asString))
      .withEntity(resource)
      .addCookie(loginToken)
    router(request).void

  def getMeals(
    date: LocalDate,
    period: Period
  )(using router: HttpApp[IO], loginToken: RequestCookie): IO[MealsResponse] =
    val request =
      Request[IO](GET, Uri.unsafeFromString(s"${calendarMealsUrl.asString}/$period?date=$date"))
        .addCookie(loginToken)
    router(request).flatMap(_.as[MealsResponse])

end MealsHelper
