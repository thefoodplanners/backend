package co.uk.foodgen.payload

import co.uk.foodgen.service.models.FullMetricsView
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.scalaland.chimney.Transformer
import io.scalaland.chimney.syntax.transformInto
import sttp.tapir.Schema

final case class FullMetricsResponse(
  label: String,
  metrics: List[MetricsResponse]
)

object FullMetricsResponse:
  given Transformer[FullMetricsView, FullMetricsResponse] = (view: FullMetricsView) =>
    FullMetricsResponse(
      label = view.label,
      metrics = view.metrics.map(_.transformInto[MetricsResponse])
    )

  given Codec[FullMetricsResponse] = deriveCodec
  given Schema[FullMetricsResponse] = Schema.derived[FullMetricsResponse].modify(_.metrics)(_.copy(isOptional = false))
