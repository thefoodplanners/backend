package co.uk.foodgen.payload.requests

import co.uk.foodgen.domain.DietaryRequirement
import zio.json.jsonNoExtraFields
import zio.schema.{Schema, derived}

@jsonNoExtraFields
final case class RegisterRequest(
  email: String,
  username: String,
  password: String,
  targetCalories: Option[Int],
  dietaryRequirements: List[DietaryRequirement]
) derives Schema
