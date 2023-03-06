package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import controllers.api.RecipeController
import models._
import org.joda.time.LocalDate
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers.{ GET, POST, contentAsString, defaultAwaitTimeout }
import play.api.test.{ FakeRequest, Helpers }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }
import scala.io.Source

class RecipeControllerSpec extends AnyFlatSpec with MockitoSugar {

  val as: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val mat: Materializer = Materializer(as)

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
    ),
    imageRef = "imageRef1"
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
    ),
    imageRef = "imageRef2"
  )

  val fetchedMealSlotExample: Seq[FetchedMealSlot] = Seq(
    FetchedMealSlot(LocalDate.parse("2023-01-30").toDate, 1, recipeExample1),
    FetchedMealSlot(LocalDate.parse("2023-01-30").toDate, 2, recipeExample2)
  )

  val receivedMealSlotExample: ReceivedMealSlot =
    ReceivedMealSlot(LocalDate.parse("2023-01-30").toDate, 1, 1, "1")

  val databaseMock: RecipeDao = mock[RecipeDao]
  val imageDaoMock: ImageDao = mock[ImageDao]

  "mealSlotToArray" should "return the correct 2d array" in {

    when(databaseMock.fetchAllMealSlots(1, "2023-01-02"))
      .thenReturn(Future.successful(fetchedMealSlotExample))

    when(imageDaoMock.fetchImages(Seq("imageRef1", "imageRef2")))
      .thenReturn(Future.successful(()))
    when(imageDaoMock.imagesToString)
      .thenReturn(Map.empty)

    val controller = new RecipeController(Helpers.stubControllerComponents(), databaseMock, imageDaoMock)

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

  "addMealSlot" should "respond with meal being successfully added" in {

    when(databaseMock.addMealSlot(receivedMealSlotExample))
      .thenReturn(Future.successful(Some(1)))

    val controller = new RecipeController(Helpers.stubControllerComponents(), databaseMock, imageDaoMock)

    val jsonBody = Json.parse(
      """{
        |  "date": "2023-01-30",
        |  "mealNum": 1,
        |  "recipeId": 1
        |}""".stripMargin
    )

    val request = FakeRequest(POST, "/addMealSlot")
      .withSession("USERID" -> "1")
      .withHeaders("Content-type" -> "application/json")
      .withBody(jsonBody)

    val result = controller.addMealSlot().apply(request)

    val actual = contentAsString(result)
    actual shouldBe "Meal successfully added."
  }


  "deleteMealSlot" should "respond with meal being successfully deleted" in {

    when(databaseMock.deleteMealSlot(receivedMealSlotExample))
      .thenReturn(Future.successful(1))

    val controller = new RecipeController(Helpers.stubControllerComponents(), databaseMock, imageDaoMock)

    val jsonBody = Json.parse(
      """{
        |  "date": "2023-01-30",
        |  "mealNum": 1,
        |  "recipeId": 1
        |}""".stripMargin
    )

    val request = FakeRequest(POST, "/addMealSlot")
      .withSession("USERID" -> "1")
      .withHeaders("Content-type" -> "application/json")
      .withBody(jsonBody)

    val result = controller.deleteMealSlot().apply(request)

    val actual = contentAsString(result)
    actual shouldBe "Meal successfully deleted."
  }
}
