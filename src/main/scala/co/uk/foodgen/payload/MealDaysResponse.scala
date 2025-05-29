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
  monday: MealResponseWithDateAndMealNumber,
  tuesday: MealResponseWithDateAndMealNumber,
  wednesday: MealResponseWithDateAndMealNumber,
  thursday: MealResponseWithDateAndMealNumber,
  friday: MealResponseWithDateAndMealNumber,
  saturday: MealResponseWithDateAndMealNumber,
  sunday: MealResponseWithDateAndMealNumber
)

object MealDaysResponse:
  // views is already grouped by meal number
  def apply(views: List[MealView], dates: List[LocalDate], mealNumber: Int): MealDaysResponse =
    MealDaysResponse(
      monday = buildMealWithDateResponse(views, dates.head, mealNumber, 1),
      tuesday = buildMealWithDateResponse(views, dates(1), mealNumber, 2),
      wednesday = buildMealWithDateResponse(views, dates(2), mealNumber, 3),
      thursday = buildMealWithDateResponse(views, dates(3), mealNumber, 4),
      friday = buildMealWithDateResponse(views, dates(4), mealNumber, 5),
      saturday = buildMealWithDateResponse(views, dates(5), mealNumber, 6),
      sunday = buildMealWithDateResponse(views, dates(6), mealNumber, 7)
    )

  private def buildMealWithDateResponse(
    views: List[MealView],
    date: LocalDate,
    mealNumber: Int,
    dayOfWeekValue: Int
  ): MealResponseWithDateAndMealNumber =
    views
      .find(mv => mv.date.getDayOfWeek.getValue === dayOfWeekValue)
      .fold(MealResponseWithDateAndMealNumber.create(date, mealNumber, view = None))(mv =>
        MealResponseWithDateAndMealNumber.create(mv.date, mv.mealNumber, view = Some(mv))
      )

  given Codec[MealDaysResponse] = deriveCodec
  given Schema[MealDaysResponse] = Schema.derived
