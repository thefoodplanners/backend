package co.uk.foodgen.payload

import cats.syntax.eq.catsSyntaxEq
import co.uk.foodgen.payload
import co.uk.foodgen.service.models.MealView
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Decoder, Encoder}
import io.scalaland.chimney.syntax.transformInto
import sttp.tapir.Schema

import java.time.LocalDate

final case class MealDaysResponse(
  monday: MealResponseWithDate,
  tuesday: MealResponseWithDate,
  wednesday: MealResponseWithDate,
  thursday: MealResponseWithDate,
  friday: MealResponseWithDate,
  saturday: MealResponseWithDate,
  sunday: MealResponseWithDate
)

object MealDaysResponse:
  // views is already grouped by meal number
  def apply(views: List[MealView], dates: List[LocalDate], mealNumber: Int): MealDaysResponse =
    MealDaysResponse(
      monday = buildMealWithDateResponse(views, dates.head, 1),
      tuesday = buildMealWithDateResponse(views, dates(1), 2),
      wednesday = buildMealWithDateResponse(views, dates(2), 3),
      thursday = buildMealWithDateResponse(views, dates(3), 4),
      friday = buildMealWithDateResponse(views, dates(4), 5),
      saturday = buildMealWithDateResponse(views, dates(5), 6),
      sunday = buildMealWithDateResponse(views, dates(6), 7)
    )

  private def buildMealWithDateResponse(views: List[MealView], date: LocalDate, dayOfWeekValue: Int): MealResponseWithDate =
    views
      .find(mv => mv.date.getDayOfWeek.getValue === dayOfWeekValue)
      .fold(MealResponseWithDate.create(date, view = None))(mv => MealResponseWithDate.create(mv.date, view = Some(mv)))

  given Codec[MealDaysResponse] = deriveCodec
  given Schema[MealDaysResponse] = Schema.derived
