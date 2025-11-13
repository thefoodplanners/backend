package co.uk.foodgen.services.models

object MetricViews:
  case class FullMetricsView(
    label: String,
    metrics: List[MetricView]
  )

  case class MetricView(
    label: String,
    totalCalories: Int,
    totalCarbohydrates: Float,
    totalProteins: Float,
    totalFats: Float
  )
