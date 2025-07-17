package co.uk.foodgen.payload

import co.uk.foodgen.service.models.MetricView
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.scalaland.chimney.Transformer
import sttp.tapir.Schema

final case class MetricsResponse(
  labels: List[String],
  calories: List[Int],
  carbohydrates: List[Float],
  proteins: List[Float],
  fats: List[Float]
)

object MetricsResponse:
  given Transformer[List[MetricView], MetricsResponse] = (views: List[MetricView]) =>
    MetricsResponse(
      labels = views.map(_.label),
      calories = views.map(_.totalCalories),
      carbohydrates = views.map(_.totalCarbohydrates),
      proteins = views.map(_.totalProteins),
      fats = views.map(_.totalFats)
    )

  given Codec[MetricsResponse] = deriveCodec
  given Schema[MetricsResponse] = Schema
    .derived[MetricsResponse]
    .modify(_.labels)(_.copy(isOptional = false))
    .modify(_.calories)(_.copy(isOptional = false))
    .modify(_.carbohydrates)(_.copy(isOptional = false))
    .modify(_.proteins)(_.copy(isOptional = false))
    .modify(_.fats)(_.copy(isOptional = false))

end MetricsResponse
