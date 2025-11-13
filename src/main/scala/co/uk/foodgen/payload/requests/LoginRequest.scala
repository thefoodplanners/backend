package co.uk.foodgen.payload.requests

import zio.json.jsonNoExtraFields
import zio.schema.{Schema, derived}

@jsonNoExtraFields
final case class LoginRequest(
  username: String,
  password: String
) derives Schema
