package controllers

import controllers.api.LoginController
import models.{ LoginDao, Preferences, RegisterData }
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{ POST, contentAsString, defaultAwaitTimeout, status }
import play.api.test.{ FakeRequest, Helpers }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

class LoginControllerSpec extends AnyFlatSpec with Matchers {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val databaseMock: LoginDao = mock[LoginDao]

  val controller = new LoginController(Helpers.stubControllerComponents(), databaseMock)

  val preferences: Preferences = Preferences()

  val registerDataExample: RegisterData =
    RegisterData("email1", "username1", "password1", preferences)

  "register" should "return an Ok response, stating the user has been added" in {
    when(databaseMock.addNewUser(registerDataExample))
      .thenReturn(Future.successful(Some(1, 1)))

    val jsonBody = Json.parse(
      """{
        |  "email": "email1",
        |  "username": "username1",
        |  "password": "password1",
        |  "preferences": {
        |    "isVegan": false,
        |    "isVegetarian": false,
        |    "isKeto": false,
        |    "isLactose": false,
        |    "isHalal": false,
        |    "isKosher": false,
        |    "isDairyFree": false,
        |    "isLowCarbs": false
        |  }
        |}""".stripMargin
    )

    val request = FakeRequest(POST, "/addMealSlot")
      .withHeaders("Content-type" -> "application/json")
      .withBody(jsonBody)

    val result = controller.register().apply(request)

    status(result) shouldBe OK
    contentAsString(result) shouldBe "User successfully added."
  }
}
