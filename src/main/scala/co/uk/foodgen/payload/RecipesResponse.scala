package co.uk.foodgen.payload

import co.uk.foodgen.models.Recipe
import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder}
import sttp.tapir.Schema

case class RecipesResponse(recipes: List[Recipe])

object RecipesResponse:
  given Codec[RecipesResponse] = Codec.from(
    Decoder.decodeList[Recipe].map(RecipesResponse.apply),
    Encoder.instance(_.recipes.asJson)
  )
  given Schema[RecipesResponse] = Schema.schemaForIterable[Recipe, List].as[RecipesResponse]
