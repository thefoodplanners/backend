package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import controllers.api.CalendarController
import models._
import org.joda.time.LocalDate
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{ DELETE, GET, POST, PUT, contentAsString, defaultAwaitTimeout, status }
import play.api.test.{ FakeRequest, Helpers }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }
import scala.io.Source

class CalendarControllerSpec extends AnyFlatSpec with MockitoSugar {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val as: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(as)

  val databaseMock: CalendarDao = mock[CalendarDao]
  val imageDaoMock: ImageDao = mock[ImageDao]
  val userDaoMock: UserDao = mock[UserDao]

  val controller = new CalendarController(Helpers.stubControllerComponents(), databaseMock, imageDaoMock, userDaoMock)

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
    preferences = Preferences(),
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
    preferences = Preferences(),
    imageRef = "imageRef2"
  )

  val fetchedMealSlotExample: Seq[FetchedMealSlot] = Seq(
    FetchedMealSlot(1, LocalDate.parse("2023-01-30").toDate, 1, recipeExample1),
    FetchedMealSlot(2, LocalDate.parse("2023-01-30").toDate, 2, recipeExample2)
  )

  val receivedMealSlotExample: ReceivedMealSlot =
    ReceivedMealSlot(LocalDate.parse("2023-01-30").toDate, 1)

  val updateMealSlotExample: UpdateMealSlot =
    UpdateMealSlot(LocalDate.parse("2023-01-30").toDate, 1, 1, 2)

  "mealSlotToArray" should "return the correct 2d array" in {

    when(databaseMock.fetchAllMealSlots("1", "2023-01-02"))
      .thenReturn(Future.successful(fetchedMealSlotExample))

    when(imageDaoMock.fetchImages(Seq("imageRef1", "imageRef2")))
      .thenReturn(Future.successful(()))
    when(imageDaoMock.imagesToString)
      .thenReturn(Map.empty)

    val request = FakeRequest(GET, "/calendar/meals")
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

    when(databaseMock.deleteMealSlot("1", 1))
      .thenReturn(Future.successful(1))

    val request = FakeRequest(DELETE, "/calendar/meals/1")
      .withSession("USERID" -> "1")

    val result = controller.deleteMealSlot(1).apply(request)

    status(result) shouldBe OK
    contentAsString(result) shouldBe "Meal successfully deleted."
  }

  behavior of "updateMealSlot"

  it should "return an Ok response, stating meal has successfully been updated" in {

    when(databaseMock.updateMealSlot("1", 1, 1))
      .thenReturn(Future.successful(1))

    val request = FakeRequest(PUT, "/calendar/meals/1")
      .withSession("USERID" -> "1")

    val result = controller.updateMealSlot(1, 1).apply(request)

    status(result) shouldBe OK
    contentAsString(result) shouldBe "Meal successfully updated."
  }
}
