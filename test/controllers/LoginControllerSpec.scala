package controllers

import controllers.api.LoginController
import models.{ LoginDao, RegisterData }
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json
import play.api.test.Helpers.{ POST, contentAsString, defaultAwaitTimeout, status }
import play.api.test.{ FakeRequest, Helpers }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

class LoginControllerSpec extends AnyFlatSpec with Matchers {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val databaseMock: LoginDao = mock[LoginDao]

  val controller = new LoginController(Helpers.stubControllerComponents(), databaseMock)

  val registerDataExample: RegisterData =
    RegisterData("email1", "username1", "password1")

  "register" should "return an Ok response, stating the user has been added" in {
    when(databaseMock.addNewUser(registerDataExample))
      .thenReturn(Future.successful(Some(1)))

    val jsonBody = Json.parse(
      """{
        |  "date": "email1",
        |  "username": "username1",
        |  "password": "password1"
        |}""".stripMargin
    )

    val request = FakeRequest(POST, "/addMealSlot")
      .withSession("USERID" -> "1")
      .withHeaders("Content-type" -> "application/json")
      .withBody(jsonBody)

    val result = controller.register().apply(request)

    status(result) shouldBe BAD_REQUEST
    contentAsString(result) shouldBe "Error in processing Json data in request body."
  }
}
