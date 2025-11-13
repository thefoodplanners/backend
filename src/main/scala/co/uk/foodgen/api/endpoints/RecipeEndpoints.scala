package co.uk.foodgen.api.endpoints

import co.uk.foodgen.domain.Recipe
import zio.*
import zio.http.*
import zio.http.codec.{Doc, HttpContentCodec, PathCodec}
import zio.http.endpoint.*
import zio.schema.{Schema, derived}

object RecipeEndpoints extends APIEndpoint:
  case class SearchParams(
    query: Option[String],
    calories: Option[Int],
    carbohydrates: Option[Float],
    proteins: Option[Float],
    fats: Option[Float],
    diets: List[String],
    limit: Int,
    offset: Int
  ) derives Schema

  val getRecipesSearch =
    Endpoint(RoutePattern.GET / "recipes" / "search")
      .auth(AuthType.Bearer)
      .query[SearchParams]
      .out[List[Recipe]](Status.Ok, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  val getRecipeRecommendations =
    Endpoint(RoutePattern.GET / "recipes" / "recommendations")
      .auth(AuthType.Bearer)
      .out[List[Recipe]](Status.Ok, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  override val endpoints: List[Endpoint[_, _, _, _, _]] = List(getRecipesSearch, getRecipeRecommendations)
end RecipeEndpoints
