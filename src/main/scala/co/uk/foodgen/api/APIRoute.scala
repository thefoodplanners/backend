package co.uk.foodgen.api

import zio.http.Routes

import javax.sql.DataSource
import scala.Throwable

trait APIRoute:
  def unauthenticatedRoutes: Routes[DataSource, Throwable] = Routes.empty
  def routes: Routes[DataSource & AuthInfo, Throwable] = Routes.empty
