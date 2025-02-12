package co.uk.foodgen.service

import cats.effect.IO
import co.uk.foodgen.dao.MealsDao
import co.uk.foodgen.models.User
import co.uk.foodgen.service.models.*
import doobie.util.transactor.Transactor

import java.time.format.{DateTimeFormatter, TextStyle}
import java.time.temporal.TemporalAdjusters
import java.time.{DayOfWeek, LocalDate}
import java.util.Locale

final class ProgressChartService(using Transactor[IO]):
  private def populateMetrics(
    mainLabel: String,
    mealViewsWithLabel: List[(String, List[MealView])]
  ): FullMetricsView =
    val metricViews = mealViewsWithLabel.map { (label, mealViews) =>
      MetricView(
        label = label,
        totalCalories = mealViews.map(_.recipe.calories).sum,
        totalCarbohydrates = mealViews.map(_.recipe.carbohydrates).sum,
        totalProteins = mealViews.map(_.recipe.proteins).sum,
        totalFats = mealViews.map(_.recipe.fats).sum
      )
    }

    FullMetricsView(label = mainLabel, metrics = metricViews)

  def fetchMetrics(
    date: LocalDate,
    period: Period,
    userId: User.Id
  ): IO[FullMetricsView] =
    for
      mealViews <- MealsDao.selectMealsByPeriod(date, period, userId).tx
      fullMetricView =
        period match
          case Period.Day =>
            val mealViewsByMealNumber =
              mealViews.map(mv => s"Meal ${mv.mealNumber}" -> List(mv))
            populateMetrics(
              mainLabel = date.format(DateTimeFormatter.ofPattern("EEEE")),
              mealViewsWithLabel = mealViewsByMealNumber
            )
          case Period.Week =>
            val mealViewsByDay = mealViews
              .groupBy(_.date.getDayOfWeek)
              .toList
              .sortBy(_._1)
              .map { (dayOfWeek, mvs) =>
                val label = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault)
                label -> mvs.sortBy(mv => mv.date -> mv.mealNumber)
              }
            val startOfWeek =
              date.`with`(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).getDayOfMonth
            val endOfWeek =
              date.`with`(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).getDayOfMonth
            populateMetrics(
              mainLabel = date.format(DateTimeFormatter.ofPattern(s"LLL $startOfWeek-$endOfWeek")),
              mealViewsWithLabel = mealViewsByDay
            )
          case Period.Month =>
            val mealViewsByWeek = mealViews
              .groupBy(_.date.`with`(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
              .toList
              .sortBy(_._1)
              .map { (startOfWeek, mealViews) =>
                val endOfWeek =
                  startOfWeek.`with`(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).getDayOfMonth
                val label =
                  startOfWeek.format(DateTimeFormatter.ofPattern(s"LLL ${startOfWeek.getDayOfMonth}-$endOfWeek"))
                label -> mealViews.sortBy(mv => mv.date -> mv.mealNumber)
              }
            populateMetrics(
              mainLabel = date.format(DateTimeFormatter.ofPattern("LLLL")),
              mealViewsWithLabel = mealViewsByWeek
            )
          case Period.Year =>
            val mealViewsByMonth = mealViews
              .groupBy(_.date.getMonth)
              .toList
              .sortBy(_._1)
              .map { (month, mealViews) =>
                val label = month.getDisplayName(TextStyle.SHORT, Locale.getDefault)
                label -> mealViews.sortBy(mv => mv.date -> mv.mealNumber)
              }
            populateMetrics(
              mainLabel = date.format(DateTimeFormatter.ofPattern("uuuu")),
              mealViewsWithLabel = mealViewsByMonth
            )
    yield fullMetricView
