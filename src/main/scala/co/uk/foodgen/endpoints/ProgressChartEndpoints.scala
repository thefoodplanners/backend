package co.uk.foodgen.endpoints

import cats.effect.IO
import co.uk.foodgen.models.User
import co.uk.foodgen.payload.FullMetricsResponse
import co.uk.foodgen.service.ProgressChartService
import co.uk.foodgen.service.models.Period
import doobie.util.transactor.Transactor
import io.scalaland.chimney.dsl.transformInto
import org.http4s.ContextRoutes
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.*

import java.time.LocalDate

class ProgressChartEndpoints(transactor: Transactor[IO]):

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

  val allEndpoints = List(getFullMetrics)

  val allRoutes: ContextRoutes[User.Id, IO] =
    Http4sServerInterpreter[IO]().toContextRoutes(allEndpoints)

end ProgressChartEndpoints
