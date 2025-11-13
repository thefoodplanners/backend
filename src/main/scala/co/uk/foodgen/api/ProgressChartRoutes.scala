package co.uk.foodgen.api

import co.uk.foodgen.api.endpoints.ProgressChartEndpoints.getProgressChartPeriodMetrics
import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.Period
import co.uk.foodgen.payload.responses.MetricResponses.FullMetricsResponse
import co.uk.foodgen.services.ProgressChartService
import zio.ZIO
import zio.http.*
import zio.schema.{DeriveSchema, Schema, derived}

import java.time.LocalDate
import javax.sql.DataSource

object ProgressChartRoutes extends APIRoute:
  override val routes = Routes(
    getProgressChartPeriodMetrics.implement { (period: Period, date: LocalDate) =>
      (for
        userId <- ZIO.service[AuthInfo]
        metricsView <- ProgressChartService.fetchMetrics(date, period, userId)
        responseBody = FullMetricsResponse.fromView(metricsView)
      yield responseBody).toRoutesError
    }
  )
end ProgressChartRoutes
