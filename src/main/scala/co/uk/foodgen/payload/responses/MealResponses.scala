package co.uk.foodgen.payload.responses

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.{Meal, MealView, Recipe}
import zio.json.JsonEncoder
import zio.schema.{Schema, derived}

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.{DayOfWeek, LocalDate}
import scala.annotation.targetName
import scala.jdk.CollectionConverters.IteratorHasAsScala

object MealResponses:
  final case class MealsResponse private (private val meals: List[MealDaysResponse]) derives CanEqual

  object MealsResponse:
    given Schema[MealsResponse] = Schema.list[MealDaysResponse].transform(MealsResponse.apply, _.meals)
    given JsonEncoder[MealsResponse] = JsonEncoder.list[MealDaysResponse].contramap(_.meals)

    def apply(views: List[MealView], date: LocalDate): List[MealDaysResponse] =
      val startOfWeek =
        date.`with`(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
      val endOfWeek =
        date.`with`(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
      val dates = startOfWeek
        .datesUntil(endOfWeek.plusDays(1))
        .iterator()
        .asScala
        .toList
      val viewsByMealNumber = views.groupBy(_.mealNumber)
      val maxMealNumber = Math.max(views.map(_.mealNumber).maxOption.getOrElse(1), 3)
      List
        .range(1, maxMealNumber + 1)
        .map(mealNumber => MealDaysResponse(viewsByMealNumber.getOrElse(mealNumber, List.empty), dates, mealNumber))
  end MealsResponse

  final case class MealDaysResponse(
    monday: MealResponseWithDateAndMealNumber,
    tuesday: MealResponseWithDateAndMealNumber,
    wednesday: MealResponseWithDateAndMealNumber,
    thursday: MealResponseWithDateAndMealNumber,
    friday: MealResponseWithDateAndMealNumber,
    saturday: MealResponseWithDateAndMealNumber,
    sunday: MealResponseWithDateAndMealNumber
  ) derives Schema,
      JsonEncoder

  private object MealDaysResponse:
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
        .find(mv => mv.date.getDayOfWeek.getValue == dayOfWeekValue)
        .fold(MealResponseWithDateAndMealNumber(date, mealNumber, view = None))(mv =>
          MealResponseWithDateAndMealNumber(mv.date, mv.mealNumber, view = Some(mv))
        )
  end MealDaysResponse

  final case class MealResponseWithDateAndMealNumber(
    date: LocalDate,
    mealNumber: Int,
    meal: Option[MealResponse]
  ) derives Schema,
      JsonEncoder

  private object MealResponseWithDateAndMealNumber:
    @targetName("realApply")
    def apply(date: LocalDate, mealNumber: Int, view: Option[MealView]): MealResponseWithDateAndMealNumber =
      MealResponseWithDateAndMealNumber(
        date = date,
        mealNumber = mealNumber,
        meal = view.map(MealResponse.apply)
      )
  end MealResponseWithDateAndMealNumber

  final case class MealResponse(
    mealId: EntityId[Meal],
    day: String,
    recipe: Recipe
  ) derives Schema,
      JsonEncoder

  private object MealResponse:
    def apply(view: MealView): MealResponse =
      MealResponse(
        mealId = view.id,
        day = view.date.format(DateTimeFormatter.ofPattern("EEEE")),
        recipe = view.recipe
      )
end MealResponses
