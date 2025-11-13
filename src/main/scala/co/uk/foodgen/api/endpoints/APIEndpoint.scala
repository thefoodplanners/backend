package co.uk.foodgen.api.endpoints

import zio.http.endpoint.Endpoint

trait APIEndpoint:
  def endpoints: List[Endpoint[_, _, _, _, _]]
