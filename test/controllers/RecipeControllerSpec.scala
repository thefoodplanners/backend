package controllers

import controllers.api.RecipeController
import models.{ FetchedMealSlot, Preferences, Recipe, RecipeDao }
import org.joda.time.LocalDate
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{ GET, contentAsString, defaultAwaitTimeout }
import play.api.test.{ FakeRequest, Helpers }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }
import scala.io.Source

class RecipeControllerSpec extends AnyFlatSpec with MockitoSugar {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val recipeExample1: Recipe = Recipe(
    id = 1,
    name = "name1",
    mealType = "mealType1",
    desc = "desc1",
    time = 1,
    difficulty = "difficulty1",
    ingredients = "ingredients1",
    calories = 1,
    fats = 1.0f,
    proteins = 1.0f,
    carbohydrates = 1.0f,
    preferences = Preferences(
      isVegan = false,
      isVegetarian = false,
      isKeto = false,
      isLactose = false,
      isHalal = false,
      isKosher = false,
      isDairyFree = false,
      isLowCarbs = false
    )
  )

  val recipeExample2: Recipe = Recipe(
    id = 2,
    name = "name2",
    mealType = "mealType2",
    desc = "desc2",
    time = 2,
    difficulty = "difficulty2",
    ingredients = "ingredients2",
    calories = 2,
    fats = 2.0f,
    proteins = 2.0f,
    carbohydrates = 2.0f,
    preferences = Preferences(
      isVegan = false,
      isVegetarian = false,
      isKeto = false,
      isLactose = false,
      isHalal = false,
      isKosher = false,
      isDairyFree = false,
      isLowCarbs = false
    )
  )

  val fetchedMealSlotExample: Seq[FetchedMealSlot] = Seq(
    FetchedMealSlot(LocalDate.parse("2023-01-30").toDate, 1, recipeExample1),
    FetchedMealSlot(LocalDate.parse("2023-01-30").toDate, 2, recipeExample2)
  )

  "RecipeController#mealSlotToArray" should "return the correct 2d array" in {

      val database = mock[RecipeDao]
      when(database.fetchAllMealSlots(1, "2023-01-02"))
        .thenReturn(Future.successful(fetchedMealSlotExample))

      val controller = new RecipeController(Helpers.stubControllerComponents(), database)

      val request = FakeRequest(GET, "/allMeals")
        .withSession("USERID" -> "1")

      val result = controller.getAllMealSlots("2023-01-02").apply(request)

      val actual = contentAsString(result)
      val expected = Source.fromResource("mealSlot1.json")
        .getLines()
        .mkString
        .replaceAll(" ", "")

      actual shouldBe expected
  }
}
