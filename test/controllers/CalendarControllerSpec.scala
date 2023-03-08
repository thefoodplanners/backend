package controllers

import controllers.api.CalendarController
import models._
import org.joda.time.LocalDate
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{ BAD_REQUEST, OK }
import play.api.libs.json.Json
import play.api.test.Helpers.{ GET, POST, contentAsString, defaultAwaitTimeout, status }
import play.api.test.{ FakeRequest, Helpers }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }
import scala.io.Source

class CalendarControllerSpec extends AnyFlatSpec with MockitoSugar {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val databaseMock: CalendarDao = mock[CalendarDao]
  val imageDaoMock: ImageDao = mock[ImageDao]

  val controller = new CalendarController(Helpers.stubControllerComponents(), databaseMock, imageDaoMock)

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
    ReceivedMealSlot(LocalDate.parse("2023-01-30").toDate, 1, 1)

  val updateMealSlotExample: UpdateMealSlot =
    UpdateMealSlot(LocalDate.parse("2023-01-30").toDate, 1, 1, 2)

  "mealSlotToArray" should "return the correct 2d array" in {

    when(databaseMock.fetchAllMealSlots(1, "2023-01-02"))
      .thenReturn(Future.successful(fetchedMealSlotExample))

    when(imageDaoMock.fetchImages(Seq("imageRef1", "imageRef2")))
      .thenReturn(Future.successful(()))
    when(imageDaoMock.imagesToString)
      .thenReturn(Map.empty)

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

  "addMealSlot" should "return an Ok response, meal being successfully added" in {

    when(databaseMock.addMealSlot("1", receivedMealSlotExample))
      .thenReturn(Future.successful(Some(1)))

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

    status(result) shouldBe OK
    contentAsString(result) shouldBe "Meal successfully added."
  }


  "deleteMealSlot" should "return an Ok response, stating meal has successfully been deleted" in {

    when(databaseMock.deleteMealSlot("1", receivedMealSlotExample))
      .thenReturn(Future.successful(1))

    val jsonBody = Json.parse(
      """{
        |  "date": "2023-01-30",
        |  "mealNum": 1,
        |  "recipeId": 1
        |}""".stripMargin
    )

    val request = FakeRequest(POST, "/deleteMealSlot")
      .withSession("USERID" -> "1")
      .withHeaders("Content-type" -> "application/json")
      .withBody(jsonBody)

    val result = controller.deleteMealSlot().apply(request)

    status(result) shouldBe OK
    contentAsString(result) shouldBe "Meal successfully deleted."
  }

  behavior of "updateMealSlot"

  it should "return an Ok response, stating meal has successfully been updated" in {

    when(databaseMock.updateMealSlot("1", updateMealSlotExample))
      .thenReturn(Future.successful(1))

    val jsonBody = Json.parse(
      """{
        |  "date": "2023-01-30",
        |  "mealNum": 1,
        |  "oldRecipeId": 1,
        |  "newRecipeId": 2
        |}""".stripMargin
    )

    val request = FakeRequest(POST, "/updateMealSlot")
      .withSession("USERID" -> "1")
      .withHeaders("Content-type" -> "application/json")
      .withBody(jsonBody)

    val result = controller.updateMealSlot().apply(request)

    status(result) shouldBe OK
    contentAsString(result) shouldBe "Meal successfully updated."
  }

  it should "return a BadRequest, stating error in Json data" in {
    val invalidJsonBody = Json.parse(
      """{
        |  "date": "2023-01-30",
        |  "mealNum": 1,
        |  "oldRecipeId": 1
        |}""".stripMargin
    )

    val request = FakeRequest(POST, "/updateMealSlot")
      .withSession("USERID" -> "1")
      .withHeaders("Content-type" -> "application/json")
      .withBody(invalidJsonBody)


    val result = controller.updateMealSlot().apply(request)

    status(result) shouldBe BAD_REQUEST
    contentAsString(result) shouldBe "Error in processing Json data in request body."
  }
}
