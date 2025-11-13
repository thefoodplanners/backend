package co.uk.foodgen

import AuthenticationHelper.loginUser
import RecipesHelper.addRecipe
import co.uk.foodgen.{httpApp, setupDbLayer}
import co.uk.foodgen.domain.DietaryRequirement.*
import co.uk.foodgen.domain.Recipe
import zio.http.*
import zio.prelude.EqualOps
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec
import zio.test.*

import javax.sql.DataSource
import scala.language.strictEquality

object RecipeSpec extends ZIOSpecDefault:
  def spec =
    suite("Recipes")(
      suite("Searching for recipes")(
        test("should only return recipes matching the query") {
          for
            recipeId1 <- addRecipe("recipe1")
            _ <- addRecipe("recipe2")

            authToken <- loginUser()
            request = Request
              .get(URL.decode("/recipes/search?query=recipe1&limit=5&offset=0").toOption.get)
              .addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.map(_.id).toSet
            expected = Set(recipeId1)
          yield assertTrue(actual === expected)
        },
        test("should only return recipes matching the query even with dietary requirements") {
          for
            recipeId1 <- addRecipe("recipe1", diets = List(Vegetarian))
            _ <- addRecipe("recipe2", diets = List(Vegan))

            authToken <- loginUser()
            request = Request
              .get(URL.decode("/recipes/search?query=recipe1&limit=5&offset=0").toOption.get)
              .addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.map(_.id).toSet
            expected = Set(recipeId1)
          yield assertTrue(actual === expected)
        },
        test("should only return recipes that have the dietary requirements") {
          for
            recipeId1 <- addRecipe("recipe1", diets = List(Halal, Vegetarian))
            _ <- addRecipe("recipe2", diets = List(Halal))

            authToken <- loginUser()
            request = Request
              .get(URL.decode("/recipes/search?diets=halal&diets=vegetarian&limit=3&offset=0").toOption.get)
              .addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.map(_.id).toSet
            expected = Set(recipeId1)
          yield assertTrue(actual === expected)
        },
        test("should only return recipes that are below the calorie limit") {
          for
            _ <- addRecipe("recipe1", calories = 11)
            recipeId2 <- addRecipe("recipe2", calories = 10)
            recipeId3 <- addRecipe("recipe3", calories = 9)

            authToken <- loginUser()
            request = Request
              .get(URL.decode("/recipes/search?calories=10&limit=3&offset=0").toOption.get)
              .addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.map(_.id).toSet
            expected = Set(recipeId2, recipeId3)
          yield assertTrue(actual === expected)
        },
        test("should only return recipes that are below the carbohydrates limit") {
          for
            _ <- addRecipe("recipe1", carbohydrates = 10.1f)
            recipeId2 <- addRecipe("recipe2", carbohydrates = 10.0f)
            recipeId3 <- addRecipe("recipe3", carbohydrates = 9.9f)

            authToken <- loginUser()
            request = Request
              .get(URL.decode("/recipes/search?carbohydrates=10&limit=3&offset=0").toOption.get)
              .addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.map(_.id).toSet
            expected = Set(recipeId2, recipeId3)
          yield assertTrue(actual === expected)
        },
        test("should only return recipes that are below the proteins limit") {
          for
            _ <- addRecipe("recipe1", proteins = 10.1f)
            recipeId2 <- addRecipe("recipe2", proteins = 10.0f)
            recipeId3 <- addRecipe("recipe3", proteins = 9.9f)

            authToken <- loginUser()
            request = Request
              .get(URL.decode("/recipes/search?proteins=10&limit=3&offset=0").toOption.get)
              .addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.map(_.id).toSet
            expected = Set(recipeId2, recipeId3)
          yield assertTrue(actual === expected)
        },
        test("should only return recipes that are below the fats limit") {
          for
            _ <- addRecipe("recipe1", fats = 10.1f)
            recipeId2 <- addRecipe("recipe2", fats = 10.0f)
            recipeId3 <- addRecipe("recipe3", fats = 9.9f)

            authToken <- loginUser()
            request = Request
              .get(URL.decode("/recipes/search?fats=10&limit=3&offset=0").toOption.get)
              .addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.map(_.id).toSet
            expected = Set(recipeId2, recipeId3)
          yield assertTrue(actual === expected)
        },
        test("should only return a set number of recipes") {
          for
            _ <- addRecipe("recipe1")
            _ <- addRecipe("recipe2")
            _ <- addRecipe("recipe3")
            _ <- addRecipe("recipe4")

            authToken <- loginUser()
            request = Request.get(URL.decode("/recipes/search?limit=3&offset=0").toOption.get).addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.length
            expected = 3
          yield assertTrue(actual === expected)
        },
        test("should return recipes that are after the limit with offset") {
          for
            _ <- addRecipe("recipe1")
            _ <- addRecipe("recipe2")
            _ <- addRecipe("recipe3")
            _ <- addRecipe("recipe4")

            authToken <- loginUser()
            request = Request.get(URL.decode("/recipes/search?limit=5&offset=2").toOption.get).addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.length
            expected = 2
          yield assertTrue(actual === expected)
        }
      ),
      suite("Recipe recommendations")(
        test("should return recipes that meet users dietary requirements") {
          for
            recipeId1 <- addRecipe("recipe1", diets = List(Vegan, Vegetarian))
            _ <- addRecipe("recipe2", diets = List(Vegan))
            _ <- addRecipe("recipe3")

            authToken <- loginUser(dietaryRequirements = List(Vegan, Vegetarian))
            request = Request.get("/recipes/recommendations").addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.to[List[Recipe]]

            actual = content.map(_.id).toSet
            expected = Set(recipeId1)
          yield assertTrue(actual === expected)
        },
        test("should return recipes in random at every call") {
          for
            _ <- addRecipe("recipe1", diets = List(Vegan, Vegetarian))
            _ <- addRecipe("recipe2", diets = List(Vegan))
            _ <- addRecipe("recipe3")
            _ <- addRecipe("recipe4", diets = List(Halal))
            _ <- addRecipe("recipe5", diets = List(Kosher))
            _ <- addRecipe("recipe6")

            authToken <- loginUser()
            request = Request.get("/recipes/recommendations").addHeader(authToken)
            response1 <- httpApp(request)
            response2 <- httpApp(request)
            content1 <- response1.body.to[List[Recipe]]
            content2 <- response2.body.to[List[Recipe]]
          yield assertTrue(content1 !== content2)
        }
      )
    ).provideSomeLayer(setupDbLayer)
end RecipeSpec
