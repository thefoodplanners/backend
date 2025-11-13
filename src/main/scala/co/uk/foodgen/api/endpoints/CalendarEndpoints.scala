package co.uk.foodgen.api.endpoints

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.Recipe
import co.uk.foodgen.payload.requests.CreateMealRequest
import co.uk.foodgen.payload.responses.MealResponses.MealDaysResponse
import zio.*
import zio.http.*
import zio.http.codec.{Doc, HttpCodec, HttpContentCodec, PathCodec}
import zio.http.endpoint.*
import zio.schema.Schema

import java.time.LocalDate

object CalendarEndpoints extends APIEndpoint:
  val getCalendarMealsPeriod =
    Endpoint(RoutePattern.GET / "calendar" / "meals" / pathCodecPeriod)
      .auth(AuthType.Bearer)
      .query(HttpCodec.query[LocalDate]("date"))
      .out[List[MealDaysResponse]](Status.Ok, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  val postCalendarMeals =
    Endpoint(RoutePattern.POST / "calendar" / "meals")
      .auth(AuthType.Bearer)
      .in[CreateMealRequest]
      .out[Unit](Status.Created, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  val putCalendarMealsId =
    Endpoint(RoutePattern.PUT / "calendar" / "meals" / pathCodecMealId)
      .auth(AuthType.Bearer)
      .query(HttpCodec.query[EntityId[Recipe]]("recipe-id"))
      .out[Unit](Status.NoContent, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  val putCalendarMealsIdMove =
    Endpoint(RoutePattern.PUT / "calendar" / "meals" / pathCodecMealId / "move")
      .auth(AuthType.Bearer)
      .query(HttpCodec.query[LocalDate]("date"))
      .query(HttpCodec.query[Int]("meal-number"))
      .out[Unit](Status.NoContent, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  val deleteCalendarMealId =
    Endpoint(RoutePattern.DELETE / "calendar" / "meals" / pathCodecMealId)
      .auth(AuthType.Bearer)
      .out[Unit](Status.NoContent, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  override val endpoints: List[Endpoint[_, _, _, _, _]] = List(
    getCalendarMealsPeriod,
    postCalendarMeals,
    putCalendarMealsId,
    putCalendarMealsIdMove,
    deleteCalendarMealId
  )
end CalendarEndpoints
