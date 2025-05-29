package co.uk.foodgen.payload

import co.uk.foodgen.service.models.MealView
import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder}
import sttp.tapir.Schema

import java.time.temporal.TemporalAdjusters
import java.time.{DayOfWeek, LocalDate}
import scala.jdk.CollectionConverters.IteratorHasAsScala

final case class MealsResponse(meals: List[MealDaysResponse])

object MealsResponse:
  def apply(views: List[MealView], date: LocalDate): MealsResponse =
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
    println(s"VIEWS: $viewsByMealNumber")
    val maxMealNumber = Math.max(views.map(_.mealNumber).maxOption.getOrElse(1), 3)
    val meals = List.range(1, maxMealNumber + 1).map(mealNumber => MealDaysResponse(viewsByMealNumber.getOrElse(mealNumber, List.empty), dates, mealNumber))
    MealsResponse(meals)

  given Codec[MealsResponse] = Codec.from(
    Decoder.decodeList[MealDaysResponse].map(MealsResponse.apply),
    Encoder.instance(_.meals.asJson)
  )
  given Schema[MealsResponse] = Schema.schemaForIterable[MealDaysResponse, List].as[MealsResponse]
