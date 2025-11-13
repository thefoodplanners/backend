package co.uk.foodgen.api.endpoints

import co.uk.foodgen.payload.responses.MetricResponses.FullMetricsResponse
import zio.*
import zio.http.*
import zio.http.codec.{Doc, HttpCodec, HttpContentCodec, PathCodec}
import zio.http.endpoint.*
import zio.schema.Schema

import java.time.LocalDate

object ProgressChartEndpoints extends APIEndpoint:
  val getProgressChartPeriodMetrics =
    Endpoint(RoutePattern.GET / "progress-chart" / pathCodecPeriod / "metrics")
      .auth(AuthType.Bearer)
      .query(HttpCodec.query[LocalDate]("date"))
      .out[FullMetricsResponse](Status.Ok, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  override val endpoints: List[Endpoint[_, _, _, _, _]] = List(getProgressChartPeriodMetrics)
end ProgressChartEndpoints
