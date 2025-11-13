package co.uk.foodgen.payload.responses

import co.uk.foodgen.domain.DietaryRequirement
import zio.schema.Schema

final case class UserDietaryRequirementsResponse(
  dietaryRequirements: List[DietaryRequirement]
)

object UserDietaryRequirementsResponse:
  given Schema[UserDietaryRequirementsResponse] = Schema
    .list[DietaryRequirement]
    .transform(UserDietaryRequirementsResponse.apply, _.dietaryRequirements)
