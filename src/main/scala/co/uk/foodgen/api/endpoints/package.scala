package co.uk.foodgen.api

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.{Meal, Period}
import zio.http.codec.PathCodec

package object endpoints:
  private val pathCodecMealId = PathCodec.long("meal-id").transform[EntityId[Meal]](EntityId.apply)(_.value)
  private val pathCodecPeriod = PathCodec
    .string("period")
    .transform[Period](period => Period.valueOf(period.toLowerCase.capitalize))(_.productPrefix.toLowerCase)
end endpoints
