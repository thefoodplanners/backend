package co.uk.foodgen.payload

import co.uk.foodgen.service.models.MetricView
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.scalaland.chimney.Transformer
import sttp.tapir.Schema

case class MetricsResponse(
  label: String,
  totalCalories: Int,
  totalCarbohydrates: Float,
  totalProteins: Float,
  totalFats: Float
)

object MetricsResponse:
  given Transformer[MetricView, MetricsResponse] = (view: MetricView) =>
    MetricsResponse(
      label = view.label,
      totalCalories = view.totalCalories,
      totalCarbohydrates = view.totalCarbohydrates,
      totalProteins = view.totalProteins,
      totalFats = view.totalFats
    )

  given Codec[MetricsResponse] = deriveCodec
  given Schema[MetricsResponse] = Schema.derived
