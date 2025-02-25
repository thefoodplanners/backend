package co.uk.foodgen.endpoints

import cats.data.Ior
import cats.effect.IO
import co.uk.foodgen.models
import co.uk.foodgen.models.User
import co.uk.foodgen.payload.FullMetricsResponse
import co.uk.foodgen.service.ProgressChartService
import co.uk.foodgen.service.models.Period
import doobie.util.transactor.Transactor
import io.scalaland.chimney.dsl.transformInto
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.*

import java.time.LocalDate

class ProgressChartEndpoints(transactor: Transactor[IO]) extends HttpEndpoint:

  private lazy val progressChartService = new ProgressChartService(using transactor)

  private val getFullMetrics = endpoint.get
    .in(progressChartUrl / path[Period]("period") / "metrics")
    .in(query[LocalDate]("date"))
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(jsonBody[FullMetricsResponse])
    .serverLogicSuccess[IO] { case (period, date, userId, _) =>
      progressChartService
        .fetchMetrics(
          date = date,
          period = period,
          userId = userId
        )
        .map(_.transformInto[FullMetricsResponse])
    }

  val endpoints = List(getFullMetrics)

  val routes =
    val authRoutes = Http4sServerInterpreter[IO]().toContextRoutes(endpoints)
    Ior.right(authRoutes)

end ProgressChartEndpoints
