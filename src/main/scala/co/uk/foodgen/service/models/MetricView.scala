package co.uk.foodgen.service.models

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
