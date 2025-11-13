package co.uk.foodgen.payload.responses

import co.uk.foodgen.services.models.MetricViews.FullMetricsView
import zio.prelude.Equal
import zio.schema.{Schema, derived}

object MetricResponses:
  final case class FullMetricsResponse(
    label: String,
    metrics: MetricsResponse
  ) derives Schema,
      Equal

  object FullMetricsResponse:
    def fromView(view: FullMetricsView): FullMetricsResponse =
      val metricsResponse = MetricsResponse(
        labels = view.metrics.map(_.label),
        calories = view.metrics.map(_.totalCalories),
        carbohydrates = view.metrics.map(_.totalCarbohydrates),
        proteins = view.metrics.map(_.totalProteins),
        fats = view.metrics.map(_.totalFats)
      )
      FullMetricsResponse(
        label = view.label,
        metrics = metricsResponse
      )

  final case class MetricsResponse(
    labels: List[String],
    calories: List[Int],
    carbohydrates: List[Float],
    proteins: List[Float],
    fats: List[Float]
  ) derives Schema,
      Equal
end MetricResponses
