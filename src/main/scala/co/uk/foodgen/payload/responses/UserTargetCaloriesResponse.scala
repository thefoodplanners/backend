package co.uk.foodgen.payload.responses

import zio.schema.Schema

final case class UserTargetCaloriesResponse(
  targetCalories: Option[Int]
)

object UserTargetCaloriesResponse:
  given Schema[UserTargetCaloriesResponse] =
    Schema.option[Int].transform(UserTargetCaloriesResponse.apply, _.targetCalories)
